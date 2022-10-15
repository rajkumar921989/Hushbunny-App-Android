package com.hushbunny.app.ui.moment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentAddMomentBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.MediaType
import com.hushbunny.app.ui.enumclass.MomentDateType
import com.hushbunny.app.ui.enumclass.MomentType
import com.hushbunny.app.ui.home.KidsAdapter
import com.hushbunny.app.ui.model.KidsResponseModel
import com.hushbunny.app.ui.model.MomentListingModel
import com.hushbunny.app.ui.model.MomentMediaModel
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.FileDownLoadState
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.ui.sealedclass.MomentResponseInfo
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.DateFormatUtils.convertDateIntoAppDateFormat
import com.hushbunny.app.uitls.DateFormatUtils.convertISODateIntoAppDateFormat
import com.hushbunny.app.uitls.FileUtils.Companion.saveImage
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.dialog.SuccessDialog
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


class AddMomentFragment : Fragment(R.layout.fragment_add_moment) {

    private var _binding: FragmentAddMomentBinding? = null
    private val binding get() = _binding!!
    private lateinit var kidsAdapter: KidsAdapter
    private val navigationArgs: AddMomentFragmentArgs by navArgs()
    private lateinit var imageFile: File
    private lateinit var videoFile: File
    private lateinit var lastCapturedImageUri: Uri
    private lateinit var lastCapturedVideoUri: Uri
    private var isImportant = false
    private var isEdit = false
    private var isSpouseAdded = false
    private var momentId: String = ""
    private var momentDetail: MomentListingModel? = null
    private var mediaImageUriList = listOf<Uri>()
    private val allTextUrlsList = hashSetOf<String>()

    @Inject
    lateinit var momentRepository: MomentRepository

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    private val addMomentViewModel: AddMomentViewModel by viewModelBuilderFragmentScope {
        AddMomentViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            momentRepository = momentRepository,
            fileUploadRepository = fileUploadRepository
        )
    }
    private val imageRequestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            addImageOrVideo()
        } else {
            PermissionUtility.showPermissionRationaleToast(
                requireContext(),
                message = resourceProvider.getString(R.string.camera_permission_rationale)
            )
        }
    }
    private val externalStorageRequestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
        } else {
            PermissionUtility.showPermissionRationaleToast(
                requireContext(),
                message = resourceProvider.getString(R.string.write_permission_rationale)
            )
        }
    }
    private val videoRequestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takeImageOrVideo()
        } else {
            PermissionUtility.showPermissionRationaleToast(
                requireContext(),
                message = resourceProvider.getString(R.string.camera_permission_rationale)
            )
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let {
                imageFile = FileUtils.getImageFile(requireContext(), it)
            }
        }
        lifecycleScope.launch {
            if (imageFile.exists()) {
                val compressedImageFile = Compressor.compress(requireContext(), imageFile) {
                    default(
                        width = resourceProvider.getDimension(R.dimen.view_320).toInt(),
                        height = resourceProvider.getDimension(R.dimen.view_270).toInt(),
                        format = Bitmap.CompressFormat.JPEG
                    )
                }
                addImageToImageList(
                    imageModel = MomentMediaModel(
                        type = MediaType.IMAGE.name,
                        isUploaded = false,
                        original = if (compressedImageFile.exists()) compressedImageFile.toString() else imageFile.toString()
                    ),
                )
            }
        }
    }

    private var imageIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (!::lastCapturedImageUri.isInitialized) {
                imageFile = FileUtils.getImageFilePath(requireContext())
                lastCapturedImageUri = FileProvider.getUriForFile(requireContext(), FileUtils.getAuthorities(requireContext()), imageFile)
            }
            lifecycleScope.launch {
                if (result.data?.clipData != null) {
                    val count: Int = result.data?.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        val filePath = FileUtils.getImageFile(requireContext(), result.data?.clipData?.getItemAt(i)?.uri)
                        if (filePath.exists()) {
                            val compressedImageFile = Compressor.compress(requireContext(), filePath) {
                                default(
                                    width = resourceProvider.getDimension(R.dimen.view_320).toInt(),
                                    height = resourceProvider.getDimension(R.dimen.view_270).toInt(),
                                    format = Bitmap.CompressFormat.JPEG
                                )
                            }
                            addMomentViewModel.momentImageList.add(0,
                                MomentMediaModel(
                                    type = MediaType.IMAGE.name,
                                    isUploaded = false,
                                    original = if (compressedImageFile.exists()) compressedImageFile.toString() else filePath.toString()
                                )
                            )
                        }
                    }
                    refreshAdapter(true)
                } else {
                    result.data?.data?.let {
                        imageFile = FileUtils.getImageFile(requireContext(), it)
                    }
                    if (imageFile.exists()) {
                        cropImage.launch(options(
                            uri = imageFile.toUri()
                        ) {
                            setGuidelines(CropImageView.Guidelines.ON)
                            setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                            setToolbarColor(color = ContextCompat.getColor(requireContext(), R.color.colorAccent))
                        })
                    }
                }
            }
        }
    }
    private var videoIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (!::lastCapturedVideoUri.isInitialized) {
                videoFile = FileUtils.getVideoFilePath(requireContext())
                lastCapturedVideoUri = FileProvider.getUriForFile(requireContext(), FileUtils.getAuthorities(requireContext()), videoFile)
            }
            lifecycleScope.launch {
                if (result.data?.clipData != null) {
                    val count: Int = result.data?.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        val filePath = FileUtils.getImageFile(requireContext(), result.data?.clipData?.getItemAt(i)?.uri)
                        if (filePath.exists()) {
                            addMomentViewModel.momentImageList.add(0,
                                MomentMediaModel(type = MediaType.VIDEO.name, isUploaded = false, thumbnail = filePath.toString())
                            )
                        }
                    }
                    refreshAdapter(true)
                } else {
                    result.data?.data?.let {
                        videoFile = FileUtils.getImageFile(requireContext(), it)
                    }
                    if (videoFile.exists()) {
                        addImageToImageList(
                            imageModel = MomentMediaModel(type = MediaType.VIDEO.name, isUploaded = false, thumbnail = videoFile.toString()),
                        )
                    }
                }
            }
        }
    }

    private fun ClipData.convertToList(): List<Uri> = 0.until(itemCount).map { getItemAt(it).uri }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEdit = navigationArgs.isEdit
        isSpouseAdded = navigationArgs.isSpouseAdded
        momentId = navigationArgs.momentID
        addMomentViewModel.momentImageList.clear()
        addMomentViewModel.selectedKidList.clear()
        if (PrefsManager.get().getBoolean(AppConstants.GALLERY_MEDIA, false))
            mediaImageUriList = activity?.intent?.clipData?.convertToList().orEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddMomentBinding.bind(view)
        PrefsManager.get().saveBooleanValues(AppConstants.GALLERY_MEDIA, false)
        initView()
        requestWritePermission()
        initializeClickListener()
        setAdapter()
        setObserver()
        getKidsList()
        setTodayDate()
        if ((isEdit || isSpouseAdded) && momentDetail == null)
            getMomentDetail()
        updateUriMediaView()
    }

    @SuppressLint("SetTextI18n")
    private fun setTodayDate() {
        binding.dateButton.text = setCurrentDate()
    }

    private fun updateUriMediaView() {
        if (mediaImageUriList.isNotEmpty()) {
            for (media in mediaImageUriList) {
                addMomentViewModel.momentImageList.add(0,
                    MomentMediaModel(
                        type = if (media.toString().contains(".mp4")) MediaType.VIDEO.name else MediaType.IMAGE.name,
                        isUploaded = false,
                        original = if (media.toString().contains(".mp4")) null else FileUtils.getImageFile(requireContext(), media).toString(),
                        thumbnail = if (media.toString().contains(".mp4")) FileUtils.getImageFile(requireContext(), media).toString() else null
                    )
                )
            }
        }
        refreshAdapter(true)
    }

    private fun getMomentDetail() {
        binding.progressIndicator.showProgressbar()
        addMomentViewModel.getMomentList(currentPage = 1, type = MomentType.ALL.name, momentID = momentId, sortBy = MomentDateType.MOMENT_DATE.name)
    }

    private fun initializeClickListener() {
        binding.header.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.dateButton.setOnClickListener {
            addMomentViewModel.selectedKidList.clear()
            addMomentViewModel.selectedKidList.addAll(kidsAdapter.getKidsList())
            findNavController().navigate(
                AddMomentFragmentDirections.actionMomentDateFragment(
                    isCurrentDate = binding.dateButton.text.toString() == setCurrentDate(),
                    otherDate = binding.dateButton.text.toString()
                )
            )
        }
        binding.addAPhotoVideoButton.root.setOnClickListener {
            requestImagePermission()
        }
        binding.takeAPhotoVideoButton.root.setOnClickListener {
            requestVideoPermission()
        }
        binding.markAsImportantMomentButton.root.setOnClickListener {
            isImportant = !isImportant
            updateImportantDetail()
        }
        binding.addMediaImage.setOnClickListener {
            requestImagePermission()
        }
        binding.takeMediaImage.setOnClickListener {
            requestVideoPermission()
        }
        binding.importantMomentImage.setOnClickListener {
            isImportant = !isImportant
            updateImportantDetail()
        }
        binding.header.postButton.setOnClickListener {
            addMomentViewModel.selectedKidList.clear()
            addMomentViewModel.selectedKidList.addAll(kidsAdapter.getKidsList())
            addMomentViewModel.addOREditMoment(
                isEdit = isEdit,
                momentId = if (isSpouseAdded) "" else momentId,
                momentDate = binding.dateButton.text.toString(),
                isImportant = isImportant,
                description = binding.saySomethingAboutThisMomentInput.text.toString().trim()
            )
        }
        binding.saySomethingAboutThisMomentInput.doAfterTextChanged {
            val text = it?.toString().orEmpty().trim()
            if (text.isNotEmpty()) {
                val urlToParse = getLastUrl(text)
                if (urlToParse.isNotEmpty()) {
                    if(!allTextUrlsList.contains(urlToParse)) {
                        allTextUrlsList.add(urlToParse)
                        addMomentViewModel.loadImageLink(urlToParse = urlToParse)
                    }
                }
            }
        }
    }

    private fun getLastUrl(input: String): String {
        val containedUrls = ArrayList<String>()
        val urlMatcher = getPatternMatcher(
            urlRegex = "(?:(?:https?|ftp):\\/\\/)?[\\w/\\-?=%.]+\\.[\\w/\\-?=%.]+",
            input
        )
        while (urlMatcher.find()) {
            containedUrls.add(
                input.substring(
                    urlMatcher.start(0),
                    urlMatcher.end(0)
                )
            )
        }
        return containedUrls.lastOrNull().orEmpty()
    }

    private fun getPatternMatcher(urlRegex: String, input: String): Matcher {
        val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
        return pattern.matcher(input)
    }

    private fun isKeyboardOpen(): Boolean {
        val visibleBounds = Rect()
        requireActivity().getRootView().getWindowVisibleDisplayFrame(visibleBounds)
        val heightDiff = requireActivity().getRootView().height - visibleBounds.height()
        val marginOfError = resourceProvider.getDimension(R.dimen.margin_70)
        return heightDiff > marginOfError
    }

    private fun Activity.getRootView(): View {
        return findViewById<View>(android.R.id.content)
    }

    private fun updateImportantDetail() {
        binding.markAsImportantMomentButton.buttonTitle.text = resourceProvider.getString(R.string.mark_as_important_moment)
        if (isImportant) {
            binding.markAsImportantMomentButton.momentImage.setImageResource(R.drawable.ic_important_marked)
            binding.starImage.setImageResource(R.drawable.ic_important_marked)
            binding.importantMomentImage.setImageResource(R.drawable.ic_important_marked)
        } else {
            binding.markAsImportantMomentButton.momentImage.setImageResource(R.drawable.ic_important)
            binding.starImage.setImageResource(R.drawable.ic_important)
            binding.importantMomentImage.setImageResource(R.drawable.ic_important)
        }
    }

    private fun getKidsList() {
        binding.kidsList.visibility = View.INVISIBLE
        binding.kidsShimmerContainer.visibility = View.VISIBLE
        addMomentViewModel.getKidsList()
    }

    private fun initView() {
        binding.header.postButton.visibility = View.VISIBLE
        if (isEdit)
            binding.header.pageTitle.text = resourceProvider.getString(R.string.edit_moment)
        else binding.header.pageTitle.text = resourceProvider.getString(R.string.add_moment)
        binding.header.backImage.setImageResource(R.drawable.ic_close)
        binding.addAPhotoVideoButton.momentImage.setImageResource(R.drawable.ic_select_image_video)
        binding.takeAPhotoVideoButton.momentImage.setImageResource(R.drawable.ic_take_image_video)
        binding.markAsImportantMomentButton.momentImage.setImageResource(R.drawable.ic_setting_important)
        binding.addAPhotoVideoButton.buttonTitle.text = resourceProvider.getString(R.string.add_a_photo_video)
        binding.takeAPhotoVideoButton.buttonTitle.text = resourceProvider.getString(R.string.take_a_photo_video)
        binding.markAsImportantMomentButton.buttonTitle.text = resourceProvider.getString(R.string.mark_as_important_moment)
    }

    private fun requestImagePermission() {
        if (PermissionUtility.userGrantedCameraPermission(requireActivity())) {
            addImageOrVideo()
        } else {
            PermissionUtility.requestCameraPermission(imageRequestPermission)
        }
    }

    private fun requestWritePermission() {
        if (PermissionUtility.userGrantedExternalPermission(requireActivity())) {
        } else {
            PermissionUtility.requestExternalPermission(externalStorageRequestPermission)
        }
    }

    private fun requestVideoPermission() {
        if (PermissionUtility.userGrantedCameraPermission(requireActivity())) {
            takeImageOrVideo()
        } else {
            PermissionUtility.requestCameraPermission(videoRequestPermission)
        }
    }

    private fun selectImage(isCamera: Boolean) {
        try {
            imageFile = FileUtils.getImageFilePath(requireContext())
            lastCapturedImageUri = FileProvider.getUriForFile(requireContext(), FileUtils.getAuthorities(requireContext()), imageFile)
            val pickIntent = Intent()
            pickIntent.type = FileUtils.INTENT_TYPE_IMAGE
            pickIntent.action = Intent.ACTION_GET_CONTENT
            val intent = if (isCamera) {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    lastCapturedImageUri
                )
            } else {
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            }
            if (!isCamera) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            imageIntentLauncher.launch(intent)
        } catch (e: Exception) {
            return
        }
    }

    private fun selectVideo(isCamera: Boolean) {
        try {
            videoFile = FileUtils.getVideoFilePath(requireContext())
            lastCapturedVideoUri = FileProvider.getUriForFile(requireContext(), FileUtils.getAuthorities(requireContext()), videoFile)
            val intent = if (isCamera) {
                Intent(MediaStore.ACTION_VIDEO_CAPTURE).putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    lastCapturedVideoUri
                )
            } else {
                val videoPickIntent = Intent(Intent.ACTION_PICK)
                videoPickIntent.type = FileUtils.INTENT_TYPE_VIDEO
                videoPickIntent
            }
            videoIntentLauncher.launch(intent)
        } catch (e: Exception) {
            return
        }
    }

    private fun setCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}".convertDateIntoAppDateFormat()
    }

    private fun setAdapter() {
        kidsAdapter = KidsAdapter(resourceProvider = resourceProvider, isAddMoment = true, addKidsClick = {
            findNavController().navigate(AddMomentFragmentDirections.actionAddKidFragment(isEditKid = false))
        })
        binding.kidsList.adapter = kidsAdapter
    }

    private fun setObserver() {
        setFragmentResultListener(AppConstants.MOMENT_DATE) { _, bundle ->
            binding.dateButton.text = bundle.getString(AppConstants.DATE)
        }
        addMomentViewModel.loadImageResourceResponseObserver.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is FileDownLoadState.Success -> {
                        binding.progressIndicator.hideProgressbar()
                        val filePath = response.bitmap.saveImage(requireContext())
                        lifecycleScope.launch {
                            val compressedImageFile = filePath?.let { it1 ->
                                Compressor.compress(requireContext(), it1) {
                                    default(
                                        width = resourceProvider.getDimension(R.dimen.view_320).toInt(),
                                        height = resourceProvider.getDimension(R.dimen.view_270).toInt(),
                                        format = Bitmap.CompressFormat.JPEG
                                    )
                                }
                            }
                            addImageToImageList(
                                imageModel = MomentMediaModel(
                                    type = MediaType.IMAGE.name,
                                    isUploaded = false,
                                    text = response.imageText,
                                    original = if (compressedImageFile?.exists() == true) compressedImageFile.toString() else filePath.toString()
                                ),
                            )
                        }
                    }
                }
        }
        addMomentViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
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
        addMomentViewModel.kidsListObserver.observe(viewLifecycleOwner) { response ->
            binding.kidsShimmerContainer.visibility = View.GONE
            when (response) {
                is KidsStatusInfo.UserList -> {
                    binding.kidsList.visibility = View.VISIBLE
                    updateKidDetail(response.userList)
                }
                else -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
            }
        }
        addMomentViewModel.addOrEditMomentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    val dialog = SuccessDialog(requireContext())
                    dialog.show()
                    if (isEdit)
                        dialog.setMessage(resourceProvider.getString(R.string.moment_edited_successfully))
                    else dialog.setMessage(resourceProvider.getString(R.string.moment_added_successfully))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        findNavController().popBackStack()
                    }, 3000)
                }
                APIConstants.UNAUTHORIZED_CODE -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
                else -> {
                    activity?.let { activity ->
                        DialogUtils.showErrorDialog(
                            activity,
                            buttonText = resourceProvider.getString(R.string.ok),
                            message = it.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )
                    }
                }
            }
        }
        addMomentViewModel.momentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is MomentResponseInfo.MomentList -> {
                        momentDetail = response.momentList.firstOrNull()
                        updateKidDetail(kidsAdapter.currentList)
                        updateMomentDetail()
                    }
                    is MomentResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                }
            }
        }
        setFragmentResultListener(APIConstants.ADD_KID) { _, bundle ->
            if (bundle.getBoolean(APIConstants.SUCCESS))
                getKidsList()
        }
    }

    private fun updateKidDetail(userList: List<KidsResponseModel>) {
        momentDetail?.kidId?.forEach { momentKidsModel ->
            userList.apply {
                this.find { apiKidModel ->
                    momentKidsModel._id == apiKidModel._id
                }?.let {
                    it.isSelected = true
                }
            }
        }
        addMomentViewModel.selectedKidList.forEach { selectedKid ->
            userList.find {
                it._id == selectedKid
            }?.let {
                it.isSelected = true
            }
        }
        kidsAdapter.submitList(userList)
    }

    private fun removeImageFromImageList(imageModel: MomentMediaModel) {
        addMomentViewModel.momentImageList.remove(imageModel)
        allTextUrlsList.remove(imageModel.text)
        refreshAdapter(true)
    }

    private fun addImageToImageList(imageModel: MomentMediaModel) {
        addMomentViewModel.momentImageList.add(imageModel)
        refreshAdapter(scrollToFirstItem = false)
    }

    private fun refreshAdapter(scrollToFirstItem: Boolean = false) {
        val imageViewAdapter = ImageSliderAdapter(
            isAddMoment = true,
            resourceProvider = resourceProvider,
            imageList = addMomentViewModel.momentImageList,
            context = binding.headerImageViewPager.context,
            onDeleteClick = {
                DialogUtils.showDialogWithCallBack(
                    requireContext(),
                    message = resourceProvider.getString(R.string.delete_message),
                    title = resourceProvider.getString(R.string.delete), positiveButtonText = resourceProvider.getString(R.string.yes),
                    negativeButtonText = resourceProvider.getString(R.string.cancel),
                    positiveButtonCallback = {
                        removeImageFromImageList(imageModel = it)
                    }
                )
            },
            onMediaClick = { type: String, url: String, isLocal: Boolean ->
                if (type == MediaType.IMAGE.name || type == MediaType.OG_IMAGE.name) {
                    findNavController().navigate(AddMomentFragmentDirections.actionUserImageDialog(url, isLocal = isLocal))
                } else if (type == MediaType.VIDEO.name) {
                    findNavController().navigate(AddMomentFragmentDirections.actionVideoPlayerFragment(isLocal = isLocal, url = url))
                }
            }
        )
        binding.headerImageViewPager.adapter = imageViewAdapter
        if (addMomentViewModel.momentImageList.size > 1) {
            binding.headerImageViewPager.offscreenPageLimit = addMomentViewModel.momentImageList.size
            binding.headerImageViewPager.clipToPadding = false
            binding.headerImageViewPager.setPadding(0, 0, resourceProvider.getDimension(R.dimen.margin_40).toInt(), 0)
            binding.headerImageViewPager.pageMargin = resourceProvider.getDimension(R.dimen.margin_10).toInt()

        } else {
            binding.headerImageViewPager.setPadding(0, 0, 0, 0)
            binding.headerImageViewPager.pageMargin = 0
        }
        if (addMomentViewModel.momentImageList.isEmpty()) {
            binding.headerImageViewPager.visibility = View.GONE
            binding.dummyView.visibility = View.VISIBLE
        } else {
            binding.dummyView.visibility = View.GONE
            binding.headerImageViewPager.visibility = View.VISIBLE
        }
        val dots = arrayOfNulls<ImageView>(addMomentViewModel.momentImageList.size)
        binding.sliderDots.removeAllViews()
        if (dots.isNotEmpty() && dots.size > 1) {
            buildViewPagerSlidingDotPanels(binding.sliderDots, dots)
            setViewPagerPageChangeListener(binding.headerImageViewPager, dots)
        }
        val scrollPosition = if (scrollToFirstItem) 0 else addMomentViewModel.momentImageList.size
        binding.headerImageViewPager.setCurrentItem(scrollPosition, true)
    }


    private fun showBottomMediaView() {
        binding.scrollView.post {
            binding.space.layoutParams.height = (Resources.getSystem().displayMetrics.heightPixels.div(3.5).toInt())
            binding.space.visibility = View.VISIBLE
            binding.mediaContainer.visibility = View.VISIBLE
            binding.addAPhotoVideoButton.root.visibility = View.GONE
            binding.takeAPhotoVideoButton.root.visibility = View.GONE
            binding.markAsImportantMomentButton.root.visibility = View.GONE
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun hideBottomMediaView() {
        binding.space.visibility = View.GONE
        binding.mediaContainer.visibility = View.GONE
        binding.addAPhotoVideoButton.root.visibility = View.VISIBLE
        binding.takeAPhotoVideoButton.root.visibility = View.VISIBLE
        binding.markAsImportantMomentButton.root.visibility = View.VISIBLE
    }

    private fun buildViewPagerSlidingDotPanels(
        sliderDotsPanel: LinearLayout,
        dots: Array<ImageView?>
    ) {
        for (i in dots.indices) {
            dots[i] = ImageView(requireContext())
            dots[i]?.background = ContextCompat.getDrawable(requireContext(), R.drawable.view_bordered_ripple)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.slider_non_active_dot
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            dots[i]?.setPadding(4, 12, 4, 8)
            sliderDotsPanel.addView(dots[i], params)
        }
        dots[0]?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.slider_active_dot
            )
        )
    }

    private fun setViewPagerPageChangeListener(
        productImageViewPager: ViewPager,
        dots: Array<ImageView?>
    ) {
        productImageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                for (imageView in dots) {
                    imageView?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.slider_non_active_dot
                        )
                    )
                }
                dots.getOrNull(position)?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.slider_active_dot
                    )
                )
            }
        })
    }

    private fun updateMomentDetail() {
        binding.header.pageTitle.text = resourceProvider.getString(R.string.edit_moment)
        momentDetail?.mediaContent?.forEach {
            addMomentViewModel.momentImageList.add(MomentMediaModel(original = it.original, type = it.type, thumbnail = it.thumbnail))
        }
        binding.dateButton.text = momentDetail?.momentDate?.convertISODateIntoAppDateFormat()
        binding.saySomethingAboutThisMomentInput.setText(momentDetail?.description.orEmpty())
        isImportant = momentDetail?.isImportant ?: false
        updateImportantDetail()
        refreshAdapter(scrollToFirstItem = true)
    }

    private fun addImageOrVideo() {
        val popUp = PopupMenu(binding.addAPhotoVideoButton.container.context, binding.addAPhotoVideoButton.container)
        popUp.menuInflater.inflate(R.menu.file_type_menu, popUp.menu)
        popUp.show()
        popUp.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.actionImage -> selectImage(false)
                R.id.actionVideo -> selectVideo(false)
            }
            true
        }
    }

    private fun takeImageOrVideo() {
        val popUp = PopupMenu(binding.takeAPhotoVideoButton.container.context, binding.takeAPhotoVideoButton.container)
        popUp.menuInflater.inflate(R.menu.file_type_menu, popUp.menu)
        popUp.show()
        popUp.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.actionImage -> selectImage(true)
                R.id.actionVideo -> selectVideo(true)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.VISIBLE)
        val view = requireActivity().getRootView()
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    override fun onPause() {
        super.onPause()
        val view = requireActivity().getRootView()
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        momentDetail = null
    }

    private val listener = ViewTreeObserver.OnGlobalLayoutListener {
        if (isKeyboardOpen()) {
            showBottomMediaView()
        } else {
            hideBottomMediaView()
        }
    }

}