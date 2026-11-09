package com.example.data.repository

import com.example.data.model.ScheduleEvent

object HolidayProvider {
    data class CountryInfo(
        val code: String,
        val name: String,
        val flagEmoji: String
    )

    val supportedCountries = listOf(
        CountryInfo("ID", "Indonesia", "🇮🇩"),
        CountryInfo("US", "United States", "🇺🇸"),
        CountryInfo("JP", "Jepang", "🇯🇵"),
        CountryInfo("GB", "United Kingdom", "🇬🇧"),
        CountryInfo("SG", "Singapura", "🇸🇬"),
        CountryInfo("AU", "Australia", "🇦🇺"),
        CountryInfo("MY", "Malaysia", "🇲🇾"),
        CountryInfo("DE", "Jerman", "🇩🇪")
    )

    fun getHolidaysForCountry(countryCode: String, year: Int = 2026): List<ScheduleEvent> {
        return when (countryCode) {
            "ID" -> listOf(
                ScheduleEvent(title = "Tahun Baru Masehi", date = "$year-01-01", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Tahun Baru Imlek 2577", date = "$year-02-17", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Isra Mikraj Nabi Muhammad", date = "$year-02-23", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Suci Nyepi (Tahun Baru Saka 1948)", date = "$year-03-21", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Raya Idul Fitri 1447 H", date = "$year-03-21", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Cuti Bersama Idul Fitri", date = "$year-03-22", isHoliday = true, countryCode = "ID", colorHex = 0xFFF97316),
                ScheduleEvent(title = "Wafat Yesus Kristus", date = "$year-04-03", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Buruh Internasional", date = "$year-05-01", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Kenaikan Yesus Kristus", date = "$year-05-14", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Raya Waisak 2570", date = "$year-05-31", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Lahir Pancasila", date = "$year-06-01", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Raya Idul Adha 1447 H", date = "$year-05-27", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Tahun Baru Islam 1448 H", date = "$year-06-16", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Kemerdekaan RI Ke-81", date = "$year-08-17", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Maulid Nabi Muhammad SAW", date = "$year-08-25", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Raya Natal", date = "$year-12-25", isHoliday = true, countryCode = "ID", colorHex = 0xFFEF4444)
            )
            "US" -> listOf(
                ScheduleEvent(title = "New Year's Day", date = "$year-01-01", isHoliday = true, countryCode = "US", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Martin Luther King Jr. Day", date = "$year-01-19", isHoliday = true, countryCode = "US", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Presidents' Day", date = "$year-02-16", isHoliday = true, countryCode = "US", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Memorial Day", date = "$year-05-25", isHoliday = true, countryCode = "US", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Juneteenth National Day", date = "$year-06-19", isHoliday = true, countryCode = "US", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Independence Day (4th of July)", date = "$year-07-04", isHoliday = true, countryCode = "US", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Labor Day", date = "$year-09-07", isHoliday = true, countryCode = "US", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Veterans Day", date = "$year-11-11", isHoliday = true, countryCode = "US", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Thanksgiving Day", date = "$year-11-26", isHoliday = true, countryCode = "US", colorHex = 0xFFF97316),
                ScheduleEvent(title = "Christmas Day", date = "$year-12-25", isHoliday = true, countryCode = "US", colorHex = 0xFFEF4444)
            )
            "JP" -> listOf(
                ScheduleEvent(title = "Gantan (Tahun Baru)", date = "$year-01-01", isHoliday = true, countryCode = "JP", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Seijin no Hi (Hari Kedewasaan)", date = "$year-01-12", isHoliday = true, countryCode = "JP", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Kenkoku Kinen no Hi", date = "$year-02-11", isHoliday = true, countryCode = "JP", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Tennō Tanjōbi (Ulang Tahun Kaisar)", date = "$year-02-23", isHoliday = true, countryCode = "JP", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Shunbun no Hi (Ekuinoks Musim Semi)", date = "$year-03-20", isHoliday = true, countryCode = "JP", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Shōwa no Hi (Golden Week)", date = "$year-04-29", isHoliday = true, countryCode = "JP", colorHex = 0xFF10B981),
                ScheduleEvent(title = "Kenpō Kinenbi", date = "$year-05-03", isHoliday = true, countryCode = "JP", colorHex = 0xFF10B981),
                ScheduleEvent(title = "Kodomo no Hi (Hari Anak)", date = "$year-05-05", isHoliday = true, countryCode = "JP", colorHex = 0xFF10B981),
                ScheduleEvent(title = "Umi no Hi (Hari Laut)", date = "$year-07-20", isHoliday = true, countryCode = "JP", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Yama no Hi (Hari Gunung)", date = "$year-08-11", isHoliday = true, countryCode = "JP", colorHex = 0xFF10B981),
                ScheduleEvent(title = "Bunka no Hi (Hari Kebudayaan)", date = "$year-11-03", isHoliday = true, countryCode = "JP", colorHex = 0xFF8B5CF6),
                ScheduleEvent(title = "Kinrō Kansha no Hi", date = "$year-11-23", isHoliday = true, countryCode = "JP", colorHex = 0xFFEF4444)
            )
            "GB" -> listOf(
                ScheduleEvent(title = "New Year's Day", date = "$year-01-01", isHoliday = true, countryCode = "GB", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Good Friday", date = "$year-04-03", isHoliday = true, countryCode = "GB", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Easter Monday", date = "$year-04-06", isHoliday = true, countryCode = "GB", colorHex = 0xFF3B82F6),
                ScheduleEvent(title = "Early May Bank Holiday", date = "$year-05-04", isHoliday = true, countryCode = "GB", colorHex = 0xFF10B981),
                ScheduleEvent(title = "Spring Bank Holiday", date = "$year-05-25", isHoliday = true, countryCode = "GB", colorHex = 0xFF10B981),
                ScheduleEvent(title = "Summer Bank Holiday", date = "$year-08-31", isHoliday = true, countryCode = "GB", colorHex = 0xFF10B981),
                ScheduleEvent(title = "Christmas Day", date = "$year-12-25", isHoliday = true, countryCode = "GB", colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Boxing Day", date = "$year-12-26", isHoliday = true, countryCode = "GB", colorHex = 0xFF3B82F6)
            )
            else -> listOf(
                ScheduleEvent(title = "Tahun Baru Global", date = "$year-01-01", isHoliday = true, countryCode = countryCode, colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Buruh Sedunia", date = "$year-05-01", isHoliday = true, countryCode = countryCode, colorHex = 0xFFEF4444),
                ScheduleEvent(title = "Hari Perdamaian Internasional", date = "$year-09-21", isHoliday = true, countryCode = countryCode, colorHex = 0xFF10B981),
                ScheduleEvent(title = "Hari Natal Bersama", date = "$year-12-25", isHoliday = true, countryCode = countryCode, colorHex = 0xFFEF4444)
            )
        }
    }
}
