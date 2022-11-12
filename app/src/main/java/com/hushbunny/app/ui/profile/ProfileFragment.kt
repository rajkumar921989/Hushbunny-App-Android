package com.hushbunny.app.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.hushbunny.app.R
import com.hushbunny.app.core.HomeSharedViewModel
import com.hushbunny.app.databinding.FragmentProfileBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.*
import com.hushbunny.app.ui.moment.AddMomentViewModel
import com.hushbunny.app.ui.moment.MomentAdapter
import com.hushbunny.app.ui.home.HomeViewModel
import com.hushbunny.app.ui.home.KidsAdapter
import com.hushbunny.app.ui.model.FilterModel
import com.hushbunny.app.ui.model.MomentListingModel
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.ui.sealedclass.*
import com.hushbunny.app.ui.setting.SettingActionDialog
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.DateFormatUtils.getAge
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL
import com.hushbunny.app.uitls.dialog.DialogUtils
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var kidsAdapter: KidsAdapter
    private lateinit var momentAdapter: MomentAdapter
    private val navigationArgs: ProfileFragmentArgs by navArgs()
    private var momentList = ArrayList<MomentListingModel>()
    private var isLoading = true
    var currentPage = 1
    private lateinit var imageFile: File
    private lateinit var lastCapturedImageUri: Uri
    private var filterModel: FilterModel? = null
    private var isFilterAPICalled = false
    var isFilterApplied = false

    @Inject
    lateinit var homeRepository: HomeRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var momentRepository: MomentRepository

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var userActionRepository: UserActionRepository

    private val homeSharedViewModel: HomeSharedViewModel by viewModelBuilderActivityScope {
        HomeSharedViewModel()
    }

    private val homeViewModel: HomeViewModel by viewModelBuilderFragmentScope {
        HomeViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            homeRepository = homeRepository,
            fileUploadRepository = fileUploadRepository
        )
    }

    private val momentViewModel: AddMomentViewModel by viewModelBuilderFragmentScope {
        AddMomentViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            momentRepository = momentRepository,
            fileUploadRepository = fileUploadRepository
        )
    }

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
        _binding = FragmentProfileBinding.bind(view)
        isFilterAPICalled = false
        isFilterApplied = false
        currentPage = 1
        momentList.clear()
        initClickListener()
        initView()
        getKidsList()
        getMomentList(true)
        setAdapter()
        setObserver()
    }

    private fun initView() {
        binding.profileContainer.nameText.text = AppConstants.getUserName()
        binding.profileContainer.associateWithText.text = setAssociate()
        binding.emptyViewContainer.welcomeMessageText.text =
            resourceProvider.getString(R.string.home_page_welcome_message, AppConstants.getUserFirstName())
        binding.profileContainer.countryValueText.text = AppConstants.getCountryNameByCode(PrefsManager.get().getString(AppConstants.USER_COUNTRY, ""))
        binding.profileContainer.ageCountText.text = PrefsManager.get().getString(AppConstants.USER_DATE_OF_BIRTH, "").getAge()
        binding.backImage.run {
            visibility = if(navigationArgs.isBackArrowEnabled) View.VISIBLE else View.GONE
            setOnClickListener {
                findNavController().popBackStack()
            }
        }
        loadUserImage()
    }

    private fun setAssociate(): String {
        val associate = PrefsManager.get().getString(AppConstants.USER_ASSOCIATE_AS, "")
        return if (associate.equals("Father", true)) {
            "(Father)"
        } else {
            "(Mother)"
        }
    }

    private fun initClickListener() {
        binding.pullRefresh.setOnRefreshListener {
            onPullToRefreshCalled()
        }
        binding.emptyViewContainer.addKidButton.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionAddKidFragment(isEditKid = false))
        }
        binding.emptyViewContainer.addMomentButton.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionAddMomentFragment())
        }
        binding.profileContainer.profilePictureContainer.setOnClickListener {
            requestCameraPermission()
        }
        binding.editImage.setOnClickListener {
            requestCameraPermission()
        }
        binding.profileContainer.editProfileButton.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionEditProfileFragment())
        }
        binding.filterButton.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionFilterFragment(filterModel = filterModel))
        }
        binding.scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val layoutManager = binding.momentList.layoutManager as LinearLayoutManager
            val totalItemCount = layoutManager.itemCount - 1
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            if (!isLoading && totalItemCount == lastVisibleItem) {
                ++currentPage
                getMomentList(false)
            }
        }
    }

    private fun onPullToRefreshCalled() {
        currentPage = 1
        momentList.clear()
        initView()
        getKidsList()
        getMomentList(true)
        binding.pullRefresh.isRefreshing = false
        homeSharedViewModel.refreshNotificationUnReadCount()
    }

    private fun getKidsList() {
        binding.kidsShimmerContainer.visibility = View.VISIBLE
        binding.kidsList.visibility = View.GONE
        momentViewModel.getKidsList()
    }

    private fun getMomentList(isRequiredShimmer: Boolean) {
        if (isRequiredShimmer) {
            binding.momentsShimmerContainer.visibility = View.VISIBLE
            binding.momentGroup.visibility = View.GONE
        }
        isLoading = true
        isFilterApplied = if (filterModel?.type == FilterType.NO_SELECTION.name) false else filterModel?.type.orEmpty().isNotEmpty()
        if (isFilterApplied) setFilterData()
        momentViewModel.getMomentList(
            currentPage,
            MomentType.MINE.name,
            startDate = filterModel?.fromDate,
            endDate = filterModel?.toDate,
            date = filterModel?.date,
            filterType = filterModel?.type,
            sortBy = MomentDateType.CREATED_DATE.name
        )
    }

    private fun setAdapter() {
        kidsAdapter = KidsAdapter(resourceProvider = resourceProvider, addKidsClick = {
            findNavController().navigate(ProfileFragmentDirections.actionAddKidFragment(isEditKid = false))
        }, kidsClick = {
            findNavController().navigate(ProfileFragmentDirections.actionKidsProfileFragment(kidId = it._id.orEmpty()))
        }, addSpouseClick = {
            findNavController().navigate(ProfileFragmentDirections.actionInviteSpouseFragment(kidId = it))
        })
        binding.kidsList.adapter = kidsAdapter
        momentAdapter =
            MomentAdapter(resourceProvider = resourceProvider, onItemClick = { view: View, position: Int, type: String, item: MomentListingModel ->
                when (type) {
                    resourceProvider.getString(R.string.bookmarks) -> {
                        if (item.isBookmarked == true) {
                            DialogUtils.showDialogWithCallBack(
                                requireContext(),
                                message = resourceProvider.getString(R.string.delete_from_bookmark),
                                title = resourceProvider.getString(R.string.bookmarks), positiveButtonText = resourceProvider.getString(R.string.yes),
                                negativeButtonText = resourceProvider.getString(R.string.cancel),
                                positiveButtonCallback = {
                                    callBookMarkAPI(position = position, momentId = item._id.orEmpty())
                                }
                            )
                        } else {
                            callBookMarkAPI(position = position, momentId = item._id.orEmpty())
                        }
                    }
                    resourceProvider.getString(R.string.comments) -> {
                        findNavController().navigate(ProfileFragmentDirections.actionCommentFragment(momentID = item._id.orEmpty()))
                    }
                    resourceProvider.getString(R.string.share) -> {
                        momentViewModel.shareMoment(item._id.orEmpty())
                        AppConstants.shareTheAPP(
                            requireActivity(),
                            title = resourceProvider.getString(R.string.share),
                            extraMessage = item.description.orEmpty(),
                            appUrl = item.shortLink.orEmpty()
                        )
                    }
                    resourceProvider.getString(R.string.like) -> {
                        findNavController().navigate(ProfileFragmentDirections.actionReactionListFragment(momentID = item._id.orEmpty()))
                    }
                    AppConstants.MOMENT_EDIT -> {
                        findNavController().navigate(ProfileFragmentDirections.actionAddMomentFragment(isEdit = true, momentID = item._id.orEmpty()))
                    }
                    AppConstants.DELETE_MOMENT -> {
                        val dialog = SettingActionDialog(requireContext(), resourceProvider.getString(R.string.delete_moment)) {
                            binding.progressIndicator.showProgressbar()
                            momentViewModel.deleteMoment(position = position, momentId = item._id.orEmpty())
                        }
                        dialog.show()
                    }
                    AppConstants.ADD_YOUR_KID -> {
                        findNavController().navigate(
                            ProfileFragmentDirections.actionAddMomentFragment(
                                isSpouseAdded = true,
                                momentID = item._id.orEmpty()
                            )
                        )
                    }
                    AppConstants.IMPORTANT_MOMENT -> {
                        binding.progressIndicator.showProgressbar()
                        momentViewModel.markMomentAsImportant(position = position, momentId = item._id.orEmpty())
                    }
                    AppConstants.MOMENT_REPORT -> {
                        findNavController().navigate(
                            ProfileFragmentDirections.actionReportFragment(
                                type = ReportType.MOMENT.name,
                                momentId = item._id.orEmpty()
                            )
                        )
                    }
                    AppConstants.COPY_URL -> {
                        AppConstants.copyURL(activity, item.shortLink)
                    }
                    ReactionPageName.LAUGH.name, ReactionPageName.SAD.name, ReactionPageName.HEART.name -> {
                        binding.progressIndicator.showProgressbar()
                        momentViewModel.addReaction(position = position, emojiType = type, momentId = item._id.toString())
                    }
                }
            }, onCommentClick = { position: Int, type: String, commentId: String ->
                when (type) {
                    AppConstants.COMMENT_REPORT -> {
                        findNavController().navigate(
                            ProfileFragmentDirections.actionReportFragment(
                                type = ReportType.COMMENT.name,
                                commentId = commentId
                            )
                        )
                    }
                    AppConstants.COMMENT_DELETE -> {
                        binding.progressIndicator.showProgressbar()
                        momentViewModel.deleteComment(position = position, commentId = commentId)
                    }
                }
            }, onMediaClick = { type: String, url: String ->
                    if (type == MediaType.IMAGE.name || type == MediaType.OG_IMAGE.name) {
                        findNavController().navigate(ProfileFragmentDirections.actionUserImageDialog(url))
                    } else if (type == MediaType.VIDEO.name) {
                        findNavController().navigate(ProfileFragmentDirections.actionVideoPlayerFragment(isLocal = false, url = url))
                    }
                }
            )
        binding.momentList.adapter = momentAdapter
    }

    private fun callBookMarkAPI(position: Int, momentId: String) {
        binding.progressIndicator.showProgressbar()
        momentViewModel.bookMarkMoment(position = position, momentId = momentId)
    }

    private fun setObserver() {
        setFragmentResultListener(APIConstants.IS_FILTER_APPLIED) { _, bundle ->
            filterModel = bundle.getSerializable(APIConstants.FILTER_MODEL) as FilterModel?
            isFilterAPICalled = true
            currentPage = 1
            momentList.clear()
            setFilterData()
            momentViewModel.cancelCurrentJob()
            getMomentList(true)
        }
        momentViewModel.kidsListObserver.observe(viewLifecycleOwner) {
            binding.kidsShimmerContainer.visibility = View.GONE
            when (it) {
                is KidsStatusInfo.UserList -> {
                    binding.kidsList.visibility = View.VISIBLE
                    kidsAdapter.submitList(it.userList)
                }
                else -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
            }
        }
        homeViewModel.fileUploadObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        editProfileViewModel.updateProfilePicture(image = response.data?.url.orEmpty(), userType = ReportType.USER.name)
                    }
                    APIConstants.UNAUTHORIZED_CODE -> {
                        binding.progressIndicator.hideProgressbar()
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
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
        editProfileViewModel.editProfileObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.progressIndicator.hideProgressbar()
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        AppConstants.saveUserDetail(response.data)
                        loadUserImage()
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
        momentViewModel.momentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is MomentResponseInfo.MomentList -> {
                        if(currentPage == 1)  momentList.clear()
                        if (isFilterAPICalled) {
                            momentList.clear()
                            isFilterAPICalled = false
                        } else {
                            PrefsManager.get().saveStringValue(AppConstants.USER_MOMENT_COUNT, response.count)
                            binding.profileContainer.totalMomentCountText.text = response.count.prependZeroToStringIfSingleDigit()
                        }
                        isLoading = response.momentList.size < 30
                        momentList.addAll(response.momentList)
                        setTotalMomentCount(count = response.count.toIntOrZero())
                        loadMomentList()
                    }
                    is MomentResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                    else -> {
                        isLoading = true
                        loadMomentList()
                    }
                }
            }
        }
        momentViewModel.bookMarkObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        response.bookMark?.let { it1 -> momentAdapter.updateMomentDetail(position = response.position, model = it1) }
                    }
                    is BookMarkResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                    is BookMarkResponseInfo.BookMarkFailure -> {
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
        momentViewModel.reactionObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        response.bookMark?.let { it1 -> momentAdapter.updateMomentDetail(position = response.position, model = it1) }
                    }
                    is BookMarkResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                    is BookMarkResponseInfo.BookMarkFailure -> {
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
        momentViewModel.markAsImportantMomentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        response.bookMark?.let { it1 -> momentAdapter.updateMomentDetail(position = response.position, model = it1) }
                    }
                    is BookMarkResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                    is BookMarkResponseInfo.BookMarkFailure -> {
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
        momentViewModel.deleteMomentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it) {
                is MomentDeletedResponseInfo.MomentDelete -> {
                    momentAdapter.updateDeletedMoment(it.position)
                }
                is MomentDeletedResponseInfo.ApiError -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
                is MomentDeletedResponseInfo.HaveError -> {
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it.message,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        momentViewModel.deleteCommentObserver.observe(viewLifecycleOwner) {
            when (it) {
                is CommentDeletedResponseInfo.CommentDelete -> {
                    currentPage = 1
                    momentList.clear()
                    getMomentList(true)
                }
                is CommentDeletedResponseInfo.ApiError -> {
                    binding.progressIndicator.hideProgressbar()
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
                is CommentDeletedResponseInfo.HaveError -> {
                    binding.progressIndicator.hideProgressbar()
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it.message,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        homeSharedViewModel.profileTabClickedTabClickedObserver.observe(viewLifecycleOwner) {
            setMomentListScrollBehaviour()
        }
    }

    private fun setMomentListScrollBehaviour() {
        binding.kidsList.smoothScrollToPosition(0)
        val isFirstItem = binding.scrollView.scrollY == 0
        if (isFirstItem) {
            binding.pullRefresh.run {
                post {
                    this.isRefreshing = true
                    onPullToRefreshCalled()
                }
            }
        } else {
            binding.scrollView.smoothScrollTo(0, 0)
        }
    }

    private fun setFilterData() {
        when (filterModel?.type) {
            FilterType.DATE.name, FilterType.MONTH.name, FilterType.YEAR.name -> {
                binding.filterButton.text = filterModel?.date
            }
            FilterType.DATE_RANGE.name, FilterType.MONTH_RANGE.name, FilterType.YEAR_RANGE.name -> {
                binding.filterButton.text = "${filterModel?.fromDate} - ${filterModel?.toDate}"
            }
            else -> {
                binding.filterButton.text = resourceProvider.getString(R.string.filter)
            }
        }
    }

    private fun setTotalMomentCount(count: Int?) {
        binding.profileContainer.totalMomentCountText.text = count.toString().prependZeroToStringIfSingleDigit()
        momentAdapter.setTotalMomentCount(count)
    }

    private fun loadMomentList() {
        binding.momentsShimmerContainer.visibility = View.GONE
        if (isFilterApplied && momentList.isEmpty()) {
            binding.momentGroup.visibility = View.VISIBLE
            momentAdapter.submitList(momentList.toList())
            binding.noMomentFound.visibility = View.VISIBLE
        } else if (momentList.isNotEmpty()) {
            binding.noMomentFound.visibility = View.GONE
            binding.emptyViewContainer.container.visibility = View.GONE
            binding.momentGroup.visibility = View.VISIBLE
            momentAdapter.submitList(momentList.toList())
        } else {
            binding.noMomentFound.visibility = View.GONE
            binding.momentGroup.visibility = View.GONE
            binding.emptyViewContainer.container.visibility = View.VISIBLE
        }
    }

    private fun loadUserImage() {
        val imageUrl = AppConstants.getUserImage()
        if (imageUrl.isNotEmpty()) {
            binding.profileContainer.defaultImage.visibility = View.GONE
            binding.profileContainer.userImage.visibility = View.VISIBLE
            binding.profileContainer.userImage.loadImageFromURL(
                imageUrl.replace(
                    APIConstants.IMAGE_BASE_URL,
                    ""
                )
            )
        } else {
            binding.profileContainer.defaultImage.visibility = View.VISIBLE
            binding.profileContainer.userImage.visibility = View.GONE
        }
    }

    private fun requestCameraPermission() {
        if (PermissionUtility.userGrantedCameraPermission(requireActivity())) {
            if (AppConstants.getUserImage().isNotEmpty()) chooseOptionForUserHavingImage() else chooseOption()
        } else {
            PermissionUtility.requestCameraPermission(requestPermission)
        }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            if (AppConstants.getUserImage().isNotEmpty()) chooseOptionForUserHavingImage() else chooseOption()
        } else {
            PermissionUtility.showPermissionRationaleToast(
                requireContext(),
                message = resourceProvider.getString(R.string.camera_permission_rationale)
            )
        }
    }

    private fun openCamera(isCamera: Boolean) {
        try {
            imageFile = FileUtils.getImageFilePath(requireContext())
            lastCapturedImageUri = FileProvider.getUriForFile(requireContext(), FileUtils.getAuthorities(requireContext()), imageFile)
            val pickIntent = Intent()
            pickIntent.type = FileUtils.INTENT_TYPE_IMAGE
            pickIntent.action = Intent.ACTION_GET_CONTENT
            val intent = if (isCamera) Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, lastCapturedImageUri)
            else Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            cameraIntentLauncher.launch(intent)
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
            }
            if (imageFile.exists()) {
                lifecycleScope.launch {
                    val compressedImageFile = Compressor.compress(requireContext(), imageFile) {
                        default(
                            width = resourceProvider.getDimension(R.dimen.view_100).toInt(),
                            height = resourceProvider.getDimension(R.dimen.view_100).toInt(),
                            format = Bitmap.CompressFormat.JPEG,
                            quality = 50
                        )
                    }
                    binding.progressIndicator.showProgressbar()
                    homeViewModel.uploadFile(if(compressedImageFile.exists()) compressedImageFile else imageFile)
                }

            }
        }
    }

    private fun chooseOption() {
        val popUp = PopupMenu(binding.profileContainer.profilePictureContainer.context, binding.profileContainer.profilePictureContainer)
        popUp.menuInflater.inflate(R.menu.file_menu, popUp.menu)
        popUp.show()
        popUp.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.actionCamera -> openCamera(true)
                R.id.actionGallery -> openCamera(false)
            }
            true
        }
    }

    private fun chooseOptionForUserHavingImage() {
        val popUp = PopupMenu(binding.profileContainer.profilePictureContainer.context, binding.profileContainer.profilePictureContainer)
        popUp.menuInflater.inflate(R.menu.user_image_menu, popUp.menu)
        popUp.show()
        popUp.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.actionEditImage -> chooseOption()
                R.id.actionViewImage -> {
                    findNavController().navigate(ProfileFragmentDirections.actionUserImageDialog(AppConstants.getUserImage()))
                }
                R.id.actionRemoveImage -> {
                    binding.progressIndicator.showProgressbar()
                    editProfileViewModel.updateProfilePicture(image = "", userType = ReportType.USER.name)
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}