package com.healthcarelocator.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window

abstract class AbsDialog {
    abstract val layoutId: Int
    abstract val touchOutsideToCancel: Boolean
    protected abstract fun initView(view: View)
    private var dialog: Dialog? = null

    constructor(context: Context) {
        dialog = Dialog(context)
    }

    fun initLayout() {
        dialog?.let {
            it.requestWindowFeature(Window.FEATURE_NO_TITLE)
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
            if (layoutId == 0)
                throw NullPointerException("LayoutId must be non null")
            it.setContentView(layoutId)
            it.setCanceledOnTouchOutside(touchOutsideToCancel)
        }
    }

    open fun show() {
        if (dialog?.isShowing == true) {
            dismiss()
        }
        dialog?.show()
    }

    open fun dismiss() {
        dialog?.dismiss()
    }
}