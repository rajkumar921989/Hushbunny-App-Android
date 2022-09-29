package com.hushbunny.app.ui.bookmark

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentBookMarkListBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.*
import com.hushbunny.app.ui.home.HomeFragmentDirections
import com.hushbunny.app.ui.moment.AddMomentViewModel
import com.hushbunny.app.ui.moment.MomentAdapter
import com.hushbunny.app.ui.home.HomeViewModel
import com.hushbunny.app.ui.model.MomentListingModel
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.BookMarkResponseInfo
import com.hushbunny.app.ui.sealedclass.CommentDeletedResponseInfo
import com.hushbunny.app.ui.sealedclass.MomentResponseInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class BookMarkFragment : Fragment(R.layout.fragment_book_mark_list) {

    private var _binding: FragmentBookMarkListBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookmarkAdapter: MomentAdapter
    private var bookMarkList = mutableListOf<MomentListingModel>()
    private val navigationArgs: BookMarkFragmentArgs by navArgs()
    var currentPage = 1
    private var isLoading = true

    @Inject
    lateinit var momentRepository: MomentRepository

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var homeRepository: HomeRepository
    private val bookmarkViewModel: AddMomentViewModel by viewModelBuilderFragmentScope {
        AddMomentViewModel(
            resourceProvider = resourceProvider,
            ioDispatcher = Dispatchers.IO,
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookMarkListBinding.bind(view)
        currentPage = 1
        bookMarkList.clear()
        setAdapter()
        setObserver()
        initClickListener()
        getBookMarkList(true)
    }

    private fun getBookMarkList(isRequiredShimmer: Boolean) {
        if (isRequiredShimmer) {
            binding.shimmerContainer.visibility = View.VISIBLE
            binding.bookMarkList.visibility = View.GONE
        }
        bookmarkViewModel.getMomentList(
            currentPage = currentPage, sortBy = MomentDateType.MOMENT_DATE.name,
            type = if (navigationArgs.type == resourceProvider.getString(R.string.important_moments)) MomentType.IMPORTANT.name
            else MomentType.BOOKMARKED.name
        )
    }

    private fun setAdapter() {
        bookmarkAdapter =
            MomentAdapter(resourceProvider = resourceProvider, onItemClick = { view: View, position: Int, type: String, item: MomentListingModel ->
                when (type) {
                    resourceProvider.getString(R.string.bookmarks) -> {
                        if (item.isBookmarked == true) {
                            DialogUtils.showDialogWithCallBack(
                                requireContext(),
                                message = resourceProvider.getString(R.string.delete_from_bookmark),
                                title = resourceProvider.getString(R.string.delete), positiveButtonText = resourceProvider.getString(R.string.yes),
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
                        findNavController().navigate(BookMarkFragmentDirections.actionCommentFragment(momentID = item._id.orEmpty()))
                    }
                    resourceProvider.getString(R.string.share) -> {
                        bookmarkViewModel.shareMoment(item._id.orEmpty())
                        AppConstants.shareTheAPP(
                            requireActivity(),
                            title = resourceProvider.getString(R.string.share),
                            extraMessage = item.description.orEmpty(),
                            appUrl = item.shortLink.orEmpty()
                        )
                    }
                    resourceProvider.getString(R.string.like) -> {
                        findNavController().navigate(BookMarkFragmentDirections.actionReactionListFragment(momentID = item._id.orEmpty()))
                    }
                    resourceProvider.getString(R.string.user_detail) -> {
                        if (item.addedBy?._id.orEmpty() == AppConstants.getUserID())
                            findNavController().navigate(BookMarkFragmentDirections.actionProfileFragment())
                        else findNavController().navigate(BookMarkFragmentDirections.actionOtherUserProfileFragment(userID = item.addedBy?._id.orEmpty()))
                    }
                    APIConstants.BLOCKED -> {
                        binding.progressIndicator.showProgressbar()
                        homeViewModel.blockUser(userId = item.addedBy?._id.orEmpty(), action = APIConstants.BLOCKED)
                    }
                    AppConstants.MOMENT_EDIT -> {
                        findNavController().navigate(BookMarkFragmentDirections.actionAddMomentFragment(isEdit = true, momentID = item._id.orEmpty()))
                    }
                    AppConstants.ADD_YOUR_KID -> {
                        findNavController().navigate(
                            BookMarkFragmentDirections.actionAddMomentFragment(
                                isSpouseAdded = true,
                                momentID = item._id.orEmpty()
                            )
                        )
                    }
                    AppConstants.IMPORTANT_MOMENT -> {
                        binding.progressIndicator.showProgressbar()
                        bookmarkViewModel.markMomentAsImportant(position = position, momentId = item._id.orEmpty())
                    }
                    AppConstants.MOMENT_REPORT -> {
                        findNavController().navigate(
                            BookMarkFragmentDirections.actionReportFragment(
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
                        bookmarkViewModel.addReaction(position = position, emojiType = type, momentId = item._id.toString())
                    }
                }
            }, onCommentClick = { position: Int, type: String, commentId: String ->
                when (type) {
                    AppConstants.COMMENT_REPORT -> {
                        findNavController().navigate(
                            BookMarkFragmentDirections.actionReportFragment(
                                type = ReportType.COMMENT.name,
                                commentId = commentId
                            )
                        )
                    }
                    AppConstants.COMMENT_DELETE -> {
                        binding.progressIndicator.showProgressbar()
                        bookmarkViewModel.deleteComment(position = position, commentId = commentId)
                    }
                    AppConstants.USER_PROFILE -> {
                        if (commentId == AppConstants.getUserID()) {
                            findNavController().navigate(BookMarkFragmentDirections.actionProfileFragment())
                        } else {
                            findNavController().navigate(BookMarkFragmentDirections.actionOtherUserProfileFragment(commentId))
                        }
                    }
                }

            }, onMediaClick = { type: String, url: String ->
                if (type == MediaType.IMAGE.name || type == MediaType.OG_IMAGE.name) {
                    findNavController().navigate(BookMarkFragmentDirections.actionUserImageDialog(url))
                } else if (type == MediaType.VIDEO.name) {
                    findNavController().navigate(BookMarkFragmentDirections.actionVideoPlayerFragment(isLocal = false, url = url))
                }

            })
        binding.bookMarkList.adapter = bookmarkAdapter
    }

    private fun callBookMarkAPI(position: Int, momentId: String) {
        binding.progressIndicator.showProgressbar()
        bookmarkViewModel.bookMarkMoment(position = position, momentId = momentId)
    }

    private fun initClickListener() {
        if (navigationArgs.type == resourceProvider.getString(R.string.important_moments)) {
            binding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.important_moments)
            binding.noDataFoundText.text = resourceProvider.getString(R.string.no_important_moments_found)
        } else {
            binding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.bookmarks)
            binding.noDataFoundText.text = resourceProvider.getString(R.string.no_book_mark_found)
        }
        binding.pullRefresh.setOnRefreshListener {
            binding.pullRefresh.isRefreshing = false
            currentPage = 1
            bookMarkList.clear()
            getBookMarkList(true)
        }
        binding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.bookMarkList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.bookMarkList.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount == lastVisibleItem) {
                    ++currentPage
                    getBookMarkList(false)
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver() {
        homeViewModel.blockedUserObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    currentPage = 1
                    bookMarkList.clear()
                    getBookMarkList(true)
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
        bookmarkViewModel.markAsImportantMomentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        response.bookMark?.let { it1 -> bookmarkAdapter.updateMomentDetail(position = response.position, model = it1) }
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
        bookmarkViewModel.momentObserver.observe(viewLifecycleOwner) {
            binding.shimmerContainer.visibility = View.GONE
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is MomentResponseInfo.MomentList -> {
                        isLoading = response.momentList.size < 30
                        bookMarkList.addAll(response.momentList)
                        loadBookMarkList()
                    }
                    is MomentResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                    else -> {
                        isLoading = true
                        loadBookMarkList()
                    }
                }
            }
        }
        bookmarkViewModel.bookMarkObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        if (navigationArgs.type == resourceProvider.getString(R.string.bookmarks)) {
                            bookMarkList.removeAt(response.position)
                            bookmarkAdapter.notifyDataSetChanged()
                            if (bookMarkList.isEmpty()) visibleNoCommentView()
                        } else {
                            response.bookMark?.let { it1 -> bookmarkAdapter.updateMomentDetail(position = response.position, model = it1) }
                        }

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
        bookmarkViewModel.reactionObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        response.bookMark?.let { it1 -> bookmarkAdapter.updateMomentDetail(position = response.position, model = it1) }
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
        bookmarkViewModel.deleteCommentObserver.observe(viewLifecycleOwner) {
            when (it) {
                is CommentDeletedResponseInfo.CommentDelete -> {
                    currentPage = 1
                    bookMarkList.clear()
                    getBookMarkList(true)
                }
                is CommentDeletedResponseInfo.ApiError -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
                is CommentDeletedResponseInfo.HaveError -> {
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

    private fun loadBookMarkList() {
        binding.shimmerContainer.visibility = View.GONE
        if (bookMarkList.isNotEmpty()) {
            binding.bookMarkList.visibility = View.VISIBLE
            binding.noDataFoundText.visibility = View.GONE
            bookmarkAdapter.submitList(bookMarkList)
        } else {
            visibleNoCommentView()
        }
    }

    private fun visibleNoCommentView() {
        binding.noDataFoundText.visibility = View.VISIBLE
        binding.bookMarkList.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}