package com.example.tuner

interface MyCallback {
    fun updateNote(note: String?)
    fun updateTest(test: String?)
    fun updateTest2(test: String?)
    fun colorUp()
    fun colorDown()
    fun colorTuned()
}