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
            val weekdaysBn = listOf("শনিবার", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার")
            val weekdaysEn = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")

            // Try to get hijri date from Aladhan API
            var hijriBaseDay = 1
            var hijriBaseMonthIdx = 0
            var hijriBaseYear = 1447

            try {
                val result = container.prayerRepository.getDefaultPrayerTimes()
                result.getOrNull()?.hijriDate?.let { hijriStr ->
                    // Parse "17 Aug 2026" format → extract hijri
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

            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val today = Calendar.getInstance()
            val isCurrentMonth = year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH)
            val todayDay = today.get(Calendar.DAY_OF_MONTH)

            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }.get(Calendar.DAY_OF_WEEK)

            val daysList = mutableListOf<HijriDayItem>()
            // Add empty days for alignment
            for (i in 1 until firstDayOfWeek) {
                daysList.add(HijriDayItem(0, "", "", "", "", "", "", false))
            }

            // Generate days
            var hijriDay = hijriBaseDay
            var hijriMonthIdx = hijriBaseMonthIdx
            var hijriYear = hijriBaseYear
            val now = Calendar.getInstance()

            for (day in 1..daysInMonth) {
                val dayCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                val weekdayIdx = dayCal.get(Calendar.DAY_OF_WEEK) - 1
                val isToday = isCurrentMonth && day == todayDay

                // Islamic events
                var event: String? = null
                if (hijriMonthIdx == 0 && hijriDay == 10) event = "আশুরা"
                if (hijriMonthIdx == 2 && hijriDay == 12) event = "ঈদে মিলাদুন্নবী"
                if (hijriMonthIdx == 6 && hijriDay == 27) event = "শবে মেরাজ"
                if (hijriMonthIdx == 7 && hijriDay == 15) event = "শবে বরাত"
                if (hijriMonthIdx == 8 && hijriDay == 1) event = "রমজান শুরু"
                if (hijriMonthIdx == 8 && hijriDay == 27) event = "শবে কদর"
                if (hijriMonthIdx == 9 && hijriDay == 1) event = "ঈদুল ফিতর"
                if (hijriMonthIdx == 11 && hijriDay == 9) event = "ঈদুল আযহা"
                if (hijriMonthIdx == 11 && hijriDay == 10) event = "ঈদুল আযহা"

                daysList.add(HijriDayItem(
                    gregorianDay = day,
                    gregorianMonth = monthNamesBn[month],
                    hijriDay = hijriDay.toString(),
                    hijriMonth = "${hijriMonthsBn[hijriMonthIdx]} (${hijriMonthsAr[hijriMonthIdx]})",
                    hijriYear = "$hijriYear هـ",
                    weekdayBn = weekdaysBn[weekdayIdx],
                    weekdayEn = weekdaysEn[weekdayIdx],
                    isToday = isToday,
                    islamicEvent = event
                ))

                hijriDay++
                // Approximate hijri month length (29 or 30)
                if (hijriDay > 29) {
                    hijriDay = 1
                    hijriMonthIdx = (hijriMonthIdx + 1) % 12
                    if (hijriMonthIdx == 0) hijriYear++
                }
            }

            _state.value = CalendarUiState(
                monthTitle = "${monthNamesBn[month]} $year",
                hijriMonthName = "${hijriMonthsBn[hijriBaseMonthIdx]} / ${hijriMonthsAr[hijriBaseMonthIdx]} / ${hijriMonthsEn[hijriBaseMonthIdx]}",
                hijriYear = "$hijriBaseYear هـ",
                days = daysList,
                isLoading = false,
                selectedMonthOffset = offset
            )
        }
    }

    fun nextMonth() { loadMonth(_state.value.selectedMonthOffset + 1) }
    fun prevMonth() { loadMonth(_state.value.selectedMonthOffset - 1) }
}
