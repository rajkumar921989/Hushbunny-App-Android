package com.hushbunny.app.ui.onboarding.chagepassword

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentChangePasswordBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.repository.OnBoardingRepository
import com.hushbunny.app.ui.onboarding.viewmodel.LoginViewModel
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.dialog.SuccessDialog
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ChangePasswordFragment : Fragment(R.layout.fragment_change_password) {
    private var _changePasswordBinding: FragmentChangePasswordBinding? = null
    private val changePasswordBinding: FragmentChangePasswordBinding get() = _changePasswordBinding!!
    var oldPassword = 1
    var newPassword = 1
    var confirmPassword = 1

    @Inject
    lateinit var changePasswordRepository: OnBoardingRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    private val changePasswordViewModel: LoginViewModel by viewModelBuilderFragmentScope {
        LoginViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            onBoardingRepository = changePasswordRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _changePasswordBinding = FragmentChangePasswordBinding.bind(view)
        initView()
        initializeClickListener()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    private fun initView() {
        changePasswordBinding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.change_password)
        changePasswordBinding.oldPasswordContainer.passwordInput.hint = resourceProvider.getString(R.string.old_password)
        changePasswordBinding.passwordContainer.passwordInput.hint = resourceProvider.getString(R.string.new_password)
        changePasswordBinding.confirmPasswordContainer.passwordInput.hint = resourceProvider.getString(R.string.confirm_password)
    }

    private fun initializeClickListener() {
        changePasswordBinding.submitButton.setOnClickListener {
            changePasswordViewModel.changePassword(
                oldPassword = changePasswordBinding.oldPasswordContainer.passwordInput.text.toString().trim(),
                password = changePasswordBinding.passwordContainer.passwordInput.text.toString().trim(),
                confirmPassword = changePasswordBinding.confirmPasswordContainer.passwordInput.text.toString().trim()
            )
        }

        changePasswordBinding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()

        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        changePasswordBinding.oldPasswordContainer.passwordImage.setOnClickListener {
            if (oldPassword == 1) {
                oldPassword = 2
                changePasswordBinding.oldPasswordContainer.passwordInput.transformationMethod = null
                changePasswordBinding.oldPasswordContainer.passwordImage.setImageResource(R.drawable.ic_show_password)
            } else {
                oldPassword = 1
                changePasswordBinding.oldPasswordContainer.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                changePasswordBinding.oldPasswordContainer.passwordImage.setImageResource(R.drawable.ic_hide_password)
            }

            changePasswordBinding.oldPasswordContainer.passwordInput.setSelection(changePasswordBinding.oldPasswordContainer.passwordInput.text.toString().length)
        }
        changePasswordBinding.passwordContainer.passwordImage.setOnClickListener {
            if (newPassword == 1) {
                newPassword = 2
                changePasswordBinding.passwordContainer.passwordInput.transformationMethod = null
                changePasswordBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_show_password)
            } else {
                newPassword = 1
                changePasswordBinding.passwordContainer.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                changePasswordBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_hide_password)
            }

            changePasswordBinding.passwordContainer.passwordInput.setSelection(changePasswordBinding.passwordContainer.passwordInput.text.toString().length)
        }
        changePasswordBinding.confirmPasswordContainer.passwordImage.setOnClickListener {
            if (confirmPassword == 1) {
                confirmPassword = 2
                changePasswordBinding.confirmPasswordContainer.passwordInput.transformationMethod = null
                changePasswordBinding.confirmPasswordContainer.passwordImage.setImageResource(R.drawable.ic_show_password)
            } else {
                confirmPassword = 1
                changePasswordBinding.confirmPasswordContainer.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                changePasswordBinding.confirmPasswordContainer.passwordImage.setImageResource(R.drawable.ic_hide_password)
            }

            changePasswordBinding.confirmPasswordContainer.passwordInput.setSelection(changePasswordBinding.confirmPasswordContainer.passwordInput.text.toString().length)
        }

    }

    private fun setObserver() {
        changePasswordViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    changePasswordBinding.progressIndicator.showProgressbar()
                    changePasswordBinding.root.hideKeyboard()
                }
                else -> {
                    changePasswordBinding.progressIndicator.hideProgressbar()
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        changePasswordViewModel.changePasswordObserver.observe(viewLifecycleOwner) {
            changePasswordBinding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    val dialog = SuccessDialog(requireContext())
                    dialog.show()
                    dialog.setMessage(resourceProvider.getString(R.string.password_changed_successfully))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        findNavController().popBackStack()
                    }, 3000)
                }
                APIConstants.UNAUTHORIZED_CODE -> {
                    AppConstants.navigateToLoginPage(requireActivity())
                }
                else -> {
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it.message,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
    }

}