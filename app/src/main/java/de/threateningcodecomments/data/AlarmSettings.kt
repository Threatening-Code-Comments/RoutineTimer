package de.threateningcodecomments.data

// TODO: 18.06.2021 make date better
class AlarmSettings(val dayOfWeek: Int = DEFAULT_WEEKDAY, val date: String = DEFAULT_DATE, val repeat: Boolean =
        false, val alarmSound: String = DEFAULT_SOUND
) {

    companion object {
        val DEFAULT_SETTINGS: AlarmSettings = AlarmSettings()

        const val DEFAULT_WEEKDAY = Day.DEFAULT
        const val DEFAULT_DATE: String = "01.01.69"
        const val DEFAULT_SOUND = "DEFAULT"
    }
}