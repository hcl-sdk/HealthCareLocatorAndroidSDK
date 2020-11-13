package com.ekino.onekeysdk.extensions

import android.content.Context

fun Context.pxFromDp(id: Int): Float {
    return resources.getDimension(id) * resources.displayMetrics.density
}