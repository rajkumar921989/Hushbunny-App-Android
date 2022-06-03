package com.hushbunny.app.uitls

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide

class AppConstants {
    companion object {
        const val ZERO = 0
        const val API_RESPONSE_200 = 200
        const val ANDROID = "ANDROID"
        const val EMAIL = "EMAIL"
        const val SUCCESS = "SUCCESS"
        const val PHONE_NUMBER = "PHONENUMBER"
        const val ACCEPT_LANGUAGE = "accept-language"
        const val AUTHORIZATION = "authorization"
        const val ENGLISH = "en"
        const val FORGOT = "FORGOT"
        const val REGISTRATION = "REGISTRATION"
        const val MALE = "Male"
        const val FEMALE = "Female"
        const val IS_USER_LOGGED_IN = "IS_USER_LOGGED_IN"
        const val USER_DETAIL = "USER_DETAIL"
        const val USER_NAME = "USER_NAME"
        const val USER_TOKEN = "USER_TOKEN"
        const val USER_PHONE_NUMBER = "USER_PHONE_NUMBER"
        const val USER_EMAIL = "USER_EMAIL"
        const val USER_ID = "USER_ID"
        const val USER_CALLING_CODE = "USER_CALLING_CODE"
        const val KIDS_LIST = "KIDS_LIST"
        const val ADD_KID = "ADD_KID"

        fun isValidEmail(email: String): Boolean {
            return (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
        }

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

        fun View.hideKeyboard() {
            (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.apply {
                hideSoftInputFromWindow(windowToken, 0)
            }
        }

        fun String.isHavingUpperCaseLetter(): Boolean {
            return this.contains("[A-Z]".toRegex())
        }

        fun String.isHavingLowerCaseLetter(): Boolean {
            return this.contains("[a-z]".toRegex())
        }

        fun String.isHavingNumber(): Boolean {
            return this.contains("[0-9]".toRegex())
        }

        fun String.isHavingSpecialCharacter(): Boolean {
            return this.contains("[~!@#$%^&*()_+{}\\[\\]:;,.<>/?-]".toRegex())
        }

        fun ImageView.loadImageFromURL(imageUrl: String) {
            Glide.with(this)
                .load(imageUrl)
                .into(this)
        }

        fun ImageView.loadLocalImage(image: Drawable?) {
            Glide.with(this)
                .load(image)
                .into(this)
        }
    }
}