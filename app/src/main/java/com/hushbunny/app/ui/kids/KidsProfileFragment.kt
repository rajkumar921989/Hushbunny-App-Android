package com.hushbunny.app.ui.kids

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentKidsProfileBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.*
import com.hushbunny.app.ui.home.HomeViewModel
import com.hushbunny.app.ui.model.FilterModel
import com.hushbunny.app.ui.model.InviteInfoModel
import com.hushbunny.app.ui.model.KidsResponseModel
import com.hushbunny.app.ui.model.MomentListingModel
import com.hushbunny.app.ui.moment.AddMomentViewModel
import com.hushbunny.app.ui.moment.MomentAdapter
import com.hushbunny.app.ui.profile.EditProfileViewModel
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.ui.sealedclass.BookMarkResponseInfo
import com.hushbunny.app.ui.sealedclass.CommentDeletedResponseInfo
import com.hushbunny.app.ui.sealedclass.MomentDeletedResponseInfo
import com.hushbunny.app.ui.sealedclass.MomentResponseInfo
import com.hushbunny.app.ui.setting.SettingActionDialog
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.DateFormatUtils.getAge
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.dialog.SuccessDialog
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class KidsProfileFragment : Fragment(R.layout.fragment_kids_profile) {
    private var _binding: FragmentKidsProfileBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: KidsProfileFragmentArgs by navArgs()
    private var momentList = ArrayList<MomentListingModel>()
    private var isLoading = true
    var currentPage = 1
    private lateinit var momentAdapter: MomentAdapter
    private lateinit var imageFile: File
    private lateinit var lastCapturedImageUri: Uri
    private var filterModel: FilterModel? = null
    private var isFilterAPICalled = false
    var isFilterApplied = false
    var kidByIdResponseModel: KidsResponseModel? = null

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var homeRepository: HomeRepository

    @Inject
    lateinit var momentRepository: MomentRepository

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var userActionRepository: UserActionRepository

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
        _binding = FragmentKidsProfileBinding.bind(view)
        isFilterAPICalled = false
        isFilterApplied = false
        currentPage = 1
        momentList.clear()
        setProfileMomentCount(0)
        initializeClickListener()
        getKidDetail()
        setObserver()
        setAdapter()
        getMomentList(true)
    }

    private fun onPullToRefreshCalled() {
        currentPage = 1
        momentList.clear()
        getKidDetail()
        getMomentList(true)
        binding.pullRefresh.isRefreshing = false
    }

    private fun getKidDetail() {
        binding.profileContainer.visibility = View.GONE
        binding.profileShimmerContainer.visibility = View.VISIBLE
        momentViewModel.getKidDetailById(navigationArgs.kidId)
    }

    private fun setAdapter() {
        momentAdapter =
            MomentAdapter(
                isKidsProfile = true,
                resourceProvider = resourceProvider,
                onItemClick = { view: View, position: Int, type: String, item: MomentListingModel ->
                    when (type) {
                        resourceProvider.getString(R.string.bookmarks) -> {
                            if (item.isBookmarked == true) {
                                DialogUtils.showDialogWithCallBack(
                                    requireContext(),
                                    message = resourceProvider.getString(R.string.delete_from_bookmark),
                                    title = resourceProvider.getString(R.string.bookmarks),
                                    positiveButtonText = resourceProvider.getString(R.string.yes),
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
                            val parentOne = item.parents?.firstOrNull()?._id.orEmpty()
                            val parentTwo = item.parents?.lastOrNull()?._id.orEmpty()
                            findNavController().navigate(KidsProfileFragmentDirections.actionCommentFragment(momentID = item._id.orEmpty(), parentOneId = parentOne, parentTwoId = parentTwo))
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
                            findNavController().navigate(KidsProfileFragmentDirections.actionReactionListFragment(momentID = item._id.orEmpty()))
                        }
                        resourceProvider.getString(R.string.user_detail) -> {
                            if (item.addedBy?._id.orEmpty() == AppConstants.getUserID())
                                findNavController().navigate(KidsProfileFragmentDirections.actionProfileFragment())
                            else findNavController().navigate(KidsProfileFragmentDirections.actionOtherUserProfileFragment(userID = item.addedBy?._id.orEmpty()))
                        }
                        AppConstants.MOMENT_EDIT -> {
                            findNavController().navigate(
                                KidsProfileFragmentDirections.actionAddMomentFragment(
                                    isEdit = true,
                                    momentID = item._id.orEmpty()
                                )
                            )
                        }
                        AppConstants.IMPORTANT_MOMENT -> {
                            //binding.progressIndicator.showProgressbar()
                            momentViewModel.markMomentAsImportant(position = position, momentId = item._id.orEmpty())
                        }
                        AppConstants.DELETE_MOMENT -> {
                            val dialog = SettingActionDialog(requireContext(), resourceProvider.getString(R.string.delete_moment)) {
                                binding.progressIndicator.showProgressbar()
                                momentViewModel.deleteMoment(position = position, momentId = item._id.orEmpty())
                            }
                            dialog.show()
                        }
                        AppConstants.MOMENT_REPORT -> {
                            findNavController().navigate(
                                KidsProfileFragmentDirections.actionReportFragment(
                                    type = ReportType.MOMENT.name,
                                    momentId = item._id.orEmpty()
                                )
                            )
                        }
                        ReactionPageName.LAUGH.name, ReactionPageName.SAD.name, ReactionPageName.HEART.name -> {
                           // binding.progressIndicator.showProgressbar()
                            momentViewModel.addReaction(position = position, emojiType = type, momentId = item._id.toString())
                        }
                    }
                },
                onKidClick = { _, kidsModel ->
                    val loggedInUserId = AppConstants.getUserID()
                    val kidParents = kidsModel.parents.orEmpty()
                    val isParentFound = kidParents.contains(loggedInUserId)
                    if (isParentFound) {
                        findNavController().navigate(
                            KidsProfileFragmentDirections.actionKidsProfileFragment(
                                kidId = kidsModel._id.orEmpty()
                            )
                        )
                    }
                },
                onCommentClick = { position: Int, type: String, commentId: String, item: MomentListingModel ->
                    when (type) {
                        AppConstants.COMMENT_REPORT -> {
                            findNavController().navigate(
                                KidsProfileFragmentDirections.actionReportFragment(
                                    type = ReportType.COMMENT.name,
                                    commentId = commentId
                                )
                            )
                        }
                        AppConstants.COMMENT_DELETE -> {
                            binding.progressIndicator.showProgressbar()
                            momentViewModel.deleteComment(position = position, commentId = commentId)
                        }
                        AppConstants.USER_PROFILE -> {
                            if (commentId == AppConstants.getUserID()) {
                                findNavController().navigate(KidsProfileFragmentDirections.actionProfileFragment())
                            } else {
                                val isOtherParent = item.parents?.any { it._id == commentId } ?: false
                                findNavController().navigate(KidsProfileFragmentDirections.actionOtherUserProfileFragment(userID = commentId, isOtherParent = isOtherParent))
                            }
                        }
                    }

                },
                onMediaClick = { type: String, url: String ->
                    if (type == MediaType.IMAGE.name || type == MediaType.OG_IMAGE.name) {
                        findNavController().navigate(KidsProfileFragmentDirections.actionUserImageDialog(url))
                    } else if (type == MediaType.VIDEO.name) {
                        findNavController().navigate(KidsProfileFragmentDirections.actionVideoPlayerFragment(isLocal = false, url = url))
                    }

                })
        binding.momentList.adapter = momentAdapter
    }

    private fun callBookMarkAPI(position: Int, momentId: String) {
       // binding.progressIndicator.showProgressbar()
        momentViewModel.bookMarkMoment(position = position, momentId = momentId)
    }

    private fun setFilterData() {
        when (filterModel?.type) {
            FilterType.DATE.name, FilterType.MONTH.name, FilterType.YEAR.name -> {
                binding.filterButton.text = filterModel?.date
                showFilterReset(isVisible = true)
            }
            FilterType.DATE_RANGE.name, FilterType.MONTH_RANGE.name, FilterType.YEAR_RANGE.name -> {
                binding.filterButton.text = "${filterModel?.fromDate} - ${filterModel?.toDate}"
                showFilterReset(isVisible = true)
            }
            else -> {
                binding.filterButton.text = resourceProvider.getString(R.string.filter)
                showFilterReset(isVisible = false)
            }
        }
    }

    private fun showFilterReset(isVisible: Boolean) {
        binding.filterReset.visibility = if(isVisible) View.VISIBLE else View.GONE
    }

    private fun loadFilterData() {
        isFilterAPICalled = true
        currentPage = 1
        momentList.clear()
        setFilterData()
        momentViewModel.cancelCurrentJob()
        getMomentList(true)
    }

    private fun setObserver() {
        setFragmentResultListener(APIConstants.IS_FILTER_APPLIED) { _, bundle ->
            filterModel = bundle.getSerializable(APIConstants.FILTER_MODEL) as FilterModel?
            loadFilterData()
        }
        setFragmentResultListener(APIConstants.IS_SPOUSE_INVITED) { _, bundle ->
            if (bundle.getBoolean(APIConstants.SUCCESS)) {
                val inviteInfo = InviteInfoModel(status = "PENDING")
                kidByIdResponseModel?.inviteInfo = inviteInfo
                loadSpouseDetail()
            }
        }
        momentViewModel.kidDetailResponseObserver.observe(viewLifecycleOwner) {
            binding.profileContainer.visibility = View.VISIBLE
            binding.profileShimmerContainer.visibility = View.GONE
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    kidByIdResponseModel = it.data
                    updateKidDetail()
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
                        message = it.message,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
            }
        }
        homeViewModel.fileUploadObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        editProfileViewModel.updateProfilePicture(
                            image = response.data?.url.orEmpty(),
                            userType = ReportType.KID.name,
                            kidId = navigationArgs.kidId
                        )
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
        homeViewModel.addKidsObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    kidByIdResponseModel = it.data
                    loadKidImage(it.data?.image.orEmpty())
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
        momentViewModel.momentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is MomentResponseInfo.MomentList -> {
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
                        setProfileMomentCount(0)
                        if (!isFilterAPICalled) loadMomentList()
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
        editProfileViewModel.editProfileObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.progressIndicator.hideProgressbar()
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        loadKidImage(response.data?.image.orEmpty())
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
        editProfileViewModel.resendInviteObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.progressIndicator.hideProgressbar()
                when (response.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        val dialog = SuccessDialog(requireContext())
                        dialog.show()
                        dialog.setMessage(resourceProvider.getString(R.string.resend_invite_successfully))
                        Handler(Looper.getMainLooper()).postDelayed({
                            dialog.dismiss()
                        }, 2000)
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

    private fun setProfileMomentCount(count: Int?) {
        binding.momentCountText.text = count.toString().prependZeroToStringIfSingleDigit()
    }

    private fun setTotalMomentCount(count: Int?) {
        setProfileMomentCount(count)
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
            binding.emptyViewContainer.visibility = View.GONE
            binding.momentGroup.visibility = View.VISIBLE
            momentAdapter.submitList(momentList.toList())
        } else {
            binding.noMomentFound.visibility = View.GONE
            binding.momentGroup.visibility = View.GONE
            binding.emptyViewContainer.visibility = View.VISIBLE
        }
    }

    private fun updateKidDetail() {
        binding.nameText.text = kidByIdResponseModel?.name.orEmpty()
        binding.nickNameText.text = if (kidByIdResponseModel?.nickName.orEmpty().isNotEmpty()) "(${kidByIdResponseModel?.nickName.orEmpty()})" else ""
        binding.countryValueText.text = AppConstants.getCountryNameByCode(kidByIdResponseModel?.birthCountryISO2.orEmpty())
        binding.emptyTextMessage.text = resourceProvider.getString(R.string.kids_add_moment_message, kidByIdResponseModel?.firstName.orEmpty())
        if (kidByIdResponseModel?.gender.equals("Yet to born")) {
            binding.ageCountText.text = "00"
        } else {
            binding.ageCountText.text = kidByIdResponseModel?.dob?.getAge()
        }
        loadKidImage(kidByIdResponseModel?.image.orEmpty())
        val parentOneDetail = kidByIdResponseModel?.parents?.firstOrNull()
        binding.parentImageContainer.setOnClickListener {
            navigateToParentDetail(userId = parentOneDetail?._id.orEmpty())
        }
        binding.parentNameText.run {
            text = parentOneDetail?.name.orEmpty()
            setOnClickListener {
                navigateToParentDetail(userId = parentOneDetail?._id.orEmpty())
            }
        }
        binding.parentAssociateText.text = setAssociate(parentOneDetail?.associatedAs.orEmpty())
        loadParentOneImage(parentOneDetail?.image.orEmpty())
        loadSpouseDetail()
    }

    private fun setAssociate(associate: String): String {
        return if (associate.equals("Father", true)) {
            "(Father)"
        } else if (associate.equals("Mother", true)) {
            "(Mother)"
        } else {
            ""
        }
    }

    private fun navigateToParentDetail(userId: String) {
        val loggedInUserId = AppConstants.getUserID()
        if(userId.equals(loggedInUserId, true)) {
            findNavController().navigate(
                KidsProfileFragmentDirections.actionProfileFragment(isBackArrowEnabled = true)
            )
        } else {
            findNavController().navigate(
                KidsProfileFragmentDirections.actionOtherUserProfileFragment(
                    userID = userId,
                    isOtherParent = true
                )
            )
        }
    }

    private fun loadSpouseDetail() {
        if (kidByIdResponseModel?.parents.orEmpty().size > 1) {
            val parentTwoDetail = kidByIdResponseModel?.parents?.lastOrNull()
            binding.spouseNameText.text = parentTwoDetail?.name.orEmpty()
            binding.spouseAssociateText.text = setAssociate(parentTwoDetail?.associatedAs.orEmpty())
            binding.spouseAssociateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            loadSpouseImage(parentTwoDetail?.image.orEmpty())
            binding.spouseImageContainer.setOnClickListener {
                navigateToParentDetail(userId = parentTwoDetail?._id.orEmpty())
            }
            binding.spouseNameText.setOnClickListener {
                navigateToParentDetail(userId = parentTwoDetail?._id.orEmpty())
            }
        } else if (kidByIdResponseModel?.inviteInfo?.status == "PENDING") {
            binding.spouseNameText.text = resourceProvider.getString(R.string.invite_pending)
            binding.spouseAssociateText.text = resourceProvider.getString(R.string.resend_invite)
            binding.spouseAssociateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.resend_invite_color))
            binding.addSpouseImage.visibility = View.VISIBLE
            binding.spouseImage.visibility = View.GONE
            binding.addSpouseImage.setImageResource(R.drawable.ic_pending)
            binding.spouseImageContainer.setOnClickListener {
                navigateToInviteScreen()
            }
            binding.spouseNameText.setOnClickListener {
                navigateToInviteScreen()
            }
            binding.spouseAssociateText.setOnClickListener {
                navigateToInviteScreen()
            }
        } else if (kidByIdResponseModel?.isSpouseAdded == false) {
            binding.spouseNameText.text = resourceProvider.getString(R.string.title_add_spouse)
            binding.addSpouseImage.visibility = View.VISIBLE
            binding.spouseImage.visibility = View.GONE
        }
    }

    private fun navigateToInviteScreen() {
        findNavController().navigate(
            KidsProfileFragmentDirections.actionInviteSpouseFragment(
                kidId = navigationArgs.kidId,
                inviteInfo = kidByIdResponseModel?.inviteInfo
            )
        )
    }

    private fun loadKidImage(uploadedFilePath: String) {
        if (uploadedFilePath.isNotEmpty()) {
            binding.userImage.visibility = View.VISIBLE
            binding.defaultImage.visibility = View.GONE
            binding.userImage.loadImageFromURL(uploadedFilePath.replace(APIConstants.IMAGE_BASE_URL, ""))
        } else {
            binding.userImage.visibility = View.GONE
            binding.defaultImage.visibility = View.VISIBLE
        }
    }

    private fun loadParentOneImage(imagePath: String) {
        if (imagePath.isNotEmpty()) {
            binding.parentImage.loadImageFromURL(imagePath.replace(APIConstants.IMAGE_BASE_URL, ""))
        } else {
            binding.parentImage.setImageResource(R.drawable.ic_no_kid_icon)
        }
    }

    private fun loadSpouseImage(imagePath: String) {
        if (imagePath.isNotEmpty()) {
            binding.addSpouseImage.visibility = View.GONE
            binding.spouseImage.visibility = View.VISIBLE
            binding.spouseImage.loadImageFromURL(imagePath.replace(APIConstants.IMAGE_BASE_URL, ""))
        } else {
            binding.addSpouseImage.visibility = View.VISIBLE
            binding.spouseImage.visibility = View.GONE
            binding.addSpouseImage.setImageResource(R.drawable.ic_no_kid_icon)
        }
    }

    private fun initializeClickListener() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.filterReset.setOnClickListener {
            filterModel = FilterModel(type = FilterType.NO_SELECTION.name)
            loadFilterData()
        }
        binding.pullRefresh.setOnRefreshListener {
            onPullToRefreshCalled()
        }
        binding.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.profilePictureContainer.setOnClickListener {
            requestCameraPermission()
        }
        binding.editImage.setOnClickListener {
            requestCameraPermission()
        }
        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(KidsProfileFragmentDirections.actionAddKidFragment(isEditKid = true, kidsDetail = kidByIdResponseModel))
        }
        binding.spouseImageContainer.setOnClickListener {
            if (kidByIdResponseModel?.isSpouseAdded == false || kidByIdResponseModel?.inviteInfo?.status == "PENDING")
                findNavController().navigate(KidsProfileFragmentDirections.actionInviteSpouseFragment(kidId = navigationArgs.kidId))
        }
        binding.filterButton.setOnClickListener {
            findNavController().navigate(KidsProfileFragmentDirections.actionFilterFragment(filterModel = filterModel))
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

    private fun getMomentList(isRequiredShimmer: Boolean) {
        if (isRequiredShimmer) {
            binding.momentsShimmerContainer.visibility = View.VISIBLE
            binding.momentGroup.visibility = View.GONE
        }
        isLoading = true
        showFilterReset(isVisible = false)
        isFilterApplied = if (filterModel?.type == FilterType.NO_SELECTION.name) false else filterModel?.type.orEmpty().isNotEmpty()
        if (isFilterApplied) setFilterData()
        momentViewModel.getMomentList(
            currentPage,
            MomentType.ALL.name,
            kidID = navigationArgs.kidId,
            startDate = filterModel?.fromDate,
            endDate = filterModel?.toDate,
            date = filterModel?.date,
            filterType = filterModel?.type,
            sortBy = MomentDateType.MOMENT_DATE.name
        )
    }

    private fun requestCameraPermission() {
        if (PermissionUtility.userGrantedCameraPermission(requireActivity())) {
            if (kidByIdResponseModel?.image.orEmpty().isNotEmpty()) chooseOptionForUserHavingImage() else chooseOption()
        } else {
            PermissionUtility.requestCameraPermission(requestPermission)
        }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            if (kidByIdResponseModel?.image.orEmpty().isNotEmpty()) chooseOptionForUserHavingImage() else chooseOption()
        } else {
            PermissionUtility.showPermissionRationaleToast(
                requireContext(),
                message = resourceProvider.getString(R.string.camera_permission_rationale)
            )
        }
    }

    private fun chooseOption() {
        val popUp = PopupMenu(binding.profilePictureContainer.context, binding.profilePictureContainer)
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
        val popUp = PopupMenu(binding.profileContainer.context, binding.profilePictureContainer)
        popUp.menuInflater.inflate(R.menu.user_image_menu, popUp.menu)
        popUp.show()
        popUp.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.actionEditImage -> chooseOption()
                R.id.actionViewImage -> {
                    findNavController().navigate(KidsProfileFragmentDirections.actionUserImageDialog(kidByIdResponseModel?.image.orEmpty()))
                }
                R.id.actionRemoveImage -> {
                   // binding.progressIndicator.showProgressbar()
                    editProfileViewModel.updateProfilePicture(image = "", userType = ReportType.KID.name, kidId = navigationArgs.kidId)
                }
            }
            true
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
                    homeViewModel.uploadFile(if (compressedImageFile.exists()) compressedImageFile else imageFile)
                }
            }
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