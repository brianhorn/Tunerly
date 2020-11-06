package com.example.tuner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
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

    override fun updateNote(note: String?) {
        noteText.text = note
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // initializing instrument spinner
        val instrumentSpinner: Spinner = findViewById(R.id.instrument_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.instruments_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            instrumentSpinner.adapter = adapter
        }

        // detecting frequencies through microphone
        val dispatcher: AudioDispatcher =
            AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)
        val pdh = PitchDetectionHandler { res, _ ->
            val pitchInHz: Float = res.pitch
            runOnUiThread { processing.processPitch(pitchInHz) }
        }
        val pitchProcessor: AudioProcessor =
            PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050F, 1024, pdh)
        dispatcher.addAudioProcessor(pitchProcessor)

        val audioThread = Thread(dispatcher, "Audio Thread")
        audioThread.start()
    }
    // set size of note displayed
    fun pitchDisplay() {
        if (noteText.text in arrayOf(
                "A", "A#", "Bb", "B", "C", "C#", "Db", "D",
                "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab"
            )) {
            noteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 300F)
        }
    }
}