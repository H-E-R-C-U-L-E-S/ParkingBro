package com.bro.parking.custom_components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LoadingFab(context: Context, attrs: AttributeSet) : FloatingActionButton(context, attrs) {

    private var mDefaultIcon: Drawable = this.drawable

    private val circularProgressDrawable = CircularProgressDrawable(context).apply {
        setStyle(CircularProgressDrawable.DEFAULT)
        setColorSchemeColors(Color.WHITE)
        start()
    }

    fun isLoading(loading: Boolean) {
        if (loading) {
            this.setImageDrawable(circularProgressDrawable)
            this.isEnabled = false
        } else {
            this.setImageDrawable(mDefaultIcon)
            this.isEnabled = true
        }
    }

}