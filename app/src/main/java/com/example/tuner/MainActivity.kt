package com.example.tuner

import android.Manifest
import android.annotation.SuppressLint
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


class MainActivity : AppCompatActivity() {
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
            runOnUiThread { processPitch(pitchInHz) }
        }
        val pitchProcessor: AudioProcessor =
            PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050F, 1024, pdh)
        dispatcher.addAudioProcessor(pitchProcessor)

        val audioThread = Thread(dispatcher, "Audio Thread")
        audioThread.start()
    }

    // basic hz to note converter
    // includes 10 cents of leeway
    @SuppressLint("SetTextI18n")
    private fun processPitch(pitchInHz: Float) {
        if (noteText.text in arrayOf(
                "A", "A#", "Bb", "B", "C", "C#", "Db", "D",
                "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab"
            )) {
            noteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 300F)
        }
        when (pitchInHz) {
            in 82.31..82.51 -> noteText.text = "E"
            in 109.9..110.10 -> noteText.text = "A"
            in 146.7..146.9 -> noteText.text = "D"
            in 195.9..196.1 -> noteText.text = "G"
            in 246.8..247.0 -> noteText.text = "B"
            in 329.5..329.7 -> noteText.text = "E"
        }
    }

}