package com.hushbunny.app.ui.onboarding.forgetpassword

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ForgetPasswordBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.onboarding.createnewpassword.CreateNewPasswordFragment
import com.hushbunny.app.ui.onboarding.login.LoginFragment
import com.hushbunny.app.ui.onboarding.viewmodel.LoginViewModel
import com.hushbunny.app.ui.repository.OnBoardingRepository
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.DialogUtils
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ForgetPasswordFragment : Fragment(R.layout.forget_password), TextWatcher, View.OnKeyListener,
    View.OnFocusChangeListener {
    private val code = CharArray(4)
    private var _forgetPasswordBinding: ForgetPasswordBinding? = null
    private val forgetPasswordBinding: ForgetPasswordBinding get() = _forgetPasswordBinding!!

    @Inject
    lateinit var forgetPasswordRepository: OnBoardingRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    private var type = APIConstants.EMAIL
    private var isOTPReceived = false
    private var focusView = 0

    private val forgetPasswordViewModel: LoginViewModel by viewModelBuilderFragmentScope {
        LoginViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            onBoardingRepository = forgetPasswordRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _forgetPasswordBinding = ForgetPasswordBinding.bind(view)
        initializeClickListener()
        setObserver()
        setListener()
    }

    private fun initializeClickListener() {
        forgetPasswordBinding.submitButton.setOnClickListener {
            if (isOTPReceived) {
                forgetPasswordViewModel.verifyForgetOTP(
                    otpID = forgetPasswordViewModel.forgetPasswordObserver.value?.data?.otpId.orEmpty(),
                    otpOne = forgetPasswordBinding.otpContainer.inputOne.text.toString().trim(),
                    otpTwo = forgetPasswordBinding.otpContainer.inputTwo.text.toString().trim(),
                    otpThree = forgetPasswordBinding.otpContainer.inputThree.text.toString().trim(),
                    otpFour = forgetPasswordBinding.otpContainer.inputFour.text.toString().trim()
                )
            } else {
                forgetPasswordViewModel.onForgetClick(
                    type = type,
                    callingCode = forgetPasswordBinding.mobileNumberContainer.countrySelection.selectedCountryCodeWithPlus,
                    email = forgetPasswordBinding.emailContainer.emailInput.text.toString().trim(),
                    phoneNumber = forgetPasswordBinding.mobileNumberContainer.mobileNumberInput.text.toString()
                        .trim()
                )
            }
        }
        forgetPasswordBinding.mobileNumberContainer.countrySelection.overrideClickListener {

        }
        forgetPasswordBinding.loginTabContainer.emilButton.setOnClickListener {
            forgetPasswordBinding.emailContainer.container.visibility = View.VISIBLE
            forgetPasswordBinding.forgetPasswordMessageText.text =
                resourceProvider.getString(R.string.enter_your_email_to_receive_password_recovery_code)
            forgetPasswordBinding.mobileNumberContainer.container.visibility = View.GONE
            type = APIConstants.EMAIL
            clearInput()
            forgetPasswordBinding.loginTabContainer.emilButton.background =
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.drawable_button_white
                )
            forgetPasswordBinding.loginTabContainer.mobileNumberButton.background = null
        }
        forgetPasswordBinding.loginTabContainer.mobileNumberButton.setOnClickListener {
            forgetPasswordBinding.forgetPasswordMessageText.text =
                resourceProvider.getString(R.string.enter_your_mobile_number_to_receive_password_recovery_code)
            forgetPasswordBinding.emailContainer.container.visibility = View.GONE
            forgetPasswordBinding.mobileNumberContainer.container.visibility = View.VISIBLE
            type = APIConstants.PHONE_NUMBER
            forgetPasswordBinding.loginTabContainer.emilButton.background = null
            forgetPasswordBinding.loginTabContainer.mobileNumberButton.background =
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.drawable_button_white
                )
            clearInput()
        }
        forgetPasswordBinding.backImage.setOnClickListener {
            if (isOTPReceived) {
                isOTPReceived = false
                handleBackClick()
            } else {
                navigateToLoginPage()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isOTPReceived) {
                isOTPReceived = false
                handleBackClick()
            } else {
                navigateToLoginPage()
            }
        }
        forgetPasswordBinding.resendCodeText.setOnClickListener {
            forgetPasswordViewModel.onResendOTP(
                forgetPasswordViewModel.forgetPasswordObserver.value?.data?.otpId.orEmpty(),
                type = APIConstants.FORGOT
            )
        }

    }

    private fun handleBackClick() {
        forgetPasswordBinding.loginTabContainer.container.visibility = View.VISIBLE
        forgetPasswordBinding.otpContainer.container.visibility = View.GONE
        forgetPasswordBinding.resendCodeText.visibility = View.GONE
        if (type == APIConstants.EMAIL) {
            forgetPasswordBinding.forgetPasswordMessageText.text =
                resourceProvider.getString(R.string.enter_your_email_to_receive_password_recovery_code)
            forgetPasswordBinding.emailContainer.container.visibility = View.VISIBLE
            forgetPasswordBinding.mobileNumberContainer.container.visibility = View.GONE
        } else {
            forgetPasswordBinding.forgetPasswordMessageText.text =
                resourceProvider.getString(R.string.enter_your_mobile_number_to_receive_password_recovery_code)
            forgetPasswordBinding.emailContainer.container.visibility = View.GONE
            forgetPasswordBinding.mobileNumberContainer.container.visibility = View.VISIBLE
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

    private fun navigateToCreatePasswordPage(forgetID: String) {
        requireActivity().supportFragmentManager.commit {
            replace(
                R.id.fragment_container_view,
                CreateNewPasswordFragment.getInstance(forgetID),
                CreateNewPasswordFragment::class.simpleName.toString()
            )
        }
    }

    private fun setOTPScreenVisibility() {
        focusView = 0
        if (type == APIConstants.EMAIL)
            forgetPasswordBinding.forgetPasswordMessageText.text =
                resourceProvider.getString(R.string.please_enter_the_password_recovery_code_sent_to_your_email)
        else forgetPasswordBinding.forgetPasswordMessageText.text =
            resourceProvider.getString(R.string.please_enter_the_password_recovery_code_sent_to_your_mobile)
        forgetPasswordBinding.otpContainer.inputOne.setText("")
        forgetPasswordBinding.otpContainer.inputTwo.setText("")
        forgetPasswordBinding.otpContainer.inputThree.setText("")
        forgetPasswordBinding.otpContainer.inputFour.setText("")
        forgetPasswordBinding.otpContainer.container.visibility = View.VISIBLE
        forgetPasswordBinding.loginTabContainer.container.visibility = View.GONE
        forgetPasswordBinding.emailContainer.container.visibility = View.GONE
        forgetPasswordBinding.mobileNumberContainer.container.visibility = View.GONE
        startTimeCounter()
    }

    private fun setObserver() {
        forgetPasswordViewModel.verifyForgetPasswordObserver.observe(viewLifecycleOwner) {
            forgetPasswordBinding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    navigateToCreatePasswordPage(forgetID = forgetPasswordViewModel.forgetPasswordObserver.value?.data?.otpId.orEmpty())
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
        forgetPasswordViewModel.forgetPasswordObserver.observe(viewLifecycleOwner) {
            forgetPasswordBinding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    isOTPReceived = true
                    setOTPScreenVisibility()
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
        forgetPasswordViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    forgetPasswordBinding.progressIndicator.showProgressbar()
                    forgetPasswordBinding.root.hideKeyboard()
                }
                else -> {
                    forgetPasswordBinding.progressIndicator.hideProgressbar()
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        forgetPasswordViewModel.resendOTPObserver.observe(viewLifecycleOwner) {
            forgetPasswordBinding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
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

    private fun clearInput() {
        forgetPasswordBinding.emailContainer.emailInput.setText("")
        forgetPasswordBinding.mobileNumberContainer.mobileNumberInput.setText("")
    }

    private fun setListener() {
        forgetPasswordBinding.otpContainer.inputOne.addTextChangedListener(this)
        forgetPasswordBinding.otpContainer.inputTwo.addTextChangedListener(this)
        forgetPasswordBinding.otpContainer.inputThree.addTextChangedListener(this)
        forgetPasswordBinding.otpContainer.inputFour.addTextChangedListener(this)

        forgetPasswordBinding.otpContainer.inputOne.setOnKeyListener(this)
        forgetPasswordBinding.otpContainer.inputTwo.setOnKeyListener(this)
        forgetPasswordBinding.otpContainer.inputThree.setOnKeyListener(this)
        forgetPasswordBinding.otpContainer.inputFour.setOnKeyListener(this)

        forgetPasswordBinding.otpContainer.inputOne.onFocusChangeListener = this
        forgetPasswordBinding.otpContainer.inputTwo.onFocusChangeListener = this
        forgetPasswordBinding.otpContainer.inputThree.onFocusChangeListener = this
        forgetPasswordBinding.otpContainer.inputFour.onFocusChangeListener = this
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(p0: Editable?) {
        when (focusView) {
            1 -> {
                if (!forgetPasswordBinding.otpContainer.inputOne.text.toString().isEmpty()) {
                    code[0] = forgetPasswordBinding.otpContainer.inputOne.text.toString().trim()[0]
                    forgetPasswordBinding.otpContainer.inputTwo.requestFocus()
                }
            }
            2 -> {
                if (!forgetPasswordBinding.otpContainer.inputTwo.text.toString().isEmpty()) {
                    code[1] = forgetPasswordBinding.otpContainer.inputTwo.text.toString()[0]
                    forgetPasswordBinding.otpContainer.inputThree.requestFocus()
                }
            }
            3 -> {
                if (!forgetPasswordBinding.otpContainer.inputThree.text.toString().isEmpty()) {
                    code[2] = forgetPasswordBinding.otpContainer.inputThree.text.toString()[0]
                    forgetPasswordBinding.otpContainer.inputFour.requestFocus()
                }
            }
            4 -> {
                if (!forgetPasswordBinding.otpContainer.inputFour.text.toString().isEmpty()) {
                    code[3] = forgetPasswordBinding.otpContainer.inputFour.text.toString()[0]
                }
            }
        }
    }

    override fun onKey(p0: View?, p1: Int, p2: KeyEvent?): Boolean {
        if (p2?.action == KeyEvent.ACTION_DOWN) {
            if (p1 == KeyEvent.KEYCODE_DEL) {
                when (p0?.id) {
                    R.id.input_two -> {
                        if (forgetPasswordBinding.otpContainer.inputTwo.text.toString().isEmpty())
                            forgetPasswordBinding.otpContainer.inputOne.requestFocus()
                    }
                    R.id.input_three -> {
                        if (forgetPasswordBinding.otpContainer.inputThree.text.toString().isEmpty())
                            forgetPasswordBinding.otpContainer.inputTwo.requestFocus()
                    }
                    R.id.input_four -> {
                        if (forgetPasswordBinding.otpContainer.inputFour.text.toString().isEmpty())
                            forgetPasswordBinding.otpContainer.inputThree.requestFocus()
                    }
                }
            }
        }
        return false
    }

    override fun onFocusChange(p0: View?, p1: Boolean) {
        when (p0?.id) {
            R.id.input_one -> {
                focusView = 1
            }
            R.id.input_two -> {
                focusView = 2
            }
            R.id.input_three -> {
                focusView = 3
            }
            R.id.input_four -> {
                focusView = 4
            }
        }
    }

    private fun startTimeCounter() {
        lifecycleScope.launch {
            val totalSeconds = TimeUnit.MINUTES.toSeconds(1)
            val tickSeconds = 1
            forgetPasswordBinding.resendCodeText.visibility = View.VISIBLE
            for (second in totalSeconds downTo tickSeconds) {
                val time = String.format("%02d sec", second)
                val text = resourceProvider.getString(R.string.resend_code_in, time)
                val timerText = SpannableStringBuilder(text)
                val timerIndex = 14
                timerText.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.button_color_pink
                        )
                    ),
                    timerIndex,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                timerText.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.text_color_gray
                        )
                    ), 0, timerIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                forgetPasswordBinding.resendCodeText.text = timerText
                forgetPasswordBinding.resendCodeText.isClickable = false
                delay(1000)
            }
            forgetPasswordBinding.resendCodeText.isClickable = true
            forgetPasswordBinding.resendCodeText.text =
                resourceProvider.getString(R.string.resend_code)
            forgetPasswordBinding.resendCodeText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.button_color_pink
                )
            )
        }

    }

    companion object {
        fun getInstance(): ForgetPasswordFragment {
            return ForgetPasswordFragment()
        }
    }
}