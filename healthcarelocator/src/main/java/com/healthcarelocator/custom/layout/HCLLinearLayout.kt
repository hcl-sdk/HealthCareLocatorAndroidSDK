package com.healthcarelocator.custom.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.healthcarelocator.R
import com.healthcarelocator.extensions.getColor
import com.healthcarelocator.state.HealthCareLocatorSDK

class HCLLinearLayout : LinearLayout {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        init(attr)
    }

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context, attr, defStyle) {
        init(attr)
    }

    private fun init(attributeSet: AttributeSet? = null) {
        var usingDrawable = false
        if (attributeSet != null) {
            var typeArray: TypedArray =
                    context.obtainStyledAttributes(attributeSet, R.styleable.HCLLayout)
            usingDrawable = typeArray.getBoolean(R.styleable.HCLLayout_useDrawable, false)
            typeArray.recycle()
        }
        val config = HealthCareLocatorSDK.getInstance().getConfiguration()
        val darkMode = config.darkMode
        if (usingDrawable) {
            background = ContextCompat.getDrawable(context, if (darkMode) R.drawable.bg_black_border_corner else R.drawable.bg_white_border_corner)
        } else {
            setBackgroundColor(if (darkMode) config.darkModeColor.getColor() else Color.WHITE)
        }
    }
}