package com.hushbunny.app.uitls

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.hushbunny.app.R
import com.hushbunny.app.databinding.SpinnerProgressBinding

class ProgressbarWidget : ConstraintLayout {
    private lateinit var spinnerProgressBinding: SpinnerProgressBinding

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        spinnerProgressBinding = SpinnerProgressBinding.inflate(LayoutInflater.from(context), this)
        spinnerProgressBinding.root.run {
            setBackgroundColor(ContextCompat.getColor(context, R.color.progress_background_color))
            setOnClickListener {
                // Prevent the view clicks while progress was shown
            }
        }
        hideProgressbar()
    }

    /**
     * Create and show progress dialog
     */
    fun showProgressbar() {
        spinnerProgressBinding.root.visibility = View.VISIBLE
    }

    /**
     * Dismiss progress dialog
     **/
    fun hideProgressbar() {
        spinnerProgressBinding.root.visibility = View.GONE

    }

}
