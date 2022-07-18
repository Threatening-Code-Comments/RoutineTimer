package de.threateningcodecomments.data

class TapSettings {
    val count: Int
        get() = _tapCount

    var increment = 1

    private var _tapCount: Int = 0

    fun increment(){
        _tapCount += increment
    }
}