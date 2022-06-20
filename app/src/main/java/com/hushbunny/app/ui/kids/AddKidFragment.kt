package com.hushbunny.app.ui.kids

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hbb20.countrypicker.dialog.launchCountryPickerDialog
import com.hbb20.countrypicker.models.CPCountry
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentAddKidBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.home.HomeViewModel
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.DateFormatUtils.convertISODateIntoAppDateFormat
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.*
import javax.inject.Inject

class AddKidFragment : Fragment(R.layout.fragment_add_kid) {
    private var _binding: FragmentAddKidBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: AddKidFragmentArgs by navArgs()

    @Inject
    lateinit var addKidsRepository: HomeRepository

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    private var currentStep = 1
    var isEditKid = false
    private lateinit var imageFile: File
    private lateinit var lastCapturedImageUri: Uri
    var uploadedFilePath = ""

    private val addKidsViewModel: HomeViewModel by viewModelBuilderFragmentScope {
        HomeViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            homeRepository = addKidsRepository,
            fileUploadRepository = fileUploadRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddKidBinding.bind(view)
        binding.header.headerView.visibility = View.GONE
        isEditKid = navigationArgs.isEditKid
        initializeClickListener()
        moveToStepOne()
        setObserver()
        if (isEditKid) {
            binding.header.pageTitle.text = resourceProvider.getString(R.string.edit_kid)
            updateKidsDetail()
        }
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    private fun updateKidsDetail() {
        binding.nameInput.setText(navigationArgs.kidsDetail?.name.orEmpty())
        binding.nickNameInput.setText(navigationArgs.kidsDetail?.nickName.orEmpty())
        if (navigationArgs.kidsDetail?.gender.orEmpty().equals(resourceProvider.getString(R.string.male), true)) {
            maleGenderSelected()
        } else if (navigationArgs.kidsDetail?.gender.orEmpty().equals(resourceProvider.getString(R.string.female), true)) {
            femaleGenderSelected()
        } else if (navigationArgs.kidsDetail?.gender.orEmpty().isNotEmpty()) {
            yetToBornSelected()
        }
        binding.dateOfBirthText.text = navigationArgs.kidsDetail?.dob?.convertISODateIntoAppDateFormat()
        binding.countryText.text = navigationArgs.kidsDetail?.birthCountryISO2.orEmpty()
        binding.cityInput.setText(navigationArgs.kidsDetail?.birtCity.orEmpty())
        if (navigationArgs.kidsDetail?.image.orEmpty().isNotEmpty()) {
            uploadedFilePath = navigationArgs.kidsDetail?.image.orEmpty()
            loadKidImage()
        }
    }

    private fun setObserver() {
        addKidsViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    binding.progressIndicator.showProgressbar()
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
        addKidsViewModel.addKidsObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    val dialog = SuccessDialog(requireContext())
                    dialog.show()
                    if (navigationArgs.isEditKid)
                        dialog.setMessage(resourceProvider.getString(R.string.kid_edit_success_message, it.data?.firstName.orEmpty()))
                    else dialog.setMessage(resourceProvider.getString(R.string.kid_add_success_message, it.data?.firstName.orEmpty()))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        findNavController().popBackStack()
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
        addKidsViewModel.fileUploadObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    uploadedFilePath = it.data?.url.orEmpty()
                    binding.addProfilePhotoText.text = resourceProvider.getString(R.string.profile_photo)
                    loadKidImage()
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

    private fun loadKidImage() {
        if (uploadedFilePath.isNotEmpty()) {
            binding.addKidImage.visibility = View.GONE
            binding.userImage.visibility = View.VISIBLE
            binding.userImage.loadImageFromURL(uploadedFilePath)
        } else {
            binding.addKidImage.visibility = View.VISIBLE
            binding.userImage.visibility = View.GONE
        }
    }

    private fun initializeClickListener() {
        binding.nextButton.setOnClickListener {
            when (currentStep) {
                1 -> handleStepOne()
                2 -> handleStepTwo()
                3 -> handleStepThree()
                4 -> handleStepFour()
            }
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
        binding.header.backImage.setOnClickListener {
            handleBackClick()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            handleBackClick()
        }
        binding.skipSubmitButton.setOnClickListener {
            handleStepFour()
        }
        binding.addKidImageContainer.setOnClickListener {
            requestCameraPermission()
        }

    }

    private fun handleBackClick() {
        when (currentStep) {
            4 -> moveToStepThree()
            3 -> moveToStepTwo()
            2 -> moveToStepOne()
            else -> findNavController().popBackStack()
        }
    }

    private fun handleStepOne() {
        val message =
            addKidsViewModel.addKidStepOne(binding.nameInput.text.toString().trim())
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
            addKidsViewModel.addKidStepTwo(binding.maleContainer.radioButton.isChecked || binding.femaleContainer.radioButton.isChecked || binding.yetToBornContainer.radioButton.isChecked)
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
        if (binding.yetToBornContainer.radioButton.isChecked) {
            handleStepFour()
        } else {
            val message = addKidsViewModel.addKidStepThree(binding.dateOfBirthText.text.toString().trim())
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
    }

    private fun handleStepFour() {
        addKidsViewModel.addOREditKid(
            isEditKid = isEditKid, kidID = navigationArgs.kidsDetail?._id,
            name = binding.nameInput.text.toString().trim(),
            nickName = binding.nickNameInput.text.toString().trim(),
            gender = if (binding.maleContainer.radioButton.isChecked) resourceProvider.getString(R.string.male) else if (binding.femaleContainer.radioButton.isChecked) resourceProvider.getString(
                R.string.female
            ) else resourceProvider.getString(R.string.yet_to_be_born),
            dateOfBirth = if (binding.yetToBornContainer.radioButton.isChecked) null else binding.dateOfBirthText.text.toString().trim(),
            country = binding.countryText.text.toString().trim(),
            city = binding.cityInput.text.toString().trim(),
            image = uploadedFilePath.ifEmpty { null }
        )
    }

    private fun moveToStepOne() {
        currentStep = 1
        binding.root.hideKeyboard()
        binding.titleText.text = resourceProvider.getString(R.string.name)
        binding.nickNameInput.hint = resourceProvider.getString(R.string.nick_name)
        binding.nextButton.text = resourceProvider.getString(R.string.next)
        binding.nameGroup.visibility = View.VISIBLE
        binding.genderGroup.visibility = View.GONE
        binding.dateOfBirthGroup.visibility = View.GONE
        binding.countryCityGroup.visibility = View.GONE
    }

    private fun moveToStepTwo() {
        currentStep = 2
        binding.root.hideKeyboard()
        binding.titleText.text = resourceProvider.getString(R.string.gender)
        binding.nextButton.text = resourceProvider.getString(R.string.next)
        binding.maleContainer.nameText.text = resourceProvider.getString(R.string.male)
        binding.femaleContainer.nameText.text = resourceProvider.getString(R.string.female)
        binding.yetToBornContainer.nameText.text = resourceProvider.getString(R.string.yet_to_be_born)
        binding.nameGroup.visibility = View.GONE
        binding.genderGroup.visibility = View.VISIBLE
        binding.dateOfBirthGroup.visibility = View.GONE
        binding.countryCityGroup.visibility = View.GONE
        binding.maleContainer.container.setOnClickListener {
            maleGenderSelected()
        }
        binding.femaleContainer.container.setOnClickListener {
            femaleGenderSelected()
        }
        binding.yetToBornContainer.container.setOnClickListener {
            yetToBornSelected()
        }
    }

    private fun moveToStepThree() {
        currentStep = 3
        binding.root.hideKeyboard()
        if (binding.yetToBornContainer.radioButton.isChecked) {
            binding.titleText.text = resourceProvider.getString(R.string.birth_country_and_city)
            binding.nextButton.text = resourceProvider.getString(R.string.submit)
            binding.nameGroup.visibility = View.GONE
            binding.genderGroup.visibility = View.GONE
            binding.dateOfBirthGroup.visibility = View.GONE
            binding.countryCityGroup.visibility = View.VISIBLE
        } else {
            binding.titleText.text = resourceProvider.getString(R.string.date_of_birth)
            binding.nextButton.text = resourceProvider.getString(R.string.next)
            binding.nameGroup.visibility = View.GONE
            binding.genderGroup.visibility = View.GONE
            binding.dateOfBirthGroup.visibility = View.VISIBLE
            binding.countryCityGroup.visibility = View.GONE
        }
    }

    private fun moveToStepFour() {
        currentStep = 4
        binding.root.hideKeyboard()
        binding.titleText.text = resourceProvider.getString(R.string.birth_country_and_city)
        binding.nextButton.text = resourceProvider.getString(R.string.submit)
        binding.nameGroup.visibility = View.GONE
        binding.genderGroup.visibility = View.GONE
        binding.dateOfBirthGroup.visibility = View.GONE
        binding.countryCityGroup.visibility = View.VISIBLE
        loadKidImage()
    }

    private fun openCamera() {
        try {
            imageFile = FileUtils.getImageFilePath(requireContext())
            lastCapturedImageUri = FileProvider.getUriForFile(requireContext(), FileUtils.getAuthorities(requireContext()), imageFile)
            val pickIntent = Intent()
            pickIntent.type = FileUtils.INTENT_TYPE_IMAGE
            pickIntent.action = Intent.ACTION_GET_CONTENT
            val cameraIntentCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, lastCapturedImageUri)
            val cameraIntentPick = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val pickTitle = getString(R.string.choose_app)
            val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntentCapture, cameraIntentPick))
            cameraIntentLauncher.launch(chooserIntent)
        } catch (e: Exception) {
            return
        }
    }

    private var cameraIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (!::lastCapturedImageUri.isInitialized) {
                imageFile = FileUtils.getImageFilePath(requireContext())
                lastCapturedImageUri = FileProvider.getUriForFile(requireContext(), FileUtils.getAuthorities(requireContext()), imageFile)
            }
            result.data?.data?.let {
                imageFile = FileUtils.getImageFile(requireContext(), it)
                binding.progressIndicator.showProgressbar()
                addKidsViewModel.uploadFile(imageFile)
            }
        }
    }
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            PermissionUtility.showPermissionRationaleToast(
                requireContext(),
                message = resourceProvider.getString(R.string.camera_permission_rationale)
            )
        }
    }

    private fun requestCameraPermission() {
        if (PermissionUtility.userGrantedCameraPermission(requireActivity())) {
            openCamera()
        } else {
            PermissionUtility.requestCameraPermission(requestPermission)
        }
    }

    private fun yetToBornSelected() {
        binding.maleContainer.radioButton.isChecked = false
        binding.femaleContainer.radioButton.isChecked = false
        binding.yetToBornContainer.radioButton.isChecked = true
    }

    private fun femaleGenderSelected() {
        binding.maleContainer.radioButton.isChecked = false
        binding.femaleContainer.radioButton.isChecked = true
        binding.yetToBornContainer.radioButton.isChecked = false
    }

    private fun maleGenderSelected() {
        binding.maleContainer.radioButton.isChecked = true
        binding.femaleContainer.radioButton.isChecked = false
        binding.yetToBornContainer.radioButton.isChecked = false
    }
}