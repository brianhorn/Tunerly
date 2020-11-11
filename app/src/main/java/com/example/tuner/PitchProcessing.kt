package com.example.tuner

import kotlin.math.*

class PitchProcessing(callback: MyCallback?) {
    private var myCallback: MyCallback? = callback
    private val allNotes = arrayOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
    private val concertPitch = 440

    /*
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
     */

    /** detects closest note in A = 440hz with equal temperament formula:
     * pitch(i) = pitch(0) * 2^(halfSteps/12)
     * therefore formula to derive interval between two pitches:
     * i = 12 * log2 * (pitch(i)/pitch(o))
     */
    fun closestNote(pitchInHz: Float, probability : Float) {
        (myCallback as MainActivity).noteSize()
        val i = (round(log2(pitchInHz / concertPitch) * 12)).toInt()
        // floorMod implementation to prevent ArrayIndexOutOfBoundException
        val closestNote = allNotes[(i % 12 + 12) % 12] /* + (4 + i.sign * ((9 + abs(i)).toDouble() / 12).toInt()).toString() */
        if (probability > 0.91) {
            myCallback?.updateNote(closestNote)
        }
    }
    /*
    private fun closestPitch(pitchInHz: Float): Float {
        val i = (round(log2(pitchInHz / concertPitch) * 12)).toInt()
        val closestPitch = concertPitch * 2.toDouble().pow(i.toDouble() / 12)
        return closestPitch.toFloat()
    }
     */
}