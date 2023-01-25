package com.bro.parking.extensions

import android.content.Context
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager

fun View.shake() {
    val shake = TranslateAnimation(0F, 10F, 0F, 0F)
    shake.duration = 500
    shake.interpolator = CycleInterpolator(5F)
    this.startAnimation(shake)
}

fun View.hideKeyboard(context : Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard(context : Context) {
    this.requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}