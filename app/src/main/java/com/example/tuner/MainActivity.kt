package com.example.tuner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }

        val instrumentSpinner: Spinner = findViewById(R.id.instrument_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.instruments_array,
            android.R.layout.simple_spinner_item
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
    @SuppressLint("SetTextI18n")
    private fun processPitch(pitchInHz: Float) {
        pitchText.text = "" + pitchInHz
        if (pitchInHz >= 110 && pitchInHz < 123.47) {
            //A
            noteText.text = "A"
        } else if (pitchInHz >= 123.47 && pitchInHz < 130.81) {
            //B
            noteText.text = "B"
        } else if (pitchInHz >= 130.81 && pitchInHz < 146.83) {
            //C
            noteText.text = "C"
        } else if (pitchInHz >= 146.83 && pitchInHz < 164.81) {
            //D
            noteText.text = "D"
        } else if (pitchInHz in 164.81..174.61) {
            //E
            noteText.text = "E"
        } else if (pitchInHz >= 174.61 && pitchInHz < 185) {
            //F
            noteText.text = "F"
        } else if (pitchInHz >= 185 && pitchInHz < 196) {
            //G
            noteText.text = "G"
        }
    }

}