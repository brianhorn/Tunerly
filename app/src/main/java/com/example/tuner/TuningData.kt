package com.example.tuner

enum class TuningData(vararg f: Float) {
    // Guitar Tunings
    GuitarStandard(82.41F, 110.00F, 146.83F, 196.00F, 246.94F, 329.63F);

    val frequencies: FloatArray = f
}