package de.threateningcodecomments.accessibility

import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference

class EditIntegerPreference : EditTextPreference {
    constructor(context: Context?) : super(context) {
        setInputMethod()
    }

    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet) {
        setInputMethod()
    }

    constructor(context: Context?, attributeSet: AttributeSet?, defStyle: Int) : super(
            context,
            attributeSet,
            defStyle
    ) {
        setInputMethod()
    }

    override fun getText(): String =
            try {
                java.lang.String.valueOf(sharedPreferences.getInt(key, 0))
            } catch (e: Exception) {
                "0"
            }

    override fun setText(text: String?) {
        try {
            if (text != null) {
                sharedPreferences?.edit()?.putInt(key, text.toInt())?.apply()
                summary = text
            } else {
                sharedPreferences?.remove(key)
                summary = ""
            }
        } catch (e: Exception) {
            sharedPreferences?.remove(key)
            summary = ""
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val defaultValueInt: Int =
                when (defaultValue) {
                    is Int -> defaultValue
                    is String -> try {
                        defaultValue.toInt()
                    } catch (ex: java.lang.Exception) {
                        0
                    }
                    else -> 0
                }

        text = sharedPreferences.getInt(key, defaultValueInt).toString()
    }

    private fun setInputMethod() {
        setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    fun SharedPreferences.remove(key: String) = edit().remove(key).apply()
}