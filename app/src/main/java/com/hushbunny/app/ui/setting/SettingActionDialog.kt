package com.hushbunny.app.ui.setting

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import com.hushbunny.app.R
import com.hushbunny.app.databinding.DialogAccountSettingsBinding

class SettingActionDialog(context: Context, val type: String, private val positiveButtonClick: ((String) -> Unit)? = null) : Dialog(context) {
    private var binding: DialogAccountSettingsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAccountSettingsBinding.inflate(layoutInflater)
        binding?.root?.let { setContentView(it) }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        initView()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (context.resources.displayMetrics.widthPixels * 0.80).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun initView() {
        when (type) {
            context.getString(R.string.logout) -> {
                binding?.titleText?.text = context.getString(R.string.logout)
                binding?.descriptionText?.text = context.getString(R.string.logout_message)
            }
            context.getString(R.string.delete_account) -> {
                binding?.titleText?.text = context.getString(R.string.delete_account)
                binding?.descriptionText?.text = context.getString(R.string.delete_account_message)
            }
            context.getString(R.string.deactivate_account) -> {
                binding?.titleText?.text = context.getString(R.string.deactivate_account)
                binding?.descriptionText?.text = context.getString(R.string.deactivate_account_message)
            }
            context.getString(R.string.delete_moment) -> {
                binding?.titleText?.text = context.getString(R.string.delete_moment)
                binding?.descriptionText?.text = context.getString(R.string.delete_moment_message)
            }
        }
        binding?.okButton?.setOnClickListener {
            dismiss()
            positiveButtonClick?.invoke(type)
        }
        binding?.cancelButton?.setOnClickListener {
            dismiss()
        }
    }


}