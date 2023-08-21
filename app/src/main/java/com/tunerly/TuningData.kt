package com.tunerly

enum class TuningData(vararg f: Float) {
    // Guitar Tunings
    GuitarStandard(82.41F, 110.00F, 146.83F, 196.00F, 246.94F, 329.63F),
    GuitarSeven(61.74F, 82.41F, 110.00F, 146.83F, 196.00F, 246.94F, 329.63F),
    GuitarEight(46.25F, 61.74F, 82.41F, 110.00F, 146.83F, 196.00F, 246.94F, 329.63F),
    GuitarEFlat(77.78F, 103.83F, 138.59F, 185.00F, 233.08F, 311.13F),
    GuitarFullStepDown(73.42F, 98.00F, 130.81F, 174.61F, 220.00F, 293.66F),
    GuitarDropC(65.41F, 98.00F, 130.81F, 174.61F, 220.00F, 293.66F),
    GuitarDropD(73.42F, 110.00F, 146.83F, 196.00F, 246.94F, 329.63F),
    GuitarDropDb(69.30F, 103.83F, 138.59F, 185.00F, 233.08F, 311.13F),
    GuitarDoubleDropD(73.42F, 110.00F, 146.83F, 196.00F, 246.94F, 293.66F),
    GuitarDADGAD(73.42F, 110.00F, 146.83F, 196.00F, 220.00F, 329.63F),
    GuitarOpenD(73.42F, 110.00F, 146.83F, 185.00F, 220.00F, 329.63F),
    GuitarOpenA(82.41F, 110.00F, 138.59F, 164.81F, 220.00F, 329.63F),
    GuitarOpenE(82.41F, 123.47F, 164.81F, 207.65F, 246.94F, 329.63F),
    GuitarOpenG(77.78F, 98.00F, 146.83F, 196.00F, 246.94F, 293.66F),
    GuitarOpenB(61.74F, 92.50F, 123.47F, 185.00F, 246.94F, 311.13F),
    // Bass Tunings
    BassStandard4(41.20F, 55.00F, 73.42F, 98.00F),
    BassStandard5(30.87F, 41.20F, 55.00F, 73.42F, 98.00F),
    BassStandard6(30.87F, 41.20F, 55.00F, 73.42F, 98.00F, 130.81F),
    BassDropD(36.71F, 55.00F, 73.42F, 98.00F),
    BassDStandard(36.71F, 49.00F, 65.40F, 87.31F),
    BassDropC(32.70F, 49.00F, 65.40F, 87.31F),
    // Ukulele Tunings
    UkuleleStandard(392.00F, 261.63F, 329.63F, 440.00F),
    UkuleleTraditional(440.00F, 293.66F, 370.00F, 493.88F),
    UkuleleBaritone(146.83F, 196.00F, 246.94F, 329.63F),
    UkuleleBass(41.20F, 55.00F, 73.42F, 98.00F),

    CuatroC(196.00F, 261.63F, 329.63F, 220.00F),
    CuatroD(220.00F, 293.66F, 370.00F, 246.94F);
    val frequencies: FloatArray = f
}
