package com.islamichub.app.data.repo

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * In-app update checker (v5.6.0).
 *
 * Resolution strategy — deliberately avoids the GitHub REST API as the primary
 * source because unauthenticated API calls are limited to 60 req/hour *per IP*;
 * on carrier-grade-NAT mobile networks (common in Bangladesh) that quota is
 * shared by many subscribers and often exhausted, returning HTTP 403.
 *
 * Primary method (no rate limit at all):
 *   GET https://github.com/{repo}/releases/latest  →  302 redirect to
 *   .../releases/tag/vX.Y.Z  →  parse the tag from the Location header.
 *
 * The signed APK asset name is deterministic (the release workflow renames it
 * to islamichub-release.apk), so the download URL is built as
 *   https://github.com/{repo}/releases/download/vX.Y.Z/islamichub-release.apk
 * and HEAD-verified; if the asset is missing we fall back to the release page.
 *
 * The REST API is used only as an OPTIONAL enrichment to fetch release notes,
 * wrapped in its own try/catch so its failure can never break the check.
 */
class UpdateChecker(private val context: Context) {

    data class AppUpdate(
        val latestVersion: String,      // "5.6.0" (no leading "v")
        val currentVersion: String,     // running versionName
        val downloadUrl: String,        // direct APK asset URL (or release page)
        val releaseNotes: String,       // may be blank when API was rate-limited
        val publishedAt: String = ""
    ) {
        val isNewer: Boolean =
            compareVersions(latestVersion, currentVersion) > 0
    }

    sealed class UpdateResult {
        data class Success(val update: AppUpdate) : UpdateResult()
        data class Failure(val message: String) : UpdateResult()
    }

    /**
     * Resolve the latest release and compare against [currentVersionName].
     * Never throws — failures come back as [UpdateResult.Failure].
     */
    suspend fun check(currentVersionName: String): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val tag = resolveLatestTagViaRedirect()
                ?: return@withContext UpdateResult.Failure("latest release not found")

            val cleanTag = tag.removePrefix("v").removePrefix("V")

            // Deterministic APK asset URL, verified with a cheap HEAD request.
            val assetUrl = "https://github.com/$REPO/releases/download/$tag/$APK_ASSET"
            val pageUrl = "https://github.com/$REPO/releases/tag/$tag"
            val downloadUrl = if (urlExists(assetUrl)) assetUrl else pageUrl

            // Optional: release notes via the REST API (best-effort only).
            val (notes, publishedAt) = fetchReleaseNotesViaApi(tag)

            UpdateResult.Success(
                AppUpdate(
                    latestVersion = cleanTag,
                    currentVersion = currentVersionName.removePrefix("v").removePrefix("V"),
                    downloadUrl = downloadUrl,
                    releaseNotes = notes,
                    publishedAt = publishedAt
                )
            )
        } catch (e: Exception) {
            UpdateResult.Failure(e.message ?: "network error")
        }
    }

    /** Open the APK download URL in the user's browser. */
    fun openDownloadPage(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            // No browser available — nothing sensible to do.
        }
    }

    /**
     * Follow https://github.com/{repo}/releases/latest one hop manually and
     * read the tag from the Location header. Returns null if unavailable.
     */
    private fun resolveLatestTagViaRedirect(): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL("https://github.com/$REPO/releases/latest")
                .openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.connect()
            val code = conn.responseCode
            if (code in 300..399) {
                val loc = conn.getHeaderField("Location") ?: return null
                loc.substringAfterLast("/tag/", "")
                    .trim()
                    .takeIf { it.isNotBlank() }
            } else null
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Best-effort release notes via the REST API. Rate-limit failures are
     * swallowed — notes simply stay blank.
     */
    private fun fetchReleaseNotesViaApi(tag: String): Pair<String, String> {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL("https://api.github.com/repos/$REPO/releases/tags/$tag")
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 6_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.connect()
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val notes = json.optString("body", "").take(NOTES_MAX_CHARS).trim()
                val date = json.optString("published_at", "")
                notes to date
            } else "" to ""
        } catch (_: Exception) {
            "" to ""
        } finally {
            conn?.disconnect()
        }
    }

    private fun urlExists(url: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 8_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.responseCode in 200..299
        } catch (_: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }

    companion object {
        private const val REPO = "khadimsorder2-hue/islamic-hub"
        private const val APK_ASSET = "islamichub-release.apk"
        private const val USER_AGENT = "islamic-hub-app"
        private const val NOTES_MAX_CHARS = 600

        /**
         * Semantic-ish version compare: splits "5.10.1" into [5,10,1] and
         * compares segment by segment. Returns >0 if [a] is newer, <0 if older,
         * 0 if equal. Non-numeric suffixes are ignored per segment.
         */
        fun compareVersions(a: String, b: String): Int {
            val pa = a.split('.').map { it.filter(Char::isDigit).toLongOrNull() ?: 0L }
            val pb = b.split('.').map { it.filter(Char::isDigit).toLongOrNull() ?: 0L }
            val len = maxOf(pa.size, pb.size)
            for (i in 0 until len) {
                val va = pa.getOrElse(i) { 0L }
                val vb = pb.getOrElse(i) { 0L }
                if (va != vb) return va.compareTo(vb)
            }
            return 0
        }
    }
}
