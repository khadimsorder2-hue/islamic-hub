package com.islamichub.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import java.util.Calendar

data class CalendarUiState(
    val monthTitle: String = "",
    val hijriMonthName: String = "",
    val hijriYear: String = "",
    val days: List<HijriDayItem> = emptyList(),
    val isLoading: Boolean = true,
    val selectedMonthOffset: Int = 0  // 0 = current month, -1 = prev, 1 = next
)

data class HijriDayItem(
    val gregorianDay: Int,
    val gregorianMonth: String,
    val hijriDay: String,
    val hijriMonth: String,
    val hijriYear: String,
    val weekdayBn: String,
    val weekdayEn: String,
    val isToday: Boolean = false,
    val islamicEvent: String? = null
)

class CalendarViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init { loadMonth(0) }

    fun loadMonth(offset: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, offset)

            val monthNamesBn = listOf(
                "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
            )
            val hijriMonthsBn = listOf(
                "মুহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
                "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শা'বান",
                "রমজান", "শাওয়াল", "জিলক্বদ", "জিলহজ্জ"
            )
            val hijriMonthsAr = listOf(
                "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
                "جمادى الأولى", "جمادى الثانية", "رجب", "شعبان",
                "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
            )
            val hijriMonthsEn = listOf(
                "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
                "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
                "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
            )
            // Java Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
            // Bangla weekday order: [Sun, Mon, Tue, Wed, Thu, Fri, Sat]
            val weekdaysBn = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
            val weekdaysEn = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) // 0-indexed
            val today = Calendar.getInstance()
            val isCurrentMonth = year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH)
            val todayDay = today.get(Calendar.DAY_OF_MONTH)

            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }.get(Calendar.DAY_OF_WEEK)

            fun islamicEventFor(hijriMonthIdx: Int, hijriDay: Int): String? = when {
                hijriMonthIdx == 0 && hijriDay == 10 -> "আশুরা (১০ মুহররম)"
                hijriMonthIdx == 2 && hijriDay == 12 -> "ঈদে মিলাদুন্নবী (১২ রবিউল আউয়াল)"
                hijriMonthIdx == 6 && hijriDay == 27 -> "শবে মেরাজ (২৭ রজব)"
                hijriMonthIdx == 7 && hijriDay == 15 -> "শবে বরাত (১৫ শা'বান)"
                hijriMonthIdx == 8 && hijriDay == 1 -> "রমজান শুরু"
                hijriMonthIdx == 8 && hijriDay == 27 -> "শবে কদর (২৭ রমজান)"
                hijriMonthIdx == 9 && hijriDay == 1 -> "ঈদুল ফিতর"
                hijriMonthIdx == 11 && hijriDay == 9 -> "ঈদুল আযহা (৯ জিলহজ্জ)"
                hijriMonthIdx == 11 && hijriDay == 10 -> "ঈদুল আযহা (১০ জিলহজ্জ)"
                else -> null
            }

            val daysList = mutableListOf<HijriDayItem>()
            // Add empty days for alignment (Calendar.SUNDAY=1, so subtract 1 to get 0-based index)
            for (i in 1 until firstDayOfWeek) {
                daysList.add(HijriDayItem(0, "", "", "", "", "", "", false))
            }

            // BUGFIX: fetch the *real* per-day Hijri date for this exact
            // Gregorian month from Aladhan's calendar endpoint, instead of
            // anchoring on today's Hijri date and incrementing with a fixed
            // (wrong) 29-day month assumption. That old approach showed the
            // wrong Hijri date for every month except the current one, and
            // drifted further with each next/prev navigation.
            var headerHijriDay = 1
            var headerHijriMonthIdx = 0
            var headerHijriYear = 1447
            var usedRealCalendar = false

            try {
                val monthResult = container.prayerRepository.getHijriMonthCalendar(
                    year = year,
                    month = month + 1 // Aladhan calendar endpoint is 1-indexed
                )
                val monthData = monthResult.getOrNull()
                if (monthData != null && monthData.size >= daysInMonth) {
                    usedRealCalendar = true
                    for (day in 1..daysInMonth) {
                        val hijri = monthData[day - 1].date.hijri
                        val hijriDayNum = hijri.day.toIntOrNull() ?: day
                        val hijriMonthIdx = (hijri.month.number - 1).coerceIn(0, 11)
                        val hijriYearNum = hijri.year.toIntOrNull() ?: 1447

                        val dayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                        }
                        val weekdayIdx = dayCal.get(Calendar.DAY_OF_WEEK) - 1
                        val isToday = isCurrentMonth && day == todayDay

                        daysList.add(HijriDayItem(
                            gregorianDay = day,
                            gregorianMonth = monthNamesBn[month],
                            hijriDay = hijriDayNum.toString(),
                            hijriMonth = "${hijriMonthsBn[hijriMonthIdx]} (${hijriMonthsAr[hijriMonthIdx]})",
                            hijriYear = "$hijriYearNum هـ",
                            weekdayBn = weekdaysBn[weekdayIdx],
                            weekdayEn = weekdaysEn[weekdayIdx],
                            isToday = isToday,
                            islamicEvent = islamicEventFor(hijriMonthIdx, hijriDayNum)
                        ))

                        if (day == 1) {
                            headerHijriDay = hijriDayNum
                            headerHijriMonthIdx = hijriMonthIdx
                            headerHijriYear = hijriYearNum
                        }
                    }
                }
            } catch (_: Exception) { /* fall through to offline approximation below */ }

            // Offline fallback — only used when the Aladhan calendar call
            // fails (no network). Anchors on today's real Hijri date and
            // shifts by the same number of days as the Gregorian offset,
            // which is far closer than the old "always assume 29-day
            // months" approach, though still an approximation.
            if (!usedRealCalendar) {
                var hijriBaseDay = 1
                var hijriBaseMonthIdx = 0
                var hijriBaseYear = 1447
                try {
                    container.prayerRepository.getDefaultPrayerTimes().getOrNull()?.hijriDate?.let { hijriStr ->
                        val parts = hijriStr.split(" ")
                        if (parts.size >= 4) {
                            hijriBaseDay = parts[0].toIntOrNull() ?: 1
                            val monthName = parts[1]
                            hijriBaseMonthIdx = hijriMonthsEn.indexOfFirst { it.equals(monthName, ignoreCase = true) }
                                .let { if (it >= 0) it else 0 }
                            hijriBaseYear = parts[2].toIntOrNull() ?: 1447
                        }
                    }
                } catch (_: Exception) { }

                // Shift the anchor by the Gregorian day-difference between
                // today and the 1st of the target month (average Hijri
                // month length 29.53 days), so navigating months at least
                // moves in the right direction and rough range.
                val firstOfMonthCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1)
                }
                val todayMidnight = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val dayShift = ((firstOfMonthCal.timeInMillis - todayMidnight.timeInMillis) / 86_400_000L).toInt()
                var totalHijriDay = hijriBaseDay + dayShift
                var hijriMonthIdx = hijriBaseMonthIdx
                var hijriYear = hijriBaseYear
                // Normalize using 29.53-day average month length
                while (totalHijriDay > 29) {
                    totalHijriDay -= 30
                    hijriMonthIdx++
                    if (hijriMonthIdx > 11) { hijriMonthIdx = 0; hijriYear++ }
                }
                while (totalHijriDay < 1) {
                    hijriMonthIdx--
                    if (hijriMonthIdx < 0) { hijriMonthIdx = 11; hijriYear-- }
                    totalHijriDay += 30
                }

                headerHijriDay = totalHijriDay
                headerHijriMonthIdx = hijriMonthIdx
                headerHijriYear = hijriYear

                var hijriDay = totalHijriDay
                for (day in 1..daysInMonth) {
                    val dayCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                    }
                    val weekdayIdx = dayCal.get(Calendar.DAY_OF_WEEK) - 1
                    val isToday = isCurrentMonth && day == todayDay

                    daysList.add(HijriDayItem(
                        gregorianDay = day,
                        gregorianMonth = monthNamesBn[month],
                        hijriDay = hijriDay.toString(),
                        hijriMonth = "${hijriMonthsBn[hijriMonthIdx]} (${hijriMonthsAr[hijriMonthIdx]})",
                        hijriYear = "$hijriYear هـ (আনুমানিক)",
                        weekdayBn = weekdaysBn[weekdayIdx],
                        weekdayEn = weekdaysEn[weekdayIdx],
                        isToday = isToday,
                        islamicEvent = islamicEventFor(hijriMonthIdx, hijriDay)
                    ))

                    hijriDay++
                    if (hijriDay > 29) {
                        hijriDay = 1
                        hijriMonthIdx = (hijriMonthIdx + 1) % 12
                        if (hijriMonthIdx == 0) hijriYear++
                    }
                }
            }

            _state.value = CalendarUiState(
                monthTitle = "${monthNamesBn[month]} $year",
                hijriMonthName = "${hijriMonthsBn[headerHijriMonthIdx]} / ${hijriMonthsAr[headerHijriMonthIdx]} / ${hijriMonthsEn[headerHijriMonthIdx]}",
                hijriYear = "$headerHijriYear هـ",
                days = daysList,
                isLoading = false,
                selectedMonthOffset = offset
            )
        }
    }

    fun nextMonth() { loadMonth(_state.value.selectedMonthOffset + 1) }
    fun prevMonth() { loadMonth(_state.value.selectedMonthOffset - 1) }
}
