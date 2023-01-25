package com.bro.parking.text_watchers

import android.text.Editable
import android.text.TextWatcher


private var addMinus = false
private var removeMinus = false

class CarNumberTextWatcher() : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (count == 2 && before == 1 || count == 6 && before == 5 || start == 1 && count == 1 || start == 5 && count == 1) {
            addMinus = true
        } else if (count == 2 && before == 3 || count == 6 && before == 7 || start == 6 && before == 1 || start == 2 && before == 1) {
            removeMinus = true
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (s != null) {
            if (addMinus) {
                addMinus = false
                s.append('-')

            } else if (removeMinus) {
                removeMinus = false
                s.delete(s.length - 1, s.length)
            }
        }
    }

}