package com.hushbunny.app.ui.profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.hbb20.countrypicker.dialog.launchCountryPickerDialog
import com.hbb20.countrypicker.models.CPCountry
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentEditProfileBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.model.EditedUserDetail
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.DateFormatUtils.convertISODateIntoAppDateFormat
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var userActionRepository: UserActionRepository

    private val editProfileViewModel: EditProfileViewModel by viewModelBuilderFragmentScope {
        EditProfileViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            userActionRepository = userActionRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)
        initView()
        initClickListener()
        updateFragmentResultListener()
        if (editProfileViewModel.userDetailObserver.value == null) {
            getUserProfileDetails()
            setObserver()
        }
        editProfileViewModel.editedUserDetail?.let {
            updateEditedDetail(it)
        }
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    private fun updateEditedDetail(editedUserDetail: EditedUserDetail) {
        binding.nameInput.setText(editedUserDetail.name)
        binding.dateOfBirthText.text = editedUserDetail.dateOfBirth
        binding.countryText.text = editedUserDetail.country
        binding.mobileNumberContainer.mobileNumberInput.setText(editedUserDetail.phoneNumber)
        binding.emailContainer.emailInput.setText(editedUserDetail.email)
        val gender = editedUserDetail.gender
        if (gender.equals(resourceProvider.getString(R.string.male), true)) {
            maleGenderSelected()
        } else if (gender.equals(resourceProvider.getString(R.string.female), true)) {
            femaleGenderSelected()
        } else if (gender.isNotEmpty()) {
            binding.otherContainer.nameInput.setText(gender)
            otherGenderSelected()
        }
        val associateAS = editedUserDetail.associatedWith
        if (associateAS.equals(resourceProvider.getString(R.string.father), true)) {
            fatherSelected()
        } else if (associateAS.equals(resourceProvider.getString(R.string.mother), true)) {
            motherSelected()
        }
        val callingCode = editedUserDetail.callingCode
        if (callingCode.isNotEmpty()) {
            binding.mobileNumberContainer.countrySelection.setCountryForPhoneCode(callingCode.replace("+", "").toInt())
        }
    }

    private fun updateFragmentResultListener() {
        setFragmentResultListener(APIConstants.IS_REQUIRED_API_CALL) { _, bundle ->
            if (bundle.getBoolean(APIConstants.SUCCESS)) {
                editProfileViewModel.editedUserDetail = null
                findNavController().popBackStack()
            }
        }
    }

    private fun getUserProfileDetails() {
        binding.progressIndicator.showProgressbar()
        editProfileViewModel.getUserDetail()
    }

    private fun setObserver() {
        editProfileViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    binding.progressIndicator.showProgressbar()
                    binding.root.hideKeyboard()
                }
                else -> {
                    binding.progressIndicator.hideProgressbar()
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        editProfileViewModel.userDetailObserver.observe(viewLifecycleOwner) { response ->
            binding.progressIndicator.hideProgressbar()
            when (response.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    AppConstants.saveUserDetail(response.data)
                    setUserDetail()
                }
                APIConstants.UNAUTHORIZED_CODE -> {
                    AppConstants.navigateToLoginPage(requireActivity())
                }
                else -> setUserDetail()
            }
        }
        editProfileViewModel.editProfileObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.progressIndicator.hideProgressbar()
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        editProfileViewModel.editedUserDetail = null
                        val dialog = SuccessDialog(requireContext())
                        dialog.show()
                        dialog.setMessage(resourceProvider.getString(R.string.profile_update_successfully))
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
                            message = response.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )
                    }
                }
            }
        }
        editProfileViewModel.sendORResendOTPObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.progressIndicator.hideProgressbar()
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        findNavController().navigate(EditProfileFragmentDirections.actionVerifyProfileOTPFragment(editedUserDetail = editProfileViewModel.editedUserDetail))
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

    private fun setUserDetail() {
        binding.nameInput.setText(PrefsManager.get().getString(AppConstants.USER_NAME, ""))
        binding.dateOfBirthText.text = PrefsManager.get().getString(AppConstants.USER_DATE_OF_BIRTH, "").convertISODateIntoAppDateFormat()
        binding.countryText.text = PrefsManager.get().getString(AppConstants.USER_COUNTRY, "")
        binding.mobileNumberContainer.mobileNumberInput.setText(PrefsManager.get().getString(AppConstants.USER_PHONE_NUMBER, ""))
        binding.emailContainer.emailInput.setText(PrefsManager.get().getString(AppConstants.USER_EMAIL, ""))
        val gender = PrefsManager.get().getString(AppConstants.USER_GENDER, "")
        if (gender.equals(resourceProvider.getString(R.string.male), true)) {
            maleGenderSelected()
        } else if (gender.equals(resourceProvider.getString(R.string.female), true)) {
            femaleGenderSelected()
        } else if (gender.isNotEmpty()) {
            binding.otherContainer.nameInput.setText(gender)
            otherGenderSelected()
        }
        val associateAS = PrefsManager.get().getString(AppConstants.USER_ASSOCIATE_AS, "")
        if (associateAS.equals(resourceProvider.getString(R.string.father), true)) {
            fatherSelected()
        } else if (associateAS.equals(resourceProvider.getString(R.string.mother), true)) {
            motherSelected()
        }
        val callingCode = PrefsManager.get().getString(AppConstants.USER_CALLING_CODE, "")
        if (callingCode.isNotEmpty()) {
            binding.mobileNumberContainer.countrySelection.setCountryForPhoneCode(callingCode.replace("+", "").toInt())
        }
    }


    private fun initView() {
        binding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.edit_profile)
        binding.emailContainer.emailInput.hint = resourceProvider.getString(R.string.add_email_address)
        binding.femaleContainer.nameText.text = resourceProvider.getString(R.string.female)
        binding.otherContainer.nameText.visibility = View.GONE
        binding.otherContainer.nameInput.visibility = View.VISIBLE
        binding.fatherContainer.nameText.text = resourceProvider.getString(R.string.father)
        binding.motherContainer.nameText.text = resourceProvider.getString(R.string.mother)
        binding.emailContainer.emailInput.imeOptions = EditorInfo.IME_ACTION_DONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClickListener() {
        binding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.maleContainer.container.setOnClickListener {
            binding.root.hideKeyboard()
            maleGenderSelected()
        }
        binding.femaleContainer.container.setOnClickListener {
            binding.root.hideKeyboard()
            femaleGenderSelected()
        }
        binding.otherContainer.container.setOnTouchListener { v, _ ->
            otherGenderSelected()
            false
        }
        binding.fatherContainer.container.setOnClickListener {
            binding.root.hideKeyboard()
            fatherSelected()
        }
        binding.motherContainer.container.setOnClickListener {
            binding.root.hideKeyboard()
            motherSelected()
        }
        binding.countryText.setOnClickListener {
            requireActivity().launchCountryPickerDialog(preferredCountryCodes = "US,IN") { selectedCountry: CPCountry? ->
                binding.countryText.text = selectedCountry?.name.orEmpty()
            }
        }
        binding.dateOfBirthText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    binding.dateOfBirthText.text = "$dayOfMonth/${monthOfYear + 1}/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }
        binding.submitButton.setOnClickListener {
            editProfileViewModel.validateEditProfile(
                name = binding.nameInput.text.toString().trim(),
                dateOfBirth = binding.dateOfBirthText.text.toString(),
                country = binding.countryText.text.toString(),
                mobileNumber = binding.mobileNumberContainer.mobileNumberInput.text.toString().trim(),
                email = binding.emailContainer.emailInput.text.toString().trim(),
                isMale = binding.maleContainer.radioButton.isChecked,
                isFemale = binding.femaleContainer.radioButton.isChecked,
                isOther = binding.otherContainer.radioButton.isChecked,
                otherText = binding.otherContainer.nameInput.text.toString().trim(),
                isFather = binding.fatherContainer.radioButton.isChecked,
                isMother = binding.motherContainer.radioButton.isChecked,
                callingCode = binding.mobileNumberContainer.countrySelection.selectedCountryCodeWithPlus
            )

        }
    }

    private fun motherSelected() {
        binding.fatherContainer.radioButton.isChecked = false
        binding.motherContainer.radioButton.isChecked = true
    }

    private fun fatherSelected() {
        binding.fatherContainer.radioButton.isChecked = true
        binding.motherContainer.radioButton.isChecked = false
    }

    private fun otherGenderSelected() {
        binding.maleContainer.radioButton.isChecked = false
        binding.femaleContainer.radioButton.isChecked = false
        binding.otherContainer.radioButton.isChecked = true
    }

    private fun femaleGenderSelected() {
        binding.maleContainer.radioButton.isChecked = false
        binding.femaleContainer.radioButton.isChecked = true
        binding.otherContainer.radioButton.isChecked = false
    }

    private fun maleGenderSelected() {
        binding.maleContainer.radioButton.isChecked = true
        binding.femaleContainer.radioButton.isChecked = false
        binding.otherContainer.radioButton.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}