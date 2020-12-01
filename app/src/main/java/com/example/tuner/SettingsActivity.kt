package com.example.tuner

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class SettingsActivity : AppCompatActivity() {
    //var locale: String = Locale.getDefault().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        lateinit var todo: String
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            // theme
            val switchPreferenceCompat: SwitchPreferenceCompat? =
                findPreference("dark_theme")
            switchPreferenceCompat!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    var isChecked = false
                    if (newValue is Boolean) isChecked = newValue
                    if (isChecked) {
                        preferenceManager.sharedPreferences.edit()
                            .putBoolean(getString(R.string.dark_theme), true).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    } else {
                        preferenceManager.sharedPreferences.edit()
                            .putBoolean(getString(R.string.dark_theme), false).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    true
                }

            // language
            val listPreference =
                findPreference<Preference>(getString(R.string.language_preference)) as ListPreference?
            listPreference?.setOnPreferenceChangeListener { preference, newValue ->
                if (preference is ListPreference) {
                    val index = preference.findIndexOfValue(newValue.toString())
                    val language = preference.entries[index]
                    val locale = preference.entryValues[index]
                    Log.i(
                        "selected val",
                        " position - $index value - $language, entryValue - $locale "
                    )
                    Toast.makeText(activity,locale, Toast.LENGTH_LONG).show()
                    todo = locale.toString()
                }
                true
            }
        }
    }
}