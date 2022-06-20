package com.hushbunny.app.ui.profile

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentVerifyProfileOtpBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.model.EditedUserDetail
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VerifyProfileOTPFragment : Fragment(R.layout.fragment_verify_profile_otp), TextWatcher, View.OnKeyListener, View.OnFocusChangeListener {

    private var _binding: FragmentVerifyProfileOtpBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: VerifyProfileOTPFragmentArgs by navArgs()
    private var focusView = 0
    private val code = CharArray(4)

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var verifyOTPRepository: UserActionRepository
    private var editedUserDetail: EditedUserDetail? = null

    private val verifyOTPViewModel: EditProfileViewModel by viewModelBuilderFragmentScope {
        EditProfileViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            userActionRepository = verifyOTPRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerifyProfileOtpBinding.bind(view)
        editedUserDetail = navigationArgs.editedUserDetail
        initView(editedUserDetail?.isEmailEdited == true && editedUserDetail?.isEmailAndPhoneNumberEdited == false)
        initClickListener()
        setObserver()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    private fun setObserver() {
        verifyOTPViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    binding.progressIndicator.showProgressbar()
                }
                else -> {
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        verifyOTPViewModel.sendORResendOTPObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.progressIndicator.hideProgressbar()
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        timer.cancel()
                        initView(isEmail = true)
                    }
                    APIConstants.UNAUTHORIZED_CODE -> {
                        DialogUtils.sessionExpiredDialog(requireActivity())
                    }
                    else -> {
                        DialogUtils.showErrorDialog(
                            requireActivity(),
                            buttonText = resourceProvider.getString(R.string.ok),
                            message = response.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )
                    }
                }
            }
        }
        verifyOTPViewModel.verifyOTPObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        if (editedUserDetail?.isEmailAndPhoneNumberEdited == true) {
                            editedUserDetail = editedUserDetail?.copy(isEmailAndPhoneNumberEdited = false)
                            verifyOTPViewModel.sendOTPForEmail(editedUserDetail?.email)
                        } else {
                            binding.progressIndicator.hideProgressbar()
                            verifyOTPViewModel.updateProfile(editedUserDetail)
                        }
                    }
                    APIConstants.UNAUTHORIZED_CODE -> {
                        binding.progressIndicator.hideProgressbar()
                        DialogUtils.sessionExpiredDialog(requireActivity())
                    }
                    else -> {
                        binding.progressIndicator.hideProgressbar()
                        DialogUtils.showErrorDialog(
                            requireActivity(),
                            buttonText = resourceProvider.getString(R.string.ok),
                            message = response.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )
                    }
                }
            }
        }
        verifyOTPViewModel.editProfileObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.progressIndicator.hideProgressbar()
                timer.cancel()
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        val dialog = SuccessDialog(requireContext())
                        dialog.show()
                        dialog.setMessage(resourceProvider.getString(R.string.profile_update_successfully))
                        Handler(Looper.getMainLooper()).postDelayed({
                            dialog.dismiss()
                            setFragmentResult(APIConstants.IS_REQUIRED_API_CALL, bundleOf(APIConstants.SUCCESS to true))
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
                            message = response.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )
                    }
                }
            }
        }
    }

    private fun initClickListener() {
        binding.otpContainer.inputOne.addTextChangedListener(this)
        binding.otpContainer.inputTwo.addTextChangedListener(this)
        binding.otpContainer.inputThree.addTextChangedListener(this)
        binding.otpContainer.inputFour.addTextChangedListener(this)

        binding.otpContainer.inputOne.setOnKeyListener(this)
        binding.otpContainer.inputTwo.setOnKeyListener(this)
        binding.otpContainer.inputThree.setOnKeyListener(this)
        binding.otpContainer.inputFour.setOnKeyListener(this)

        binding.otpContainer.inputOne.onFocusChangeListener = this
        binding.otpContainer.inputTwo.onFocusChangeListener = this
        binding.otpContainer.inputThree.onFocusChangeListener = this
        binding.otpContainer.inputFour.onFocusChangeListener = this

        binding.resendCodeText.setOnClickListener {
            binding.progressIndicator.showProgressbar()
            verifyOTPViewModel.reSendOTP()
        }

        binding.verifyButton.setOnClickListener {
            binding.root.hideKeyboard()
            verifyOTPViewModel.verifyOTP(
                otpOne = binding.otpContainer.inputOne.text.toString().trim(),
                otpTwo = binding.otpContainer.inputTwo.text.toString().trim(),
                otpThree = binding.otpContainer.inputThree.text.toString().trim(),
                otpFour = binding.otpContainer.inputFour.text.toString().trim()
            )
        }
        binding.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }

    private fun initView(isEmail: Boolean) {
        if (isEmail) {
            binding.titleText.text = resourceProvider.getString(R.string.verify_email)
            binding.verifyMessageText.text = resourceProvider.getString(R.string.please_enter_the_four_digits_code_sent_on_your_email)
        } else {
            binding.titleText.text = resourceProvider.getString(R.string.verify_mobile_number)
            binding.verifyMessageText.text = resourceProvider.getString(R.string.please_enter_the_four_digits_code_sent_on_your_mobile)
        }
        binding.otpContainer.inputOne.setText("")
        binding.otpContainer.inputTwo.setText("")
        binding.otpContainer.inputThree.setText("")
        binding.otpContainer.inputFour.setText("")
        timer.start()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        when (focusView) {
            1 -> {
                if (!binding.otpContainer.inputOne.text.toString().isEmpty()) {
                    code[0] = binding.otpContainer.inputOne.text.toString().trim()[0]
                    binding.otpContainer.inputTwo.requestFocus()
                }
            }
            2 -> {
                if (!binding.otpContainer.inputTwo.text.toString().isEmpty()) {
                    code[1] = binding.otpContainer.inputTwo.text.toString()[0]
                    binding.otpContainer.inputThree.requestFocus()
                }
            }
            3 -> {
                if (!binding.otpContainer.inputThree.text.toString().isEmpty()) {
                    code[2] = binding.otpContainer.inputThree.text.toString()[0]
                    binding.otpContainer.inputFour.requestFocus()
                }
            }
            4 -> {
                if (!binding.otpContainer.inputFour.text.toString().isEmpty()) {
                    code[3] = binding.otpContainer.inputFour.text.toString()[0]
                }
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                when (v?.id) {
                    R.id.input_two -> {
                        if (binding.otpContainer.inputTwo.text.toString().isEmpty())
                            binding.otpContainer.inputOne.requestFocus()
                    }
                    R.id.input_three -> {
                        if (binding.otpContainer.inputThree.text.toString().isEmpty())
                            binding.otpContainer.inputTwo.requestFocus()
                    }
                    R.id.input_four -> {
                        if (binding.otpContainer.inputFour.text.toString().isEmpty())
                            binding.otpContainer.inputThree.requestFocus()
                    }
                }
            }
        }
        return false
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        when (v?.id) {
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
            binding.resendCodeText.visibility = View.VISIBLE
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
            binding.resendCodeText.text = timerText
            binding.resendCodeText.isClickable = false
        }

        override fun onFinish() {
            updateResendCode()
        }
    }

    private fun updateResendCode() {
        binding.resendCodeText.isClickable = true
        binding.resendCodeText.text = resourceProvider.getString(R.string.resend_code)
        binding.resendCodeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.button_color_pink))
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
        updateResendCode()
    }
}