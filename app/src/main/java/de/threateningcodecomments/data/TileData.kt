package de.threateningcodecomments.data

import android.text.Editable
import android.widget.AutoCompleteTextView
import com.google.firebase.database.Exclude
import de.threateningcodecomments.accessibility.MyLog
import java.lang.IllegalStateException

class TileData(val mode: Int = MODE_STRING) {

    fun logFieldValue() =
        add(
            (dataField?.text.toString()) ?: "null"
        )

    var isInitialized: Boolean = false

    val lastEntry: String?
        get() {
            val value = list.lastOrNull().toString()

            return if (value == "null") null
            else value
        }

    private fun add(text: String): Boolean {
        if (text == "null")
            return false

        if (text == "")
            return true

        when (mode) {
            MODE_INT ->
                intArr.add(text.toInt())

            MODE_DOUBLE ->
                doubleArr.add(text.toDouble())

            MODE_STRING ->
                strArr.add(text)
        }

        dataField?.setText("")
        MyLog.d("adds $text to list")

        return true
    }

    fun remove(text: Editable?) = remove(text.toString())

    fun remove(text: String) {
        if (text == "null")
            return

        when (mode) {
            MODE_INT ->
                intArr.remove(text.toInt())

            MODE_DOUBLE ->
                doubleArr.remove(text.toDouble())

            MODE_STRING ->
                strArr.remove(text)
        }
    }

    val list: java.util.ArrayList<out Any>
        get() = when (mode) {
            MODE_INT ->
                intArr

            MODE_DOUBLE ->
                doubleArr

            MODE_STRING ->
                strArr

            else ->
                throw IllegalStateException("TileDataMode is unrecognizable: Mode = $mode")
        }

    @Exclude
    @JvmField
    var dataField: AutoCompleteTextView? = null

    private val strArr = ArrayList<String>()
    private val intArr = ArrayList<Int>()
    private val doubleArr = ArrayList<Double>()

    companion object {
        const val MODE_INT = 0
        const val MODE_DOUBLE = 1
        const val MODE_STRING = 69
    }
}