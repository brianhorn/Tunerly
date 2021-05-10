package com.tunerly

import android.os.Bundle
import android.view.MenuItem
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.example.tuner.R
import com.example.tuner.databinding.ActivityAboutBinding


class AboutActivity : LocalizationActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val versionName: String = packageManager.getPackageInfo(packageName, 0).versionName
        binding.versionTextView.text = versionName
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
}