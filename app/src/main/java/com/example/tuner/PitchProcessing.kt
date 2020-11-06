package com.example.tuner

class PitchProcessing(callback: MyCallback?) {
    private var myCallback: MyCallback? = callback
    fun processPitch(pitchInHz: Float) {
        (myCallback as MainActivity).pitchDisplay()
        when (pitchInHz) {
            in 82.31..82.51 -> myCallback?.updateMyText("E")
            in 109.9..110.10 -> myCallback?.updateMyText("A")
            in 146.7..146.9 -> myCallback?.updateMyText("D")
            in 195.9..196.1 -> myCallback?.updateMyText("G")
            in 246.8..247.0 -> myCallback?.updateMyText("H")
            in 329.5..329.7 -> myCallback?.updateMyText("E")
        }
    }
}