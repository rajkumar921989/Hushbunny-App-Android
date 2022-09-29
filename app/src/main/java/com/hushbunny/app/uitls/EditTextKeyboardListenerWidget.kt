package com.hushbunny.app.uitls

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class EditTextKeyboardListenerWidget : AppCompatEditText {
    private var onKeyboardDismissListener: (() -> Unit)? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            onKeyboardDismissListener?.invoke()
        }
        return super.onKeyPreIme(keyCode, event)
    }

    fun setOnKeyboardDismissListener(onKeyboardDismissListener: () -> Unit) {
        this.onKeyboardDismissListener = onKeyboardDismissListener
    }
}
