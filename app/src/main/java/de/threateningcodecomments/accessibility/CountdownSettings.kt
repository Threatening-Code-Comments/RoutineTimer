package de.threateningcodecomments.accessibility

class CountdownSettings {
    var countDownTime: Long = DEFAULT_COUNTDOWN_TIME

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
        const val DEFAULT_REMIND = true
        const val DEFAULT_CUSTOM_REMINDER = false
        const val DEFAULT_RING = "default ring tone path"
    }
}