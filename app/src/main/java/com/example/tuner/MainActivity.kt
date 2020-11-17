package com.example.tuner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        var curTuning : String? = null
    }

    object CurInstrument {
        var curInstrument : String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                CurInstrument.curInstrument = parent.getItemAtPosition(pos).toString()
                when (parent.getItemAtPosition(pos).toString()) {
                    "Guitar" -> ArrayAdapter.createFromResource(
                        this@MainActivity,
                        R.array.tuning_array_guitar,
                        android.R.layout.simple_spinner_dropdown_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        tuningSpinner.adapter = adapter
                    }
                    "Bass" -> ArrayAdapter.createFromResource(
                        this@MainActivity,
                        R.array.tuning_array_bass,
                        android.R.layout.simple_spinner_dropdown_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        tuningSpinner.adapter = adapter
                    }
                    "Ukulele" -> ArrayAdapter.createFromResource(
                        this@MainActivity,
                        R.array.tuning_array_ukulele,
                        android.R.layout.simple_spinner_dropdown_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        tuningSpinner.adapter = adapter
                    }
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
                runOnUiThread { processing.tuneGuitar(pitchInHz, probability)}
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
            noteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 300F)
        }
    }
}