package com.hushbunny.app.ui.onboarding.login

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentLoginBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.onboarding.serviceandrepository.LoginResponseStatus
import com.hushbunny.app.ui.onboarding.serviceandrepository.OnBoardingRepository
import com.hushbunny.app.ui.onboarding.viewmodel.LoginViewModel
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.AppConstants.Companion.hideKeyboard
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class LoginFragment : Fragment() {
    private var _fragmentLoginBinding: FragmentLoginBinding? = null
    private val fragmentLoginBinding: FragmentLoginBinding get() = _fragmentLoginBinding!!

    @Inject
    lateinit var loginRepository: OnBoardingRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    private var type = AppConstants.EMAIL

    private val loginViewModel: LoginViewModel by viewModelBuilderFragmentScope {
        LoginViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            onBoardingRepository = loginRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)
        initializeClickListener()
        setObserver()
        return fragmentLoginBinding.root
    }

    private fun initializeClickListener() {
        fragmentLoginBinding.loginButton.setOnClickListener {
            loginViewModel.onLoginClick(
                type = type,
                callingCode = fragmentLoginBinding.mobileNumberContainer.countrySelection.selectedCountryCodeWithPlus,
                email = fragmentLoginBinding.emailContainer.emailInput.text.toString().trim(),
                phoneNumber = fragmentLoginBinding.mobileNumberContainer.mobileNumberInput.text.toString()
                    .trim(),
                password = fragmentLoginBinding.passwordContainer.passwordInput.text.toString()
                    .trim(),
            )
        }
        fragmentLoginBinding.loginTabContainer.emilButton.setOnClickListener {
            fragmentLoginBinding.emailContainer.container.visibility = View.VISIBLE
            fragmentLoginBinding.mobileNumberContainer.container.visibility = View.GONE
            type = AppConstants.EMAIL
            clearInput()
            fragmentLoginBinding.loginTabContainer.emilButton.background =
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.drawable_button_white
                )
            fragmentLoginBinding.loginTabContainer.mobileNumberButton.background = null
        }
        fragmentLoginBinding.loginTabContainer.mobileNumberButton.setOnClickListener {
            fragmentLoginBinding.emailContainer.container.visibility = View.GONE
            fragmentLoginBinding.mobileNumberContainer.container.visibility = View.VISIBLE
            type = AppConstants.PHONE_NUMBER
            fragmentLoginBinding.loginTabContainer.emilButton.background = null
            fragmentLoginBinding.loginTabContainer.mobileNumberButton.background =
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.drawable_button_white
                )
            clearInput()
        }

        fragmentLoginBinding.passwordContainer.passwordImage.setOnClickListener {
            if (fragmentLoginBinding.passwordContainer.passwordInput.inputType == 1) {
                fragmentLoginBinding.passwordContainer.passwordInput.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                fragmentLoginBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_show_password)
            } else {
                fragmentLoginBinding.passwordContainer.passwordInput.inputType =
                    InputType.TYPE_CLASS_TEXT
                fragmentLoginBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_password)
            }

            fragmentLoginBinding.passwordContainer.passwordInput.setSelection(fragmentLoginBinding.passwordContainer.passwordInput.text.toString().length)
        }


    }

    private fun setObserver() {
        loginViewModel.loginResponseObserver.observe(viewLifecycleOwner) {
            fragmentLoginBinding.progressIndicator.hideProgressbar()
            when (it) {
                is LoginResponseStatus.Error -> AppConstants.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = it.message,
                    title = resourceProvider.getString(R.string.app_name)
                )
                is LoginResponseStatus.Success -> {
                    AppConstants.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it.message,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        loginViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                AppConstants.SUCCESS -> {
                    fragmentLoginBinding.progressIndicator.showProgressbar()
                    fragmentLoginBinding.root.hideKeyboard()
                }
                else -> {
                    fragmentLoginBinding.progressIndicator.hideProgressbar()
                    AppConstants.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
    }


    private fun clearInput() {
        fragmentLoginBinding.emailContainer.emailInput.setText("")
        fragmentLoginBinding.mobileNumberContainer.mobileNumberInput.setText("")
        fragmentLoginBinding.passwordContainer.passwordInput.setText("")
    }

    companion object {
        fun getInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}