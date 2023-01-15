package com.hushbunny.app.ui.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hushbunny.app.R
import com.hushbunny.app.core.HomeSharedViewModel
import com.hushbunny.app.databinding.FragmentHomeBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.*
import com.hushbunny.app.ui.model.MomentListingModel
import com.hushbunny.app.ui.moment.AddMomentViewModel
import com.hushbunny.app.ui.moment.MomentAdapter
import com.hushbunny.app.ui.notifications.NotificationsFragmentDirections
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.ui.sealedclass.*
import com.hushbunny.app.ui.setting.SettingActionDialog
import com.hushbunny.app.ui.setting.SettingViewModel
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.dialog.DialogUtils
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var kidsAdapter: KidsAdapter
    private lateinit var momentAdapter: MomentAdapter
    private var momentList = ArrayList<MomentListingModel>()
    private var isLoading = true
    var currentPage = 1
    private var momentID = ""
    private var kidID = ""
    private var notificationType = ""

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    private val homeSharedViewModel: HomeSharedViewModel by viewModelBuilderActivityScope {
        HomeSharedViewModel()
    }

    private val settingViewModel: SettingViewModel by viewModelBuilderFragmentScope {
        SettingViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            userActionRepository = userActionRepository
        )
    }

    private val homeViewModel: HomeViewModel by viewModelBuilderFragmentScope {
        HomeViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            homeRepository = homeRepository
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        momentID = activity?.intent?.getStringExtra(APIConstants.QUERY_PARAMS_MOMENT_ID).orEmpty()
        kidID = activity?.intent?.getStringExtra(APIConstants.QUERY_PARAMS_KID_ID).orEmpty()
        notificationType = activity?.intent?.getStringExtra(AppConstants.NOTIFICATION_TYPE).orEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        currentPage = 1
        momentList.clear()
        handleIntentLink()
        setAdapter()
        updateDeviceToken()
        getKidsList()
        getMomentList(true)
        setObserver()
        initClickListener()
    }

    private fun updateDeviceToken() {
        if (PrefsManager.get().getBoolean(AppConstants.IS_REQUIRED_UPDATE_TOKEN, false)) {
            settingViewModel.updateDeviceToken()
            PrefsManager.get().saveBooleanValues(AppConstants.IS_REQUIRED_UPDATE_TOKEN, false)
        }
    }

    private fun handleIntentLink() {
        if (PrefsManager.get().getBoolean(AppConstants.IS_FROM_PUSH_NOTIFICATION, false)) {
            handlePushNotification()
            PrefsManager.get().saveBooleanValues(AppConstants.IS_FROM_PUSH_NOTIFICATION, false)
        } else if (momentID.isNotEmpty() && PrefsManager.get().getBoolean(AppConstants.IS_REQUIRED_NAVIGATION, false)) {
            findNavController().navigate(HomeFragmentDirections.actionMomentDetailFragment(momentID))
        } else if (PrefsManager.get().getBoolean(AppConstants.GALLERY_MEDIA, false)) {
            findNavController().navigate(HomeFragmentDirections.actionAddMomentFragment())
        }

    }

    private fun handlePushNotification() {
        when (notificationType) {
            NotificationType.COMMENT_ON_MOMENT.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionCommentFragment(momentID = momentID))
            }
            NotificationType.REACTION_ON_MOMENT.name, NotificationType.LAUGH_REACTION_ON_MOMENT.name, NotificationType.LOVE_REACTION_ON_MOMENT.name, NotificationType.SAD_REACTION_ON_MOMENT.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionReactionListFragment(momentID = momentID))
            }
            NotificationType.REMINDER_TO_ADD_SPOUSE_AFTER_72_HOURS.name, NotificationType.REMINDER_ADD_OTHER_PARENT.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionInviteSpouseFragment(kidID))
            }
            NotificationType.WELCOME_NOTIFICATION.name, NotificationType.ADD_KID.name, NotificationType.REMINDER_ADD_KID.name, NotificationType.REMINDER_TO_ADD_KID_AFTER_72_HOURS.name, NotificationType.ADD_KID_WITHOUT_ADDED_SPOUSE.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionAddKidFragment())
            }
            NotificationType.ADD_OTHER_PARENT.name, NotificationType.OTHER_PARENT_ACCEPTED_INVITATION.name, NotificationType.PARENT_DELETED_ACCOUNT.name, NotificationType.PARENT_DEACTIVATED_ACCOUNT.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionKidsProfileFragment(kidId = kidID))
            }
            NotificationType.PARENT_REACTIVATED_ACCOUNT.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionOtherUserProfileFragment(kidID))
            }
            NotificationType.ADD_FIRST_MOMENT_BY_FIRST_PARENT.name, NotificationType.ADD_FIRST_MOMENT_BY_OTHER_PARENT.name, NotificationType.REMINDER_BOTH_PARENT_ADD_FIRST_MOMENT.name, NotificationType.REMINDER_ADD_MOMENT.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionAddMomentFragment())
            }
            NotificationType.PARENT_MARKED_MOMENT_IMPORTANT.name -> {
                findNavController().navigate(NotificationsFragmentDirections.actionMomentDetailFragment(momentID))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.VISIBLE)
    }

    private fun initClickListener() {
        binding.emptyViewContainer.welcomeMessageText.text =
            resourceProvider.getString(R.string.home_page_welcome_message, AppConstants.getUserFirstName())
        binding.pullRefresh.setOnRefreshListener {
            onPullToRefreshCalled()
        }
        binding.emptyViewContainer.addKidButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionAddKidFragment(isEditKid = false))
        }
        binding.emptyViewContainer.addMomentButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionAddMomentFragment())
        }
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
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
        getKidsList()
        getMomentList(true)
        binding.pullRefresh.isRefreshing = false
        homeSharedViewModel.refreshNotificationUnReadCount()
    }

    private fun setMomentListScrollBehaviour() {
        if (binding.momentList.isVisible && momentList.isNotEmpty()) {
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
    }

    private fun setAdapter() {
        kidsAdapter = KidsAdapter(resourceProvider = resourceProvider, isFromHome = true, addKidsClick = {
            findNavController().navigate(HomeFragmentDirections.actionAddKidFragment(isEditKid = false))
        }, kidsClick = {
            findNavController().navigate(HomeFragmentDirections.actionKidsProfileFragment(kidId = it._id.orEmpty()))
        }, addSpouseClick = { id, inviteInfoModel ->
            findNavController().navigate(HomeFragmentDirections.actionInviteSpouseFragment(kidId = id, inviteInfo = inviteInfoModel))
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
                    /*Added By RajKumar*/
                    resourceProvider.getString(R.string.mark_as_important_moment) -> {
                        if (item.isImportant == true) {
                            DialogUtils.showDialogWithCallBack(
                                requireContext(),
                                message = resourceProvider.getString(R.string.delete_from_important),
                                title = "",
                                positiveButtonText = resourceProvider.getString(R.string.yes),
                                negativeButtonText = resourceProvider.getString(R.string.cancel),
                                positiveButtonCallback = {
                                    callImportatMomentAPI(position = position, momentId = item._id.orEmpty())
                                }
                            )
                        } else {
                            callImportatMomentAPI(position = position, momentId = item._id.orEmpty())
                        }
                    }
                    resourceProvider.getString(R.string.comments) -> {
                        val parentOne = item.parents?.firstOrNull()?._id.orEmpty()
                        val parentTwo = item.parents?.lastOrNull()?._id.orEmpty()
                        findNavController().navigate(HomeFragmentDirections.actionCommentFragment(momentID = item._id.orEmpty(), parentOneId = parentOne, parentTwoId = parentTwo))
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
                        findNavController().navigate(HomeFragmentDirections.actionReactionListFragment(momentID = item._id.orEmpty()))
                    }
                    resourceProvider.getString(R.string.user_detail) -> {
                        if (item.addedBy?._id.orEmpty() == AppConstants.getUserID())
                            findNavController().navigate(HomeFragmentDirections.actionProfileFragment())
                        else findNavController().navigate(HomeFragmentDirections.actionOtherUserProfileFragment(userID = item.addedBy?._id.orEmpty()))
                    }
                    APIConstants.BLOCKED -> {
                        binding.progressIndicator.showProgressbar()
                        homeViewModel.blockUser(userId = item.addedBy?._id.orEmpty(), action = APIConstants.BLOCKED)
                    }
                    AppConstants.MOMENT_EDIT -> {
                        findNavController().navigate(HomeFragmentDirections.actionAddMomentFragment(isEdit = true, momentID = item._id.orEmpty()))
                    }
                    AppConstants.ADD_YOUR_KID -> {
                        findNavController().navigate(
                            HomeFragmentDirections.actionAddMomentFragment(
                                isSpouseAdded = true,
                                momentID = item._id.orEmpty()
                            )
                        )
                    }
                    AppConstants.IMPORTANT_MOMENT -> {
                       // binding.progressIndicator.showProgressbar()
                        momentViewModel.markMomentAsImportant(position = position, momentId = item._id.orEmpty())
                    }
                    AppConstants.MOMENT_REPORT -> {
                        findNavController().navigate(
                            HomeFragmentDirections.actionReportFragment(
                                type = ReportType.MOMENT.name,
                                momentId = item._id.orEmpty()
                            )
                        )
                    }
                    AppConstants.COPY_URL -> {
                        AppConstants.copyURL(activity, item.shortLink)
                    }
                    ReactionPageName.LAUGH.name, ReactionPageName.SAD.name, ReactionPageName.HEART.name -> {
                       // binding.progressIndicator.showProgressbar()
                        momentViewModel.addReaction(position = position, emojiType = type, momentId = item._id.orEmpty())
                    }
                    AppConstants.DELETE_MOMENT -> {
                        val dialog = SettingActionDialog(requireContext(), resourceProvider.getString(R.string.delete_moment)) {
                            binding.progressIndicator.showProgressbar()
                            momentViewModel.deleteMoment(position = position, momentId = item._id.orEmpty())
                        }
                        dialog.show()
                    }
                }
            }, onKidClick = { _ , kidsModel ->
                val loggedInUserId = AppConstants.getUserID()
                val kidParents = kidsModel.parents.orEmpty()
                val isParentFound = kidParents.contains(loggedInUserId)
                if(isParentFound) {
                    findNavController().navigate(HomeFragmentDirections.actionKidsProfileFragment(kidId = kidsModel._id.orEmpty()))
                }
            }, onCommentClick = { position: Int, type: String, commentId: String, item: MomentListingModel ->
                when (type) {
                    AppConstants.COMMENT_REPORT -> {
                        findNavController().navigate(
                            HomeFragmentDirections.actionReportFragment(
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
                            findNavController().navigate(HomeFragmentDirections.actionProfileFragment())
                        } else {
                            val isOtherParent = item.parents?.any { it._id == commentId } ?: false
                            findNavController().navigate(HomeFragmentDirections.actionOtherUserProfileFragment(userID = commentId, isOtherParent = isOtherParent))
                        }
                    }
                }

            }, onMediaClick = { type: String, url: String ->
                if (type == MediaType.IMAGE.name || type == MediaType.OG_IMAGE.name) {
                    findNavController().navigate(HomeFragmentDirections.actionUserImageDialog(url))
                } else if (type == MediaType.VIDEO.name) {
                    findNavController().navigate(HomeFragmentDirections.actionVideoPlayerFragment(isLocal = false, url = url))
                }

            })
        binding.momentList.adapter = momentAdapter
    }

    private fun callBookMarkAPI(position: Int, momentId: String) {
        //binding.progressIndicator.showProgressbar()
        momentViewModel.bookMarkMoment(position = position, momentId = momentId)
    }
    private fun callImportatMomentAPI(position: Int, momentId: String) {
        //binding.progressIndicator.showProgressbar()
        momentViewModel.markMomentAsImportant(position = position, momentId = momentId)
    }

    private fun setObserver() {
        homeViewModel.blockedUserObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    currentPage = 1
                    momentList.clear()
                    getMomentList(true)
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
        homeViewModel.kidsListObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.kidsShimmerContainer.visibility = View.GONE
                binding.kidsList.visibility = View.VISIBLE
                when (response) {
                    is KidsStatusInfo.UserList -> {
                        kidsAdapter.submitList(response.userList)
                    }
                    else -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
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

        homeSharedViewModel.homeTabClickedObserver.observe(viewLifecycleOwner) {
            setMomentListScrollBehaviour()
        }
    }

    private fun setTotalMomentCount(count: Int?) {
        momentAdapter.setTotalMomentCount(count)
    }

    private fun loadMomentList() {
        binding.momentsShimmerContainer.visibility = View.GONE
        if (momentList.isNotEmpty()) {
            binding.emptyViewContainer.container.visibility = View.GONE
            binding.momentList.visibility = View.VISIBLE
            momentAdapter.submitList(momentList.toList())
        } else {
            binding.momentList.visibility = View.GONE
            binding.emptyViewContainer.container.visibility = View.VISIBLE
        }
    }

    private fun getKidsList() {
        binding.kidsShimmerContainer.visibility = View.VISIBLE
        binding.kidsList.visibility = View.GONE
        homeViewModel.getKidsList()
    }

    private fun getMomentList(isRequiredShimmer: Boolean) {
        if (isRequiredShimmer) {
            binding.momentList.visibility = View.GONE
            binding.momentsShimmerContainer.visibility = View.VISIBLE
        }
        isLoading = true
        momentViewModel.getMomentList(currentPage, MomentType.ALL.name, sortBy = MomentDateType.CREATED_DATE.name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}