package de.threateningcodecomments.accessibility

class CountdownSettings {
    var countDownTime: Long = DEFAULT_COUNTDOWN_TIME
        set(value) {
            field = value

            if (field != DEFAULT_COUNTDOWN_TIME) {
                val cdTimeStringAsLong = RC.Conversions.Time.convertReadableToMillis(countDownTimeString)
                if (cdTimeStringAsLong != field) {
                    val valueAsString = RC.Conversions.Time.millisToHHMMSS(field)
                    countDownTimeString = valueAsString
                }
            }
        }
    var countDownTimeString: String = DEFAULT_COUNTDOWN_TIME_STRING
        set(value) {
            field = value

            if (field != DEFAULT_COUNTDOWN_TIME_STRING) {
                val valueAsLong = RC.Conversions.Time.convertReadableToMillis(field)
                if (countDownTime != valueAsLong) {
                    countDownTime = valueAsLong
                }
            }
        }

    var reminds: Boolean = DEFAULT_REMIND
    var hasCustomReminder: Boolean = DEFAULT_CUSTOM_REMINDER
    var pathToRing: String = DEFAULT_RING

    constructor(countDownTime: Long = DEFAULT_COUNTDOWN_TIME, reminds: Boolean = DEFAULT_REMIND, hasCustomReminder: Boolean = DEFAULT_CUSTOM_REMINDER, pathToRing: String = DEFAULT_RING) {
        this.countDownTime = countDownTime
        this.reminds = reminds
        this.hasCustomReminder = hasCustomReminder
        this.pathToRing = pathToRing
    }

    constructor() : this(DEFAULT_COUNTDOWN_TIME, DEFAULT_REMIND, DEFAULT_CUSTOM_REMINDER, DEFAULT_RING)

    companion object {
        const val DEFAULT_COUNTDOWN_TIME = 1000L
        const val DEFAULT_COUNTDOWN_TIME_STRING = "00:00:01"
        const val DEFAULT_REMIND = true
        const val DEFAULT_CUSTOM_REMINDER = false
        const val DEFAULT_RING = "default ring tone path"
    }
}