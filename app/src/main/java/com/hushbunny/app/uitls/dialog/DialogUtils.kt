package com.hushbunny.app.uitls.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.hushbunny.app.R
import com.hushbunny.app.databinding.DialogAccountSettingsBinding
import com.hushbunny.app.uitls.AppConstants

object DialogUtils {
    fun showErrorDialog(
        context: Context,
        buttonText: String?,
        title: String?,
        message: String?
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(buttonText) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    fun showDialogWithCallBack(
        context: Context,
        message: String,
        title: String,
        positiveButtonText: String,
        negativeButtonText: String,
        positiveButtonCallback: (() -> Unit)? = null,
        negativeButtonCallback: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context, R.style.DialogBackgroundStyle)
        dialog.setCancelable(false)
        val dialogBinding = DialogAccountSettingsBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)
        if (title.isEmpty()) {
            dialogBinding.titleText.visibility = View.GONE
        }
        if (negativeButtonText.isEmpty()) {
            dialogBinding.cancelButton.visibility = View.GONE
        }
        dialogBinding.titleText.text = title
        dialogBinding.descriptionText.text = message
        dialogBinding.okButton.text = positiveButtonText
        dialogBinding.cancelButton.text = negativeButtonText
        dialogBinding.cancelButton.setOnClickListener {
            dialog.cancel()
            negativeButtonCallback?.invoke()
        }
        dialogBinding.okButton.setOnClickListener {
            dialog.cancel()
            positiveButtonCallback?.invoke()
        }
        dialog.show()
    }

    fun sessionExpiredDialog(context: Activity) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.app_name))
        builder.setMessage(context.getString(R.string.session_expired_message))
        builder.setPositiveButton(context.getString(R.string.ok)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            AppConstants.navigateToLoginPage(context)
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}