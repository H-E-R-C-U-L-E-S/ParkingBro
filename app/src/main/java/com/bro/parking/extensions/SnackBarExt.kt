package com.bro.parking.extensions

import android.view.Gravity
import android.widget.TextView
import com.bro.parking.R
import com.google.android.material.snackbar.Snackbar

fun Snackbar.setIcon(left : Int = 0,top : Int = 0,right : Int = 0,bottom : Int = 0){

    val text = this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    text.gravity = Gravity.CENTER_VERTICAL
    text.compoundDrawablePadding = 42

    text.setCompoundDrawablesWithIntrinsicBounds(
        left, top, right, bottom
    )

}