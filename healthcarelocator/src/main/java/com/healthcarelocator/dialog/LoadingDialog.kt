package com.healthcarelocator.dialog

import android.content.Context
import android.view.View
import com.healthcarelocator.R

class LoadingDialog(context: Context) : AbsDialog(context) {
    override val layoutId: Int = R.layout.layout_loading
    override val touchOutsideToCancel: Boolean = true

    override fun initView(view: View) {

    }
}