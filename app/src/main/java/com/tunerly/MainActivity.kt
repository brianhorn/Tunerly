package com.tunerly

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.example.tuner.R
import com.example.tuner.databinding.ActivityMainBinding
import java.util.*

class MainActivity : LocalizationActivity(), MyCallback {
    private lateinit var binding: ActivityMainBinding
    private val processing = PitchProcessing(this@MainActivity)
    private val sampleRate = 44100
    private var bufferSize: Int = 4096
    private val recordOverlaps = 3072
    private val localizationDelegate = LocalizationApplicationDelegate()
    private lateinit var instrumentSpinner: Spinner
    private lateinit var tuningSpinner: Spinner

    object CurTuning {
        internal lateinit var curTuning: String
    }

    object CurInstrument {
        internal lateinit var curInstrument: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ask for microphone permissions
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1234
            )
        }

        // save current state of dark mode
        val isNightMode: Boolean = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
            "dark_theme",
            false
        )
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setTheme(R.style.AppTheme)
        setContentView(view)

        // keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // initializing instrument spinner
        instrumentSpinner = findViewById(R.id.instrument_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.instruments_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            instrumentSpinner.adapter = adapter
        }
        instrumentSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, pos: Int, id: Long
            ) {
                CurInstrument.curInstrument = instrumentSpinner.getItemAtPosition(pos).toString()
                when (parent.getItemAtPosition(pos).toString()) {
                    getString(R.string.guitar) -> {
                        tuningSpinner.visibility = View.VISIBLE
                        ArrayAdapter.createFromResource(
                            this@MainActivity,
                            R.array.tuning_array_guitar,
                            android.R.layout.simple_spinner_dropdown_item
                        ).also { adapter ->
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            tuningSpinner.adapter = adapter
                        }
                    }
                    getString(R.string.bass) -> {
                        tuningSpinner.visibility = View.VISIBLE
                        ArrayAdapter.createFromResource(
                            this@MainActivity,
                            R.array.tuning_array_bass,
                            android.R.layout.simple_spinner_dropdown_item
                        ).also { adapter ->
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            tuningSpinner.adapter = adapter
                        }
                    }
                    getString(R.string.ukulele) -> {
                        tuningSpinner.visibility = View.VISIBLE
                        ArrayAdapter.createFromResource(
                            this@MainActivity,
                            R.array.tuning_array_ukulele,
                            android.R.layout.simple_spinner_dropdown_item
                        ).also { adapter ->
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            tuningSpinner.adapter = adapter
                        }
                    }
                    getString(R.string.chromatic) -> tuningSpinner.visibility = View.GONE

                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing.
            }
        }

        // initializing tuning spinner
        tuningSpinner = findViewById(R.id.tuning_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.tuning_array_guitar,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tuningSpinner.adapter = adapter
        }
        tuningSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, pos: Int, id: Long
            ) {
                CurTuning.curTuning = tuningSpinner.getItemAtPosition(pos).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing.
            }
        }

        // menu
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // annotation dialog
        val annotation = findViewById<View>(R.id.annotation) as TextView
        annotation.setOnClickListener {
            val dialog: DialogFragment = Dialog()
            dialog.show(supportFragmentManager, "MyDialogFragmentTag")
        }
        val permission: Int = PermissionChecker.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            audioProcessing()
        }
    }

    private fun audioProcessing() {
        // detecting frequencies through microphone
        val dispatcher: AudioDispatcher =
            AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, recordOverlaps)
        val pdh = PitchDetectionHandler { res, _ ->
            val pitchInHz: Float = res.pitch
            val probability: Float = res.probability
            if (pitchInHz > -1) {
                runOnUiThread { processing.tune(pitchInHz, probability)}
            }
        }
        val pitchProcessor: AudioProcessor =
            PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                sampleRate.toFloat(), bufferSize, pdh
            )
        dispatcher.addAudioProcessor(pitchProcessor)

        val audioThread = Thread(dispatcher, "Audio Thread")
        audioThread.start()
    }

    // set language to default phone language
    override fun attachBaseContext(newBase: Context) {
        localizationDelegate.setDefaultLanguage(newBase, Locale.getDefault())
        super.attachBaseContext(localizationDelegate.attachBaseContext(newBase))
    }

    // permission handling
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1234 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    audioProcessing()
                } else {
                    Log.d("TAG", "permission denied by user")
                }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    // Items in menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.settings) {
            val myIntent = Intent(this@MainActivity, SettingsActivity::class.java)
            this@MainActivity.startActivity(myIntent)
            return true
        }
        if (id == R.id.about) {
            val myIntent = Intent(this@MainActivity, AboutActivity::class.java)
            this@MainActivity.startActivity(myIntent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun colorTuned(tuningDirection: String) {
        var colorTop = R.color.colorControlNormal
        var colorBottom = R.color.colorControlNormal

        when (tuningDirection) {
            "none" -> {
                colorTop = R.color.colorTuned
                colorBottom = R.color.colorTuned
            }
            "up" -> {
                colorTop = R.color.colorOutOfTune
                colorBottom = R.color.colorControlNormal
            }
            "down" -> {
                colorTop = R.color.colorControlNormal
                colorBottom = R.color.colorOutOfTune
            }
        }

        val drawableUp = DrawableCompat.wrap(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_play_arrow
            )!!
        )
        binding.up.setImageDrawable(drawableUp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            DrawableCompat.setTint(drawableUp, ContextCompat.getColor(this, colorTop))

        } else {
            drawableUp.mutate().colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                colorTop,
                BlendModeCompat.SRC_ATOP
            )
        }

        val drawableDown = DrawableCompat.wrap(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_play_arrow
            )!!
        )
        binding.down.setImageDrawable(drawableDown)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            DrawableCompat.setTint(drawableDown, ContextCompat.getColor(this, colorBottom))

        } else {
            drawableDown.mutate().colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                colorBottom,
                BlendModeCompat.SRC_ATOP
            )
        }
    }

    override fun updateNote(note: String?) {
        binding.noteText.gravity = Gravity.CENTER
        binding.noteText.text = note
    }

    // set size of note displayed
    fun noteSize() {
        if (binding.noteText.text in arrayOf(
                "A", "A#", "Bb", "B", "C", "C#", "Db", "D",
                "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab"
            )) {
            binding.noteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 280F)
        }
    }
}