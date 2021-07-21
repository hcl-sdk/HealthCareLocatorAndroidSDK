package com.healthcarelocator.strategy

import android.widget.EditText

abstract class TextFillStrategy(var editText: EditText?, var suggestions: ArrayList<String>?) {

    var suggested = false
    var nonSelectionText: String? = null

    init {
        nonSelectionText = ""
    }

    abstract fun apply(fullText: String?)
}
