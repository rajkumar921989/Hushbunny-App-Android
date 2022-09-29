package com.hushbunny.app.uitls.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.hushbunny.app.databinding.DialogSuccessBinding

class SuccessDialog(context: Context) : Dialog(context) {
    private var dialogSuccessBinding: DialogSuccessBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogSuccessBinding = DialogSuccessBinding.inflate(layoutInflater)
        dialogSuccessBinding?.root?.let { setContentView(it) }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

    }

    fun setMessage(message: String, isRequiredWelcomeMessage: Boolean = false) {
        dialogSuccessBinding?.messageText?.text = message
        if (isRequiredWelcomeMessage) dialogSuccessBinding?.welcomeMessageText?.visibility =
            View.VISIBLE else dialogSuccessBinding?.welcomeMessageText?.visibility = View.GONE
    }

}