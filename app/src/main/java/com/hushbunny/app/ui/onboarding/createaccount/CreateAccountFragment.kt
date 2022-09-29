package com.hushbunny.app.ui.onboarding.createaccount

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentCreateAccountBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.onboarding.introscreen.IntroScreenFragment
import com.hushbunny.app.ui.onboarding.login.LoginFragment
import com.hushbunny.app.ui.onboarding.model.SignInUserModel
import com.hushbunny.app.ui.onboarding.viewmodel.LoginViewModel
import com.hushbunny.app.ui.repository.OnBoardingRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.dialog.SuccessDialog
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


class CreateAccountFragment : Fragment(R.layout.fragment_create_account), TextWatcher, View.OnKeyListener,
    View.OnFocusChangeListener {
    private val code = CharArray(4)
    private var _fragmentCreateAccountBinding: FragmentCreateAccountBinding? = null
    private val fragmentCreateAccountBinding: FragmentCreateAccountBinding get() = _fragmentCreateAccountBinding!!

    @Inject
    lateinit var createAccountRepository: OnBoardingRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    var password = 1
    private var type = APIConstants.EMAIL
    private var isOTPReceived = false
    private var focusView = 0
    private var userDetail: SignInUserModel? = null

    private val createAccountViewModel: LoginViewModel by viewModelBuilderFragmentScope {
        LoginViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            onBoardingRepository = createAccountRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _fragmentCreateAccountBinding = FragmentCreateAccountBinding.bind(view)
        userDetail = requireArguments().getSerializable(USER_DETAIL) as SignInUserModel?
        initializeClickListener()
        setObserver()
        setListener()
        setAlreadyHaveAccountText()
        setTermsAndConditionText()
    }

    private fun setAlreadyHaveAccountText() {
        val signUpText =
            SpannableStringBuilder(resourceProvider.getString(R.string.already_have_an_account))
        signUpText.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.button_color_pink
                )
            ),
            signUpText.indexOf(resourceProvider.getString(R.string.sign_in), ignoreCase = true),
            signUpText.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        signUpText.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_color_gray
                )
            ),
            0,
            signUpText.length - signUpText.indexOf(
                resourceProvider.getString(R.string.sign_in),
                ignoreCase = true
            ),
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        fragmentCreateAccountBinding.alreadyHaveAnAccountText.text = signUpText
    }

    private fun setTermsAndConditionText() {
        val termsConditionText = SpannableStringBuilder(resourceProvider.getString(R.string.sign_up_terms_condition))
        termsConditionText.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.button_color_pink)),
            termsConditionText.indexOf(resourceProvider.getString(R.string.terms_conditions), ignoreCase = true),
            termsConditionText.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        termsConditionText.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.text_color_gray)),
            0,
            termsConditionText.length - termsConditionText.indexOf(
                resourceProvider.getString(R.string.terms_conditions),
                ignoreCase = true
            ),
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        fragmentCreateAccountBinding.termsConditionText.text = termsConditionText
    }

    private fun initializeClickListener() {
        Handler(Looper.getMainLooper()).postDelayed({
            fragmentCreateAccountBinding.tabContainer.emilButton.performClick()
        }, 1)

        fragmentCreateAccountBinding.termsConditionText.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resourceProvider.getString(R.string.env_terms_condition_url)))
            startActivity(intent)
        }
        fragmentCreateAccountBinding.createAccountButton.setOnClickListener {
            if (isOTPReceived) {
                createAccountViewModel.verifyOTPForNewUser(
                    otpID = createAccountViewModel.newAccountOTPObserver.value?.data?.otpId.orEmpty(),
                    otpOne = fragmentCreateAccountBinding.otpContainer.inputOne.text.toString().trim(),
                    otpTwo = fragmentCreateAccountBinding.otpContainer.inputTwo.text.toString().trim(),
                    otpThree = fragmentCreateAccountBinding.otpContainer.inputThree.text.toString().trim(),
                    otpFour = fragmentCreateAccountBinding.otpContainer.inputFour.text.toString().trim()
                )
            } else {
                createAccountViewModel.getOTPForNewAccount(
                    userModel = userDetail,
                    password = fragmentCreateAccountBinding.passwordContainer.passwordInput.text.toString().trim(),
                    email = fragmentCreateAccountBinding.emailContainer.emailInput.text.toString().trim(),
                    type = type, callingCode = fragmentCreateAccountBinding.mobileNumberContainer.countrySelection.selectedCountryCodeWithPlus,
                    phoneNumber = fragmentCreateAccountBinding.mobileNumberContainer.mobileNumberInput.text.toString().trim()
                )
            }
        }
        fragmentCreateAccountBinding.tabContainer.emilButton.setOnClickListener {
            fragmentCreateAccountBinding.emailContainer.container.visibility = View.VISIBLE
            fragmentCreateAccountBinding.mobileNumberContainer.container.visibility = View.GONE
            type = APIConstants.EMAIL
            clearInput()
            fragmentCreateAccountBinding.tabContainer.emilButton.background =
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.drawable_button_white
                )
            fragmentCreateAccountBinding.tabContainer.mobileNumberButton.background = null
        }
        fragmentCreateAccountBinding.tabContainer.mobileNumberButton.setOnClickListener {
            fragmentCreateAccountBinding.emailContainer.container.visibility = View.GONE
            fragmentCreateAccountBinding.mobileNumberContainer.container.visibility = View.VISIBLE
            type = APIConstants.PHONE_NUMBER
            fragmentCreateAccountBinding.tabContainer.emilButton.background = null
            fragmentCreateAccountBinding.tabContainer.mobileNumberButton.background =
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.drawable_button_white
                )
            clearInput()
        }
        fragmentCreateAccountBinding.backImage.setOnClickListener {
            if (isOTPReceived) {
                isOTPReceived = false
                handleBackClick()
            } else {
                requireActivity().supportFragmentManager.popBackStackImmediate()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isOTPReceived) {
                isOTPReceived = false
                handleBackClick()
            } else {
                requireActivity().supportFragmentManager.popBackStackImmediate()
            }
        }
        fragmentCreateAccountBinding.resendCodeText.setOnClickListener {
            createAccountViewModel.onResendOTP(
                createAccountViewModel.forgetPasswordObserver.value?.data?.otpId.orEmpty(),
                type = APIConstants.REGISTRATION
            )
        }
        fragmentCreateAccountBinding.alreadyHaveAnAccountText.setOnClickListener {
            navigateToLoginPage()
        }
        fragmentCreateAccountBinding.passwordContainer.passwordImage.setOnClickListener {
            if (password == 1) {
                password = 2
                fragmentCreateAccountBinding.passwordContainer.passwordInput.transformationMethod = null
                fragmentCreateAccountBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_show_password)
            } else {
                password = 1
                fragmentCreateAccountBinding.passwordContainer.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                fragmentCreateAccountBinding.passwordContainer.passwordImage.setImageResource(R.drawable.ic_hide_password)
            }
            fragmentCreateAccountBinding.passwordContainer.passwordInput.setSelection(fragmentCreateAccountBinding.passwordContainer.passwordInput.text.toString().length)
        }

    }

    private fun handleBackClick() {
        timer.cancel()
        fragmentCreateAccountBinding.emailMobileGroup.visibility = View.VISIBLE
        fragmentCreateAccountBinding.otpGroup.visibility = View.GONE
        fragmentCreateAccountBinding.resendCodeText.visibility = View.GONE
        if (type == APIConstants.EMAIL) {
            fragmentCreateAccountBinding.emailContainer.container.visibility = View.VISIBLE
            fragmentCreateAccountBinding.mobileNumberContainer.container.visibility = View.GONE
        } else {
            fragmentCreateAccountBinding.emailContainer.container.visibility = View.GONE
            fragmentCreateAccountBinding.mobileNumberContainer.container.visibility = View.VISIBLE
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

    private fun navigateIntroScreenPage() {
        PrefsManager.get().saveBooleanValues(AppConstants.IS_USER_LOGGED_IN, true)
        requireActivity().supportFragmentManager.commit {
            replace(
                R.id.fragment_container_view,
                IntroScreenFragment.getInstance(true),
                IntroScreenFragment::class.simpleName.toString()
            )
        }
    }

    private fun setOTPScreenVisibility() {
        focusView = 0
        if (type == APIConstants.EMAIL)
            fragmentCreateAccountBinding.verifyMessageText.text =
                resourceProvider.getString(R.string.please_enter_the_four_digits_code_sent_on_your_email)
        else fragmentCreateAccountBinding.verifyMessageText.text =
            resourceProvider.getString(R.string.please_enter_the_four_digits_code_sent_on_your_mobile)
        fragmentCreateAccountBinding.otpContainer.inputOne.setText("")
        fragmentCreateAccountBinding.otpContainer.inputTwo.setText("")
        fragmentCreateAccountBinding.otpContainer.inputThree.setText("")
        fragmentCreateAccountBinding.otpContainer.inputFour.setText("")
        fragmentCreateAccountBinding.otpGroup.visibility = View.VISIBLE
        fragmentCreateAccountBinding.emailMobileGroup.visibility = View.GONE
        timer.cancel()
        timer.start()
    }

    private fun setObserver() {
        createAccountViewModel.newAccountOTPObserver.observe(viewLifecycleOwner) {
            fragmentCreateAccountBinding.progressIndicator.hideProgressbar()
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
        createAccountViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    fragmentCreateAccountBinding.progressIndicator.showProgressbar()
                    fragmentCreateAccountBinding.root.hideKeyboard()
                }
                else -> {
                    fragmentCreateAccountBinding.progressIndicator.hideProgressbar()
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        createAccountViewModel.resendOTPObserver.observe(viewLifecycleOwner) {
            fragmentCreateAccountBinding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    timer.cancel()
                    timer.start()
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
        createAccountViewModel.verifyNewUserOTPObserver.observe(viewLifecycleOwner) {
            fragmentCreateAccountBinding.progressIndicator.hideProgressbar()
            timer.cancel()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    val dialog = SuccessDialog(requireContext())
                    dialog.show()
                    dialog.setMessage(
                        if (type == APIConstants.EMAIL) resourceProvider.getString(R.string.email_verified) else resourceProvider.getString(
                            R.string.phone_number_verified
                        ), isRequiredWelcomeMessage = true
                    )
                    AppConstants.saveUserDetail(it.data)
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        navigateIntroScreenPage()
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

    private fun clearInput() {
        fragmentCreateAccountBinding.emailContainer.emailInput.setText("")
        fragmentCreateAccountBinding.mobileNumberContainer.mobileNumberInput.setText("")
        fragmentCreateAccountBinding.passwordContainer.passwordInput.setText("")
    }

    private fun setListener() {
        fragmentCreateAccountBinding.otpContainer.inputOne.addTextChangedListener(this)
        fragmentCreateAccountBinding.otpContainer.inputTwo.addTextChangedListener(this)
        fragmentCreateAccountBinding.otpContainer.inputThree.addTextChangedListener(this)
        fragmentCreateAccountBinding.otpContainer.inputFour.addTextChangedListener(this)

        fragmentCreateAccountBinding.otpContainer.inputOne.setOnKeyListener(this)
        fragmentCreateAccountBinding.otpContainer.inputTwo.setOnKeyListener(this)
        fragmentCreateAccountBinding.otpContainer.inputThree.setOnKeyListener(this)
        fragmentCreateAccountBinding.otpContainer.inputFour.setOnKeyListener(this)

        fragmentCreateAccountBinding.otpContainer.inputOne.onFocusChangeListener = this
        fragmentCreateAccountBinding.otpContainer.inputTwo.onFocusChangeListener = this
        fragmentCreateAccountBinding.otpContainer.inputThree.onFocusChangeListener = this
        fragmentCreateAccountBinding.otpContainer.inputFour.onFocusChangeListener = this
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(p0: Editable?) {
        when (focusView) {
            1 -> {
                if (!fragmentCreateAccountBinding.otpContainer.inputOne.text.toString().isEmpty()) {
                    code[0] = fragmentCreateAccountBinding.otpContainer.inputOne.text.toString().trim()[0]
                    fragmentCreateAccountBinding.otpContainer.inputTwo.requestFocus()
                }
            }
            2 -> {
                if (!fragmentCreateAccountBinding.otpContainer.inputTwo.text.toString().isEmpty()) {
                    code[1] = fragmentCreateAccountBinding.otpContainer.inputTwo.text.toString()[0]
                    fragmentCreateAccountBinding.otpContainer.inputThree.requestFocus()
                }
            }
            3 -> {
                if (!fragmentCreateAccountBinding.otpContainer.inputThree.text.toString().isEmpty()) {
                    code[2] = fragmentCreateAccountBinding.otpContainer.inputThree.text.toString()[0]
                    fragmentCreateAccountBinding.otpContainer.inputFour.requestFocus()
                }
            }
            4 -> {
                if (!fragmentCreateAccountBinding.otpContainer.inputFour.text.toString().isEmpty()) {
                    code[3] = fragmentCreateAccountBinding.otpContainer.inputFour.text.toString()[0]
                }
            }
        }
    }

    override fun onKey(p0: View?, p1: Int, p2: KeyEvent?): Boolean {
        if (p2?.action == KeyEvent.ACTION_DOWN) {
            if (p1 == KeyEvent.KEYCODE_DEL) {
                when (p0?.id) {
                    R.id.input_two -> {
                        if (fragmentCreateAccountBinding.otpContainer.inputTwo.text.toString().isEmpty())
                            fragmentCreateAccountBinding.otpContainer.inputOne.requestFocus()
                    }
                    R.id.input_three -> {
                        if (fragmentCreateAccountBinding.otpContainer.inputThree.text.toString().isEmpty())
                            fragmentCreateAccountBinding.otpContainer.inputTwo.requestFocus()
                    }
                    R.id.input_four -> {
                        if (fragmentCreateAccountBinding.otpContainer.inputFour.text.toString().isEmpty())
                            fragmentCreateAccountBinding.otpContainer.inputThree.requestFocus()
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

    private val timer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            fragmentCreateAccountBinding.resendCodeText.visibility = View.VISIBLE
            val text = resourceProvider.getString(R.string.resend_code_in, millisUntilFinished / 1000)
            val timerText = SpannableStringBuilder(text)
            val timerIndex = 14
            timerText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.button_color_pink)),
                timerIndex,
                text.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            timerText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.text_color_gray)),
                0,
                timerIndex,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            fragmentCreateAccountBinding.resendCodeText.text = timerText
            fragmentCreateAccountBinding.resendCodeText.isClickable = false
        }

        override fun onFinish() {
            updateResendCode()
        }
    }

    private fun updateResendCode() {
        fragmentCreateAccountBinding.resendCodeText.isClickable = true
        fragmentCreateAccountBinding.resendCodeText.text = resourceProvider.getString(R.string.resend_code)
        fragmentCreateAccountBinding.resendCodeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.button_color_pink))
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
        updateResendCode()
    }

    companion object {
        private const val USER_DETAIL = "signInUserModel"
        fun getInstance(signInUserModel: SignInUserModel): CreateAccountFragment {
            val createAccountFragment = CreateAccountFragment()
            createAccountFragment.arguments = Bundle().apply {
                putSerializable(USER_DETAIL, signInUserModel)
            }
            return createAccountFragment
        }
    }
}