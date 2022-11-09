package com.hushbunny.app.ui.onboarding.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.hushbunny.app.core.HomeActivity
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentLoginBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.onboarding.forgetpassword.ForgetPasswordFragment
import com.hushbunny.app.ui.onboarding.signup.SignUpFragment
import com.hushbunny.app.ui.onboarding.viewmodel.LoginViewModel
import com.hushbunny.app.ui.repository.LoginResponseStatus
import com.hushbunny.app.ui.repository.OnBoardingRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.dialog.DialogUtils
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _fragmentLoginBinding: FragmentLoginBinding? = null
    private val fragmentLoginBinding: FragmentLoginBinding get() = _fragmentLoginBinding!!
    var password = 1
    @Inject
    lateinit var loginRepository: OnBoardingRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    private var type = APIConstants.EMAIL

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _fragmentLoginBinding = FragmentLoginBinding.bind(view)
        setObserver()
        initializeClickListener()
    }

    @SuppressLint("HardwareIds")
    private fun initializeClickListener() {
        fragmentLoginBinding.loginButton.setOnClickListener {
            loginViewModel.onLoginClick(
                type = type,
                callingCode = fragmentLoginBinding.mobileNumberContainer.countrySelection.selectedCountryCodeWithPlus,
                email = fragmentLoginBinding.emailContainer.emailInput.text.toString().trim(),
                phoneNumber = fragmentLoginBinding.mobileNumberContainer.mobileNumberInput.text.toString().trim(),
                password = fragmentLoginBinding.passwordContainer.passwordInput.text.toString().trim(),
                deviceID = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
            )
        }
        fragmentLoginBinding.loginTabContainer.emilButton.setOnClickListener {
            fragmentLoginBinding.emailContainer.container.visibility = View.VISIBLE
            fragmentLoginBinding.mobileNumberContainer.container.visibility = View.GONE
            type = APIConstants.EMAIL
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
            type = APIConstants.PHONE_NUMBER
            fragmentLoginBinding.loginTabContainer.emilButton.background = null
            fragmentLoginBinding.loginTabContainer.mobileNumberButton.background =
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.drawable_button_white
                )
            clearInput()
        }
        fragmentLoginBinding.forgetButton.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_view,
                    ForgetPasswordFragment.getInstance(),
                    ForgetPasswordFragment::class.simpleName.toString()
                )
            }
        }
        fragmentLoginBinding.passwordContainer.passwordImage.setOnClickListener {
            if (password == 1) {
                password = 2
                fragmentLoginBinding.passwordContainer.passwordInput.transformationMethod = null
                fragmentLoginBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_show_password)
            } else {
                password = 1
                fragmentLoginBinding.passwordContainer.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                fragmentLoginBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_hide_password)
            }

            fragmentLoginBinding.passwordContainer.passwordInput.setSelection(fragmentLoginBinding.passwordContainer.passwordInput.text.toString().length)
        }
        fragmentLoginBinding.loginCreateAccount.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_view,
                    SignUpFragment.getInstance(),
                    SignUpFragment::class.simpleName.toString()
                )
            }
        }

    }

    private fun setObserver() {
        loginViewModel.loginResponseObserver.observe(viewLifecycleOwner) {
            fragmentLoginBinding.progressIndicator.hideProgressbar()
            when (it) {
                is LoginResponseStatus.Error -> DialogUtils.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = it.message,
                    title = resourceProvider.getString(R.string.app_name)
                )
                is LoginResponseStatus.Success -> {
                    AppConstants.saveUserDetail(it.loginResponse)
                    navigateToHomePage()
                }
            }
        }
        loginViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    fragmentLoginBinding.progressIndicator.showProgressbar()
                    fragmentLoginBinding.root.hideKeyboard()
                }
                else -> {
                    fragmentLoginBinding.progressIndicator.hideProgressbar()
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
    }

    private fun navigateToHomePage() {
        PrefsManager.get().saveBooleanValues(AppConstants.IS_USER_LOGGED_IN, true)
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        activity?.finish()
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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