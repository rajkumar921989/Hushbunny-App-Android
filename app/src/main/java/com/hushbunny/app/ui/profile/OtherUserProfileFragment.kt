package com.hushbunny.app.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.hushbunny.app.R
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
import com.hushbunny.app.ui.onboarding.model.UserData
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.ui.sealedclass.BookMarkResponseInfo
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.ui.sealedclass.MomentResponseInfo
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.DateFormatUtils.getAge
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.ui.sealedclass.CommentDeletedResponseInfo
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class OtherUserProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var kidsAdapter: KidsAdapter
    private lateinit var momentAdapter: MomentAdapter
    private var momentList = ArrayList<MomentListingModel>()
    private var isLoading = true
    var currentPage = 1
    var userID = ""
    var kidsName = ""
    private var filterModel: FilterModel? = null
    private var isFilterAPICalled = false
    private var isFilterApplied = false
    var isBlockedByHim = false
    private var isOtherParent = false
    private val navigationArgs: OtherUserProfileFragmentArgs by navArgs()

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var momentRepository: MomentRepository

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var userActionRepository: UserActionRepository

    @Inject
    lateinit var homeRepository: HomeRepository
    private val momentViewModel: AddMomentViewModel by viewModelBuilderFragmentScope {
        AddMomentViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            momentRepository = momentRepository,
            fileUploadRepository = fileUploadRepository
        )
    }
    private val homeViewModel: HomeViewModel by viewModelBuilderFragmentScope {
        HomeViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            homeRepository = homeRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userID = navigationArgs.userID
        isOtherParent = navigationArgs.isOtherParent
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
        getUserDetail()
        getKidsList()
        getMomentList(true)
        setAdapter()
        setObserver()
    }

    private fun initView() {
        setProfileMomentCount(0)
        binding.editImage.visibility = View.GONE
        binding.profileContainer.editProfileButton.visibility = View.GONE
        binding.profileContainer.space.visibility = View.VISIBLE
        binding.backImage.visibility = View.VISIBLE
        binding.moreImage.visibility = View.VISIBLE
    }

    private fun initClickListener() {
        binding.pullRefresh.setOnRefreshListener {
            binding.pullRefresh.isRefreshing = false
            currentPage = 1
            momentList.clear()
            getUserDetail()
            getKidsList()
            getMomentList(true)
        }
        binding.filterButton.setOnClickListener {
            findNavController().navigate(OtherUserProfileFragmentDirections.actionFilterFragment(filterModel = filterModel))
        }
        binding.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
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
        binding.moreImage.setOnClickListener {
            val popUp = PopupMenu(binding.moreImage.context, it)
            popUp.menuInflater.inflate(R.menu.other_user_menu, popUp.menu)
            popUp.menu.findItem(R.id.actionBlock).title =
                if (isBlockedByHim) resourceProvider.getString(R.string.unblock) else resourceProvider.getString(R.string.menu_block)
            popUp.show()
            popUp.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.actionBlock -> {
                        binding.progressIndicator.showProgressbar()
                        homeViewModel.blockUser(userId = userID, action = if (isBlockedByHim) APIConstants.UNBLOCKED else APIConstants.BLOCKED)
                    }
                    R.id.actionReport -> {
                        findNavController().navigate(
                            OtherUserProfileFragmentDirections.actionReportFragment(
                                type = ReportType.USER.name,
                                userId = userID
                            )
                        )
                    }
                }
                true
            }
        }
        binding.filterReset.setOnClickListener {
            filterModel = FilterModel(type = FilterType.NO_SELECTION.name)
            loadFilterData()
        }
    }

    private fun getKidsList() {
        if(isOtherParent) {
            binding.kidsShimmerContainer.visibility = View.VISIBLE
            binding.kidsList.visibility = View.GONE
            momentViewModel.getKidsList(true, userId = userID)
        } else {
            binding.kidsShimmerContainer.visibility = View.GONE
            binding.kidsList.visibility = View.GONE
        }
    }

    private fun getUserDetail() {
        binding.profileContainer.container.visibility = View.GONE
        binding.profileShimmerContainer.visibility = View.VISIBLE
        momentViewModel.getUserDetail(userId = userID)
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
            MomentType.OTHERS.name,
            startDate = filterModel?.fromDate,
            endDate = filterModel?.toDate,
            date = filterModel?.date,
            filterType = filterModel?.type,
            userId = userID,
            sortBy = MomentDateType.CREATED_DATE.name
        )
    }

    private fun setAdapter() {
        kidsAdapter = KidsAdapter(resourceProvider = resourceProvider, isOtherUser = true) {
            findNavController().navigate(OtherUserProfileFragmentDirections.actionKidsProfileFragment(kidId = it._id.orEmpty()))
        }
        binding.kidsList.adapter = kidsAdapter
        momentAdapter =
            MomentAdapter(
                isOtherUser = !isOtherParent,
                resourceProvider = resourceProvider, onItemClick = { view: View, position: Int, type: String, item: MomentListingModel ->
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
                        val parentOne = item.parents?.firstOrNull()?._id.orEmpty()
                        val parentTwo = item.parents?.lastOrNull()?._id.orEmpty()
                        findNavController().navigate(OtherUserProfileFragmentDirections.actionCommentFragment(momentID = item._id.orEmpty(), parentOneId = parentOne, parentTwoId = parentTwo))
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
                        findNavController().navigate(OtherUserProfileFragmentDirections.actionReactionListFragment(momentID = item._id.orEmpty()))
                    }
                    resourceProvider.getString(R.string.user_detail) -> {
                        if (item.addedBy?._id.orEmpty() == AppConstants.getUserID())
                            findNavController().navigate(OtherUserProfileFragmentDirections.actionProfileFragment())
                        else findNavController().navigate(OtherUserProfileFragmentDirections.actionOtherUserProfileFragment(userID = item.addedBy?._id.orEmpty()))
                    }
                    AppConstants.MOMENT_REPORT -> {
                        findNavController().navigate(
                            OtherUserProfileFragmentDirections.actionReportFragment(
                                type = ReportType.MOMENT.name,
                                momentId = item._id.orEmpty()
                            )
                        )
                    }
                    AppConstants.COPY_URL -> {
                        AppConstants.copyURL(activity, item.shortLink)
                    }
                    ReactionPageName.LAUGH.name, ReactionPageName.SAD.name, ReactionPageName.HEART.name -> {
                        //binding.progressIndicator.showProgressbar()
                        momentViewModel.addReaction(position = position, emojiType = type, momentId = item._id.toString())
                    }
                }
            }, onKidClick = { _, kidsModel ->
                    val loggedInUserId = AppConstants.getUserID()
                    val kidParents = kidsModel.parents.orEmpty()
                    val isParentFound = kidParents.contains(loggedInUserId)
                    if (isParentFound) {
                        findNavController().navigate(
                            OtherUserProfileFragmentDirections.actionKidsProfileFragment(
                                kidId = kidsModel._id.orEmpty()
                            )
                        )
                    }
                }, onCommentClick = { position: Int, type: String, commentId: String, item: MomentListingModel ->
                when (type) {
                    AppConstants.COMMENT_REPORT -> {
                        findNavController().navigate(
                            OtherUserProfileFragmentDirections.actionReportFragment(
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
                            findNavController().navigate(OtherUserProfileFragmentDirections.actionProfileFragment())
                        } else {
                            val isOtherParent = item.parents?.any { it._id == commentId } ?: false
                            findNavController().navigate(OtherUserProfileFragmentDirections.actionOtherUserProfileFragment(userID = commentId, isOtherParent = isOtherParent))
                        }
                    }
                }

            }, onMediaClick = { type: String, url: String ->
                if (type == MediaType.IMAGE.name || type == MediaType.OG_IMAGE.name) {
                    findNavController().navigate(OtherUserProfileFragmentDirections.actionUserImageDialog(url))
                } else if (type == MediaType.VIDEO.name) {
                    findNavController().navigate(OtherUserProfileFragmentDirections.actionVideoPlayerFragment(isLocal = false, url = url))
                }

            })
        binding.momentList.adapter = momentAdapter
    }

    private fun callBookMarkAPI(position: Int, momentId: String) {
        //binding.progressIndicator.showProgressbar()
        momentViewModel.bookMarkMoment(position = position, momentId = momentId)
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
        homeViewModel.blockedUserObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    isBlockedByHim = !isBlockedByHim
                }
                APIConstants.UNAUTHORIZED_CODE -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
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
        momentViewModel.userDetailResponseObserver.observe(viewLifecycleOwner) {
            binding.profileContainer.container.visibility = View.VISIBLE
            binding.profileShimmerContainer.visibility = View.GONE
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    loadUserDetail(it.data)
                }
                APIConstants.UNAUTHORIZED_CODE -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
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
        momentViewModel.kidsListObserver.observe(viewLifecycleOwner) {
            binding.kidsShimmerContainer.visibility = View.GONE
            when (it) {
                is KidsStatusInfo.UserList -> {
                    binding.kidsList.visibility = View.VISIBLE
                    kidsName = it.userList.map {
                        it.name
                    }.joinToString()
                    kidsAdapter.submitList(it.userList)
                    setOtherParentMessage()
                }
                is KidsStatusInfo.NoKids -> {
                    binding.kidsList.visibility = View.GONE
                }
                else -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
            }
        }
        momentViewModel.momentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is MomentResponseInfo.MomentList -> {
                        if (isFilterAPICalled) {
                            momentList.clear()
                            isFilterAPICalled = false
                        } else {
                            PrefsManager.get().saveStringValue(AppConstants.USER_MOMENT_COUNT, response.count)
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
    }

    private fun showFilterReset(isVisible: Boolean) {
        binding.filterReset.visibility = if(isVisible) View.VISIBLE else View.GONE
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

    private fun setProfileMomentCount(count: Int?) {
        binding.profileContainer.totalMomentCountText.text = count.toString().prependZeroToStringIfSingleDigit()
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
            binding.otherUserEmptyViewContainer.container.visibility = View.GONE
            binding.momentGroup.visibility = View.VISIBLE
            momentAdapter.submitList(momentList.toList())
        } else if (isOtherParent) {
            binding.noMomentFound.visibility = View.GONE
            binding.momentGroup.visibility = View.GONE
            binding.emptyViewContainer.container.visibility = View.VISIBLE
            binding.emptyViewContainer.addKidButton.visibility = View.GONE
            binding.emptyViewContainer.addMomentButton.visibility = View.GONE
            binding.otherUserEmptyViewContainer.container.visibility = View.GONE
            setOtherParentMessage()
        } else {
            binding.noMomentFound.visibility = View.GONE
            binding.momentGroup.visibility = View.GONE
            binding.otherUserEmptyViewContainer.emptyMessageText.text =
                resourceProvider.getString(R.string.other_user_message, AppConstants.getUserName(), binding.profileContainer.nameText.text.toString())
            binding.otherUserEmptyViewContainer.container.visibility = View.VISIBLE
        }
    }

    private fun setOtherParentMessage() {
        binding.emptyViewContainer.welcomeMessageText.text = resourceProvider.getString(
            R.string.other_parent_message,
            AppConstants.getUserName(),
            binding.profileContainer.nameText.text.toString(),
            kidsName,
            kidsName
        )
    }

    private fun loadUserDetail(userDetail: UserData?) {
        isBlockedByHim = userDetail?.isBlockedByHim ?: false
        binding.profileContainer.nameText.text = userDetail?.name.orEmpty()
        binding.profileContainer.associateWithText.text = userDetail?.associatedAs.orEmpty()
        binding.profileContainer.countryValueText.text = AppConstants.getCountryNameByCode(userDetail?.countryId.orEmpty())
        binding.profileContainer.ageCountText.text = userDetail?.dob?.getAge()
        if (userDetail?.image?.isNotEmpty() == true) {
            binding.profileContainer.userImage.visibility = View.VISIBLE
            binding.profileContainer.defaultImage.visibility = View.GONE
            binding.profileContainer.userImage.loadImageFromURL(userDetail.image)
        } else {
            binding.profileContainer.userImage.visibility = View.GONE
            binding.profileContainer.defaultImage.visibility = View.VISIBLE
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