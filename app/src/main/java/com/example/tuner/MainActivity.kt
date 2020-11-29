package com.example.tuner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MyCallback {
    private val processing = PitchProcessing(this@MainActivity)
    private val sampleRate = 44100
    private val bufferSize = 4096
    private val recordOverlaps = 3072
    private lateinit var instrumentSpinner : Spinner
    private lateinit var tuningSpinner : Spinner

    object CurTuning {
        internal lateinit var curTuning : String
    }

    object CurInstrument {
        internal lateinit var curInstrument : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // save current state of dark mode
        val isNightMode: Boolean = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", true)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)


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
                    "Guitar" -> {
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
                    "Bass" -> {
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
                    "Ukulele" -> {
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
                    "Chromatic" -> tuningSpinner.visibility = View.GONE
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
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // popup window
        val annotation = findViewById<View>(R.id.annotation) as TextView
        annotation.setOnClickListener {
            val intent = Intent(this, PopUpWindowActivity::class.java)
            startActivity(intent)
        }

        // ask for microphone permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        }

        // detecting frequencies through microphone
        val dispatcher: AudioDispatcher =
            AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, recordOverlaps)
        val pdh = PitchDetectionHandler { res, _ ->
            val pitchInHz: Float = res.pitch
            val probability : Float = res.probability
            //runOnUiThread{updateNote(probability.toString())}
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
        if (id == R.id.privacy_policy) {
            Toast.makeText(this, "Item Two Clicked", Toast.LENGTH_LONG).show()
            return true
        }
        if (id == R.id.donate) {
            Toast.makeText(this, "Item Three Clicked", Toast.LENGTH_LONG).show()
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
        up.setImageDrawable(drawableUp)
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
        down.setImageDrawable(drawableDown)
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
        noteText.gravity = Gravity.CENTER
        noteText.text = note
    }

    // set size of note displayed
    fun noteSize() {
        if (noteText.text in arrayOf(
                "A", "A#", "Bb", "B", "C", "C#", "Db", "D",
                "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab"
            )) {
            noteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 280F)
        }
    }
}