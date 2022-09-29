package com.hushbunny.app.ui.onboarding.signup

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.hbb20.countrypicker.dialog.launchCountryPickerDialog
import com.hbb20.countrypicker.models.CPCountry
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentSignUpBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.onboarding.createaccount.CreateAccountFragment
import com.hushbunny.app.ui.onboarding.login.LoginFragment
import com.hushbunny.app.ui.onboarding.model.SignInUserModel
import com.hushbunny.app.ui.onboarding.viewmodel.LoginViewModel
import com.hushbunny.app.ui.repository.OnBoardingRepository
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.DateFormatUtils.convertDateIntoAppDateFormat
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private var _fragmentSignUpBinding: FragmentSignUpBinding? = null
    private val fragmentSignUpBinding: FragmentSignUpBinding get() = _fragmentSignUpBinding!!

    @Inject
    lateinit var signUpRepository: OnBoardingRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    private var signUpStep = 1
    private var selectedCountryName = ""

    private val signUpViewModel: LoginViewModel by viewModelBuilderFragmentScope {
        LoginViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            onBoardingRepository = signUpRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _fragmentSignUpBinding = FragmentSignUpBinding.bind(view)
        initializeClickListener()
        setAlreadyHaveAccountText()
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
        fragmentSignUpBinding.alreadyHaveAnAccountText.text = signUpText
    }


    private fun initializeClickListener() {
        fragmentSignUpBinding.backImage.setOnClickListener {
            handleBackClick()
        }
        fragmentSignUpBinding.alreadyHaveAnAccountText.setOnClickListener {
            navigateToLoginPage()
        }
        fragmentSignUpBinding.dateOfBirthText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    fragmentSignUpBinding.dateOfBirthText.text =
                        "$dayOfMonth/${monthOfYear + 1}/$year".convertDateIntoAppDateFormat()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.maxDate = Date().time
            dpd.show()
        }
        fragmentSignUpBinding.nextButton.setOnClickListener {
            when (signUpStep) {
                1 -> {
                    handleStepOne()
                }
                2 -> {
                    handleStepTwo()
                }
                3 -> {
                    handleStepThree()
                }
                4 -> {
                    handleStepFour()
                }
                5 -> {
                    handleStepFive()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            handleBackClick()
        }
        fragmentSignUpBinding.countryText.setOnClickListener {
            requireActivity().launchCountryPickerDialog(preferredCountryCodes = "US,IN") { selectedCountry: CPCountry? ->
                selectedCountryName  = selectedCountry?.alpha2.orEmpty()
                fragmentSignUpBinding.countryText.text = selectedCountry?.name.orEmpty()
            }
        }
    }

    private fun handleStepOne() {
        val message =
            signUpViewModel.signUpStepOne(fragmentSignUpBinding.nameInput.text.toString().trim())
        if (message.isEmpty()) {
            moveToStepTwo()
        } else {
            DialogUtils.showErrorDialog(
                requireActivity(),
                buttonText = resourceProvider.getString(R.string.ok),
                message = message,
                title = resourceProvider.getString(R.string.app_name)
            )
        }
    }

    private fun handleStepTwo() {
        val message =
            signUpViewModel.signUpStepTwo(
                fragmentSignUpBinding.dateOfBirthText.text.toString().trim()
            )
        if (message.isEmpty()) {
            moveToStepThree()
        } else {
            DialogUtils.showErrorDialog(
                requireActivity(),
                buttonText = resourceProvider.getString(R.string.ok),
                message = message,
                title = resourceProvider.getString(R.string.app_name)
            )
        }
    }

    private fun handleStepThree() {
        val message =
            signUpViewModel.signUpStepThree(
                isMale = fragmentSignUpBinding.maleContainer.radioButton.isChecked,
                isFemale = fragmentSignUpBinding.femaleContainer.radioButton.isChecked,
                isOthers = fragmentSignUpBinding.otherContainer.radioButton.isChecked,
                others = fragmentSignUpBinding.otherContainer.nameInput.text.toString().trim()
            )
        if (message.isEmpty()) {
            moveToStepFour()
        } else {
            DialogUtils.showErrorDialog(
                requireActivity(),
                buttonText = resourceProvider.getString(R.string.ok),
                message = message,
                title = resourceProvider.getString(R.string.app_name)
            )
        }
    }

    private fun handleStepFive() {
        val message =
            signUpViewModel.signUpStepFive(
                country = fragmentSignUpBinding.countryText.text.toString().trim()
            )
        if (message.isEmpty()) {
            requireActivity().supportFragmentManager.commit {
                add(
                    R.id.fragment_container_view,
                    CreateAccountFragment.getInstance(
                        SignInUserModel(
                            name = fragmentSignUpBinding.nameInput.text.toString().trim(),
                            dateOfBirth = fragmentSignUpBinding.dateOfBirthText.text.toString().trim(),
                            gender = if (fragmentSignUpBinding.maleContainer.radioButton.isChecked) APIConstants.MALE else if (fragmentSignUpBinding.femaleContainer.radioButton.isChecked) APIConstants.FEMALE else fragmentSignUpBinding.otherContainer.nameInput.text.toString().trim(),
                            relationShipWithKid = if (fragmentSignUpBinding.fatherContainer.radioButton.isChecked) resourceProvider.getString(R.string.father) else resourceProvider.getString(
                                R.string.mother
                            ),
                            country = selectedCountryName
                        )
                    ),
                    CreateAccountFragment::class.simpleName.toString()
                )
                addToBackStack(null)
            }
        } else {
            DialogUtils.showErrorDialog(
                requireActivity(),
                buttonText = resourceProvider.getString(R.string.ok),
                message = message,
                title = resourceProvider.getString(R.string.app_name)
            )
        }
    }

    private fun handleStepFour() {
        val message =
            signUpViewModel.signUpStepFour(
                isFather = fragmentSignUpBinding.fatherContainer.radioButton.isChecked,
                isMother = fragmentSignUpBinding.motherContainer.radioButton.isChecked
            )
        if (message.isEmpty()) {
            moveToStepFive()
        } else {
            DialogUtils.showErrorDialog(
                requireActivity(),
                buttonText = resourceProvider.getString(R.string.ok),
                message = message,
                title = resourceProvider.getString(R.string.app_name)
            )
        }
    }

    private fun moveToStepOne() {
        signUpStep = 1
        fragmentSignUpBinding.root.hideKeyboard()
        fragmentSignUpBinding.stepOneGroup.visibility = View.VISIBLE
        fragmentSignUpBinding.stepTwoGroup.visibility = View.GONE
        fragmentSignUpBinding.stepThreeGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFourGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFiveGroup.visibility = View.GONE
        setUpStepByStepImage()
    }

    private fun moveToStepTwo() {
        signUpStep = 2
        fragmentSignUpBinding.root.hideKeyboard()
        fragmentSignUpBinding.stepOneGroup.visibility = View.GONE
        fragmentSignUpBinding.stepTwoGroup.visibility = View.VISIBLE
        fragmentSignUpBinding.stepThreeGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFourGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFiveGroup.visibility = View.GONE
        setUpStepByStepImage()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun moveToStepThree() {
        signUpStep = 3
        fragmentSignUpBinding.root.hideKeyboard()
        fragmentSignUpBinding.stepOneGroup.visibility = View.GONE
        fragmentSignUpBinding.stepTwoGroup.visibility = View.GONE
        fragmentSignUpBinding.stepThreeGroup.visibility = View.VISIBLE
        fragmentSignUpBinding.stepFourGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFiveGroup.visibility = View.GONE
        fragmentSignUpBinding.femaleContainer.nameText.text =
            resourceProvider.getString(R.string.female)
        fragmentSignUpBinding.otherContainer.nameInput.visibility = View.VISIBLE
        fragmentSignUpBinding.otherContainer.nameText.visibility = View.GONE

        fragmentSignUpBinding.maleContainer.container.setOnClickListener {
            fragmentSignUpBinding.root.hideKeyboard()
            fragmentSignUpBinding.maleContainer.radioButton.isChecked = true
            fragmentSignUpBinding.femaleContainer.radioButton.isChecked = false
            fragmentSignUpBinding.otherContainer.radioButton.isChecked = false
        }
        fragmentSignUpBinding.femaleContainer.container.setOnClickListener {
            fragmentSignUpBinding.root.hideKeyboard()
            fragmentSignUpBinding.maleContainer.radioButton.isChecked = false
            fragmentSignUpBinding.femaleContainer.radioButton.isChecked = true
            fragmentSignUpBinding.otherContainer.radioButton.isChecked = false
        }
        fragmentSignUpBinding.otherContainer.container.setOnTouchListener { v, _ ->
            fragmentSignUpBinding.maleContainer.radioButton.isChecked = false
            fragmentSignUpBinding.femaleContainer.radioButton.isChecked = false
            fragmentSignUpBinding.otherContainer.radioButton.isChecked = true
            false
        }
        fragmentSignUpBinding.otherContainer.nameInput.setOnTouchListener { v, _ ->
            fragmentSignUpBinding.maleContainer.radioButton.isChecked = false
            fragmentSignUpBinding.femaleContainer.radioButton.isChecked = false
            fragmentSignUpBinding.otherContainer.radioButton.isChecked = true
            false
        }
        setUpStepByStepImage()
    }

    private fun moveToStepFour() {
        signUpStep = 4
        fragmentSignUpBinding.root.hideKeyboard()
        fragmentSignUpBinding.stepOneGroup.visibility = View.GONE
        fragmentSignUpBinding.stepTwoGroup.visibility = View.GONE
        fragmentSignUpBinding.stepThreeGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFourGroup.visibility = View.VISIBLE
        fragmentSignUpBinding.stepFiveGroup.visibility = View.GONE
        fragmentSignUpBinding.fatherContainer.nameText.text =
            resourceProvider.getString(R.string.father)
        fragmentSignUpBinding.motherContainer.nameText.text =
            resourceProvider.getString(R.string.mother)
        fragmentSignUpBinding.fatherContainer.radioButton.isChecked =
            fragmentSignUpBinding.maleContainer.radioButton.isChecked
        fragmentSignUpBinding.motherContainer.radioButton.isChecked =
            fragmentSignUpBinding.femaleContainer.radioButton.isChecked

        fragmentSignUpBinding.fatherContainer.container.setOnClickListener {
            fragmentSignUpBinding.root.hideKeyboard()
            fragmentSignUpBinding.fatherContainer.radioButton.isChecked = true
            fragmentSignUpBinding.motherContainer.radioButton.isChecked = false
        }
        fragmentSignUpBinding.motherContainer.container.setOnClickListener {
            fragmentSignUpBinding.root.hideKeyboard()
            fragmentSignUpBinding.fatherContainer.radioButton.isChecked = false
            fragmentSignUpBinding.motherContainer.radioButton.isChecked = true
        }
        setUpStepByStepImage()
    }

    private fun moveToStepFive() {
        signUpStep = 5
        fragmentSignUpBinding.root.hideKeyboard()
        fragmentSignUpBinding.stepOneGroup.visibility = View.GONE
        fragmentSignUpBinding.stepTwoGroup.visibility = View.GONE
        fragmentSignUpBinding.stepThreeGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFourGroup.visibility = View.GONE
        fragmentSignUpBinding.stepFiveGroup.visibility = View.VISIBLE
        setUpStepByStepImage()
    }

    private fun handleBackClick() {
        when (signUpStep) {
            5 -> moveToStepFour()
            4 -> moveToStepThree()
            3 -> moveToStepTwo()
            2 -> moveToStepOne()
            else -> navigateToLoginPage()
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

    private fun setUpStepByStepImage() {
        fragmentSignUpBinding.stepByStepContainer.stepOneView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_color_gray
            )
        )
        fragmentSignUpBinding.stepByStepContainer.stepTwoView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_color_gray
            )
        )
        fragmentSignUpBinding.stepByStepContainer.stepThreeView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_color_gray
            )
        )
        fragmentSignUpBinding.stepByStepContainer.stepFourView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_color_gray
            )
        )
        fragmentSignUpBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_gray)
        fragmentSignUpBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_gray)
        fragmentSignUpBinding.stepByStepContainer.stepThreeImage.setImageResource(R.drawable.ic_round_background_gray)
        fragmentSignUpBinding.stepByStepContainer.stepFourImage.setImageResource(R.drawable.ic_round_background_gray)
        fragmentSignUpBinding.stepByStepContainer.stepFiveImage.setImageResource(R.drawable.ic_round_background_gray)
        val layoutParams =
            fragmentSignUpBinding.hushBunnyLogoImage.layoutParams as ConstraintLayout.LayoutParams
        val marginLeft = resourceProvider.getDimension(R.dimen.margin_60).toInt()
        when (signUpStep) {
            1 -> {
                layoutParams.setMargins(marginLeft, 0, 0, 0)
                fragmentSignUpBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
            }
            2 -> {
                layoutParams.setMargins(
                    resourceProvider.getDimension(R.dimen.margin_110).toInt(),
                    0,
                    0,
                    0
                )
                fragmentSignUpBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepOneView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
            }
            3 -> {
                layoutParams.setMargins(
                    resourceProvider.getDimension(R.dimen.margin_165).toInt(),
                    0,
                    0,
                    0
                )
                fragmentSignUpBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepThreeImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepOneView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentSignUpBinding.stepByStepContainer.stepTwoView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
            }
            4 -> {
                layoutParams.setMargins(
                    resourceProvider.getDimension(R.dimen.margin_220).toInt(),
                    0,
                    0,
                    0
                )
                fragmentSignUpBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepThreeImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepFourImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepOneView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentSignUpBinding.stepByStepContainer.stepTwoView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentSignUpBinding.stepByStepContainer.stepThreeView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
            }
            5 -> {
                layoutParams.setMargins(
                    resourceProvider.getDimension(R.dimen.margin_276).toInt(),
                    0,
                    0,
                    0
                )
                fragmentSignUpBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepThreeImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepFourImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepFiveImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentSignUpBinding.stepByStepContainer.stepOneView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentSignUpBinding.stepByStepContainer.stepTwoView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentSignUpBinding.stepByStepContainer.stepThreeView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentSignUpBinding.stepByStepContainer.stepFourView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
            }

        }
        fragmentSignUpBinding.hushBunnyLogoImage.layoutParams = layoutParams
    }

    companion object {
        fun getInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }
}