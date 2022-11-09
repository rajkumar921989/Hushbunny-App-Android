package com.hushbunny.app.uitls.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.R
import com.hushbunny.app.databinding.PopupUserImageBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL
import com.hushbunny.app.uitls.SwipeGestureListener
import com.hushbunny.app.uitls.SwipeGestureInterface
import javax.inject.Inject

class UserPictureDialog : DialogFragment(R.layout.popup_user_image) {
    private var _binding: PopupUserImageBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: UserPictureDialogArgs by navArgs()

    @Inject
    lateinit var resourceProvider: ResourceProvider


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun getTheme(): Int {
        return R.style.AppDialogStyle
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState).apply {
            val window = this.window;
            window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            window?.setGravity(Gravity.CENTER)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = PopupUserImageBinding.bind(view)
        initClickListener()
        val swipeGesture = SwipeGestureListener(object : SwipeGestureInterface {
            override fun onRightToLeftSwipe(v: View?) {}
            override fun onLeftToRightSwipe(v: View?) {}
            override fun onTopToBottomSwipe(v: View?) {
                this@UserPictureDialog.dismiss()
            }
            override fun onBottomToTopSwipe(v: View?) {}
        })
        binding.productHeaderImage.run {
            loadImageFromURL(navigationArgs.imagePath, isLocal = navigationArgs.isLocal)
            setOnTouchListener(swipeGesture)
        }
    }


    private fun initClickListener() {
        binding.actionCloseImage.setOnClickListener {
            findNavController().popBackStack()
        }
    }


}