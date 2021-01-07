package de.threateningcodecomments.routinetimer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import de.threateningcodecomments.accessibility.MyLog
import de.threateningcodecomments.accessibility.ResourceClass

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private lateinit var dataSavingPref: SwitchPreferenceCompat
    private lateinit var debugModePref: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        instance = this

        dataSavingPref = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_general_dataSaving_key))!!
        dataSavingPref.onPreferenceChangeListener = this

        debugModePref = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_dev_debug_key))!!
        debugModePref.onPreferenceChangeListener = this
    }

    override fun onStart() {
        super.onStart()

        dataSavingPref.isChecked = preferences.general.dataSaving

        debugModePref.isChecked = preferences.dev.debug
        MyLog.d("debug mode is ${preferences.dev.debug}")
    }


    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        ResourceClass.updatePreference(preference, newValue)
        Toast.makeText(MainActivity.instance, "value changed to $newValue", Toast.LENGTH_SHORT).show()

        return true
    }

    companion object {
        lateinit var instance: SettingsFragment
        var preferences = Preferences()
    }

    class Preferences {
        var general: General = General()
        var dev: Dev = Dev()

        constructor() : this(General(), Dev())

        constructor(pref: Preferences) : this(pref.general, pref.dev)


        constructor(general: General, dev: Dev) {
            this.general = general
            this.dev = dev

            //printUpdate()
        }

        private fun printUpdate() {
            MyLog.d("updating preferences! $this")
        }

        class General : PreferenceCategory {
            var dataSaving: Boolean
            override val name: String = "general"

            constructor() : this(defaultDataSavingValue)

            constructor(dataSaving: Boolean) {
                this.dataSaving = dataSaving
            }

            companion object {
                private const val defaultDataSavingValue = false
            }

            override fun toString(): String {
                return "General:{dataSaving:$dataSaving}"
            }

            override fun copy(): PreferenceCategory {
                return General(dataSaving)
            }

            override fun copyWithoutName(): Any {
                return object {
                    val dataSaving = this@General.dataSaving
                }
            }

            override fun isIdenticalWith(prefs: SharedPreferences): Boolean {
                val dataSavingKey = MainActivity.instance.getString(R.string.pref_general_dataSaving_key)
                val sharedDataSaving = prefs.getBoolean(dataSavingKey, defaultDataSavingValue)
                if (sharedDataSaving != dataSaving)
                    return false

                return true
            }
        }

        class Dev : PreferenceCategory {
            override val name: String = "dev"
            var debug: Boolean

            constructor() : this(defaultDebugValue)

            constructor(debug: Boolean) {
                this.debug = debug
            }

            companion object {
                private const val defaultDebugValue = false

            }

            override fun toString(): String {
                return "Dev:{debug:$debug}"
            }

            override fun copy(): PreferenceCategory {
                return Dev(debug)
            }

            override fun copyWithoutName(): Any {
                return object {
                    val debug = this@Dev.debug
                }
            }

            override fun isIdenticalWith(prefs: SharedPreferences): Boolean {
                val debugKey = MainActivity.instance.getString(R.string.pref_dev_debug_key)
                val sharedDebug = prefs.getBoolean(debugKey, defaultDebugValue)
                if (sharedDebug != debug)
                    return false

                return true
            }
        }

        override fun toString(): String {
            return "prefs:{$general, $dev}"
        }

        fun isDifferentFromSharedPreferences(): Boolean {
            val sharedPrefs = MainActivity.instance.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)

            var identical = true
            if (!this.general.isIdenticalWith(sharedPrefs) || !this.dev.isIdenticalWith(sharedPrefs))
                identical = false

            return identical
        }

        interface PreferenceCategory {
            val name: String

            fun copy(): PreferenceCategory

            fun copyWithoutName(): Any

            fun isIdenticalWith(prefs: SharedPreferences): Boolean
        }

        companion object {
            const val sharedPreferenceName = "RoutineTimerSharedPreferences"
        }
    }
}