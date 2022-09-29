package com.hushbunny.app.ui.onboarding.createnewpassword

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.hushbunny.app.R
import com.hushbunny.app.databinding.CreateNewPasswordBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.onboarding.login.LoginFragment
import com.hushbunny.app.ui.onboarding.viewmodel.LoginViewModel
import com.hushbunny.app.ui.repository.OnBoardingRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.dialog.SuccessDialog
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CreateNewPasswordFragment : Fragment(R.layout.create_new_password) {
    private var _changePasswordBinding: CreateNewPasswordBinding? = null
    private val changePasswordBinding: CreateNewPasswordBinding get() = _changePasswordBinding!!
    var password = 1

    @Inject
    lateinit var changePasswordRepository: OnBoardingRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    private var forgotId = ""

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
        _changePasswordBinding = CreateNewPasswordBinding.bind(view)
        forgotId = requireArguments().getString(FORGET_ID, "")
        initializeClickListener()
        setObserver()
    }

    private fun initializeClickListener() {
        changePasswordBinding.passwordContainer.passwordImage.visibility = View.GONE
        changePasswordBinding.confirmPasswordContainer.passwordInput.hint =
            resourceProvider.getString(R.string.confirm_password)
        changePasswordBinding.resetPasswordButton.setOnClickListener {
            changePasswordViewModel.createPassword(
                password = changePasswordBinding.passwordContainer.passwordInput.text.toString()
                    .trim(),
                confirmPassword = changePasswordBinding.confirmPasswordContainer.passwordInput.text.toString()
                    .trim(),
                forgotId = forgotId
            )
        }

        changePasswordBinding.backImage.setOnClickListener {
            navigateToLoginPage()

        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            navigateToLoginPage()
        }
        changePasswordBinding.confirmPasswordContainer.passwordImage.setOnClickListener {
            if (password == 1) {
                password = 2
                changePasswordBinding.confirmPasswordContainer.passwordInput.transformationMethod = null
                changePasswordBinding.confirmPasswordContainer.passwordImage.setImageResource(R.drawable.ic_show_password)
            } else {
                password = 1
                changePasswordBinding.confirmPasswordContainer.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                changePasswordBinding.confirmPasswordContainer.passwordImage.setImageResource(R.drawable.ic_hide_password)
            }
            changePasswordBinding.confirmPasswordContainer.passwordInput.setSelection(changePasswordBinding.confirmPasswordContainer.passwordInput.text.toString().length)
        }

    }

    private fun navigateToLoginPage() {
        requireActivity().supportFragmentManager.commit {
            replace(
                R.id.fragment_container_view,
                LoginFragment.getInstance(),
                LoginFragment::class.simpleName.toString()
            )
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
        changePasswordViewModel.createPasswordObserver.observe(viewLifecycleOwner) {
            changePasswordBinding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    val dialog = SuccessDialog(requireContext())
                    dialog.show()
                    dialog.setMessage(resourceProvider.getString(R.string.password_changed_successfully))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        navigateToLoginPage()
                    }, 3000)
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


    companion object {
        private const val FORGET_ID = "forgotId"
        fun getInstance(forgotId: String): CreateNewPasswordFragment {
            val changePasswordFragment = CreateNewPasswordFragment()
            changePasswordFragment.arguments = Bundle().apply {
                putString(FORGET_ID, forgotId)
            }
            return changePasswordFragment
        }
    }
}