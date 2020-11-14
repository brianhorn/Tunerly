package com.example.tuner

import kotlin.math.round
import kotlin.math.log2
import kotlin.math.abs
import kotlin.math.pow

class PitchProcessing(callback: MyCallback?) {
    private var myCallback: MyCallback? = callback
    private val allNotes = arrayOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
    private val concertPitch = 440
    private val pitchMap = mapOf(82.41F to "E", 110.00F to "A", 146.83F to "D",
        196.00F to "G", 246.94F to "B", 329.63F to "E")

    fun tuneGuitar(pitchInHz: Float, probability: Float) {
        if (MainActivity().curTuning == "Standard Tuning") {
            val stringPitch = closestString(pitchInHz, TuningData().guitarStandard)
            if (probability > 0.91) {
                myCallback?.updateNote(pitchMap.getValue(stringPitch))
            }
        }
    }

    // determine which string current note is closest to
    private fun closestString(pitchInHz: Float, tuning: FloatArray) : Float {
        var difference = abs(tuning[0] - pitchInHz)
        var idx = 0
        for (c in 1..tuning.size) {
            val cdifference = abs(tuning[c] - pitchInHz)
            if (cdifference < difference) {
                idx = c
                difference = cdifference
            }
        }
        return tuning[idx]
    }

    /** detects closest note in A = concertPitch with equal temperament formula:
     * pitch(i) = pitch(0) * 2^(halfSteps/12)
     * therefore formula to derive interval between two pitches:
     * i = 12 * log2 * (pitch(i)/pitch(o))
     */
    fun closestNote(pitchInHz: Float, probability: Float) {
        (myCallback as MainActivity).noteSize()
        val i = (round(log2(pitchInHz / concertPitch) * 12)).toInt()
        // floorMod implementation to prevent ArrayIndexOutOfBoundException
        val closestNote = allNotes[(i % 12 + 12) % 12]
        if (probability > 0.91) {
            myCallback?.updateNote(closestNote)
        }
    }

    private fun closestPitch(pitchInHz: Float): Float {
        val i = (round(log2(pitchInHz / concertPitch) * 12)).toInt()
        val closestPitch = concertPitch * 2.toDouble().pow(i.toDouble() / 12)
        return closestPitch.toFloat()
    }

    // 6 string standard tuning guitar, includes 10 cents of leeway
    fun processPitch(pitchInHz: Float) {
        (myCallback as MainActivity).noteSize()
        when (pitchInHz) {
            in 82.31..82.51 -> myCallback?.updateNote("E")
            in 109.9..110.10 -> myCallback?.updateNote("A")
            in 146.7..146.9 -> myCallback?.updateNote("D")
            in 195.9..196.1 -> myCallback?.updateNote("G")
            in 246.8..247.0 -> myCallback?.updateNote("B")
            in 329.5..329.7 -> myCallback?.updateNote("E")
        }
    }
}