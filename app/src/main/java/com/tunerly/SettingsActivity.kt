package com.tunerly

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.example.tuner.R
import kotlinx.android.synthetic.main.activity_main.*


class SettingsActivity : LocalizationActivity() {

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
                    val locale = preference.entryValues[index]

                    if (locale == "de") {
                        (activity as SettingsActivity).changeLanguageGer()
                    }
                    if (locale == "en") {
                        (activity as SettingsActivity).changeLanguageEn()
                    }
                    if (locale == "ru") {
                        (activity as SettingsActivity).changeLanguageRu()
                    }
                }
                true
            }
        }
    }

    fun changeLanguageGer() {
        setLanguage("de")
    }

    fun changeLanguageEn() {
        setLanguage("en")
    }

    fun changeLanguageRu() {
        setLanguage("ru")
    }
}