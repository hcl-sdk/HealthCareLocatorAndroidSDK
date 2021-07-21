package com.healthcarelocator.strategy

import android.widget.EditText

class NormalTextFillStrategy(editText: EditText?, suggestions: ArrayList<String>?) : TextFillStrategy(editText, suggestions) {
    override fun apply(fullText: String?) {
        if (nonSelectionText!!.length >= fullText!!.length) {
            suggested = false
            nonSelectionText = fullText
            return
        }

        if (suggested) {
            suggested = false
            return
        }

        for (suggestion in suggestions!!) {
            if (suggestion.startsWith(fullText)) {
                suggested = true
                editText!!.setText(suggestion)
                editText!!.setSelection(fullText.length, suggestion.length)
                nonSelectionText = fullText
                break
            }
        }
    }
}