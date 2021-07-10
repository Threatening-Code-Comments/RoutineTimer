package de.threateningcodecomments.accessibility

class TileData(val mode: Int = MODE_DEFAULT) {

    lateinit var data: ArrayList<*>

    init {
        data =
                when (mode) {
                    MODE_DOUBLE ->
                        ArrayList<Double>()
                    MODE_INT ->
                        ArrayList<Int>()
                    MODE_STRING ->
                        ArrayList<String>()
                    else ->
                        ArrayList<Any>()
                }
    }

    private val strArr = ArrayList<String>()
    private val intArr = ArrayList<Int>()
    private val doubleArr = ArrayList<Double>()

    companion object {
        const val MODE_INT = 0
        const val MODE_DOUBLE = 1
        const val MODE_STRING = 2
        const val MODE_DEFAULT = 69

        val DEFAULT_DATA = TileData(MODE_DEFAULT)
    }
}