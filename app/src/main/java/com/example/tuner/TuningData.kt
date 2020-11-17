package com.example.tuner

enum class TuningData(vararg f: Float) {
    GuitarStandard(82.41F, 110.0F, 146.83F, 196.0F, 246.94F, 329.63F);
    val frequencies: FloatArray = f
}