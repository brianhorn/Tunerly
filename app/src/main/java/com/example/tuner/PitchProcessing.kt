package com.example.tuner

class PitchProcessing(callback: MyCallback?) {
    private var myCallback: MyCallback? = callback

    // 6 string standard tuning guitar, includes 10 cents of leeway
    fun processPitch(pitchInHz: Float) {
        (myCallback as MainActivity).pitchDisplay()
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