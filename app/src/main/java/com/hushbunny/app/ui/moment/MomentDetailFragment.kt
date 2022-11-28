package com.hushbunny.app.ui.moment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentMomentDetailBinding
import com.hushbunny.app.databinding.PopupReactionBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.comment.CommentListAdapter
import com.hushbunny.app.ui.enumclass.MediaType
import com.hushbunny.app.ui.enumclass.ReactionPageName
import com.hushbunny.app.ui.enumclass.ReportType
import com.hushbunny.app.ui.home.HomeFragmentDirections
import com.hushbunny.app.ui.model.CommentModel
import com.hushbunny.app.ui.model.MomentDetailDataModel
import com.hushbunny.app.ui.model.MomentMediaModel
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.BookMarkResponseInfo
import com.hushbunny.app.ui.sealedclass.CommentDeletedResponseInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.DateFormatUtils.convertISODateIntoAppDateFormat
import com.hushbunny.app.uitls.DateFormatUtils.getTimeAgo
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadCircleImageFromURL
import com.hushbunny.app.uitls.PrefsManager
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class MomentDetailFragment : Fragment(R.layout.fragment_moment_detail) {
    private var _binding: FragmentMomentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageViewAdapter: ImageSliderAdapter
    private val navigationArgs: MomentDetailFragmentArgs by navArgs()
    private var momentId: String = ""
    private var momentDetailDataModel: MomentDetailDataModel? = null

    @Inject
    lateinit var momentRepository: MomentRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var homeRepository: HomeRepository
    private var momentImageList = mutableListOf<MomentMediaModel>()

    private val momentDetailViewModel: MomentDetailViewModel by viewModelBuilderFragmentScope {
        MomentDetailViewModel(
            ioDispatcher = Dispatchers.IO,
            momentRepository = momentRepository
        )
    }
    private val bookmarkViewModel: AddMomentViewModel by viewModelBuilderFragmentScope {
        AddMomentViewModel(
            resourceProvider = resourceProvider,
            ioDispatcher = Dispatchers.IO,
            momentRepository = momentRepository,
            fileUploadRepository = fileUploadRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        momentId = navigationArgs.momentID
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMomentDetailBinding.bind(view)
        PrefsManager.get().saveBooleanValues(AppConstants.IS_REQUIRED_NAVIGATION, false)
        momentImageList.clear()
        getMomentDetail()
        initializeClickListener()
        setAdapter()
        setObserver()
    }

    private fun setObserver() {
        momentDetailViewModel.momentDetailObserver.observe(viewLifecycleOwner) {
            binding.shimmerContainer.visibility = View.GONE
            binding.momentContainer.root.visibility = View.VISIBLE
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    momentDetailDataModel = it.data
                    updateMomentDetail()
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
        bookmarkViewModel.bookMarkObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        momentDetailDataModel = MomentDetailDataModel(
                            _id = response.bookMark?._id, momentDate = response.bookMark?.momentDate,
                            momentCount = response.bookMark?.momentNumber,
                            reactionCount = response.bookMark?.reactionCount,
                            commentCount = response.bookMark?.commentCount,
                            shortLink = response.bookMark?.shortLink,
                            isImportant = response.bookMark?.isImportant,
                            isAddedBySpouse = response.bookMark?.isAddedBySpouse,
                            isBookmarked = response.bookMark?.isBookmarked,
                            description = response.bookMark?.description,
                            addedBy = response.bookMark?.addedBy,
                            kidId = response.bookMark?.kidId,
                            parents = response.bookMark?.parents,
                            mediaContent = response.bookMark?.mediaContent,
                            createdAt = response.bookMark?.createdAt,
                            updatedAt = response.bookMark?.updatedAt,
                            isReacted = response.bookMark?.isReacted,
                            reactedInfo = response.bookMark?.reactedInfo,
                            comments = response.bookMark?.comments
                        )
                        updateMomentDetail()
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
        bookmarkViewModel.markAsImportantMomentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is BookMarkResponseInfo.BookMarkSuccess -> {
                        momentDetailDataModel = MomentDetailDataModel(
                            _id = response.bookMark?._id, momentDate = response.bookMark?.momentDate,
                            momentCount = response.bookMark?.momentNumber,
                            reactionCount = response.bookMark?.reactionCount,
                            commentCount = response.bookMark?.commentCount,
                            shortLink = response.bookMark?.shortLink,
                            isImportant = response.bookMark?.isImportant,
                            isAddedBySpouse = response.bookMark?.isAddedBySpouse,
                            isBookmarked = response.bookMark?.isBookmarked,
                            description = response.bookMark?.description,
                            addedBy = response.bookMark?.addedBy,
                            kidId = response.bookMark?.kidId,
                            parents = response.bookMark?.parents,
                            mediaContent = response.bookMark?.mediaContent,
                            createdAt = response.bookMark?.createdAt,
                            updatedAt = response.bookMark?.updatedAt,
                            isReacted = response.bookMark?.isReacted,
                            reactedInfo = response.bookMark?.reactedInfo,
                            comments = response.bookMark?.comments
                        )
                        updateMomentDetail()
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
                        momentDetailDataModel = MomentDetailDataModel(
                            _id = response.bookMark?._id, momentDate = response.bookMark?.momentDate,
                            momentCount = response.bookMark?.momentNumber,
                            reactionCount = response.bookMark?.reactionCount,
                            commentCount = response.bookMark?.commentCount,
                            shortLink = response.bookMark?.shortLink,
                            isImportant = response.bookMark?.isImportant,
                            isAddedBySpouse = response.bookMark?.isAddedBySpouse,
                            isBookmarked = response.bookMark?.isBookmarked,
                            description = response.bookMark?.description,
                            addedBy = response.bookMark?.addedBy,
                            kidId = response.bookMark?.kidId,
                            parents = response.bookMark?.parents,
                            mediaContent = response.bookMark?.mediaContent,
                            createdAt = response.bookMark?.createdAt,
                            updatedAt = response.bookMark?.updatedAt,
                            isReacted = response.bookMark?.isReacted,
                            reactedInfo = response.bookMark?.reactedInfo,
                            comments = response.bookMark?.comments
                        )
                        updateMomentDetail()
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
                    momentDetailViewModel.getMomentID(momentId = momentId)
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

    private fun updateMomentDetail() {
        momentImageList.clear()
        if (momentDetailDataModel?.isImportant == true) binding.momentContainer.starImage.setImageResource(R.drawable.ic_important_marked)
        else binding.momentContainer.starImage.setImageResource(R.drawable.ic_important)
        if (momentDetailDataModel?.isBookmarked == true) binding.momentContainer.bookmarkImage.setImageResource(R.drawable.ic_setting_book_mark)
        else binding.momentContainer.bookmarkImage.setImageResource(R.drawable.ic_book_mark)
        binding.momentContainer.dateText.text = momentDetailDataModel?.updatedAt?.getTimeAgo()
        binding.momentContainer.dateValueText.text = momentDetailDataModel?.momentDate?.convertISODateIntoAppDateFormat()
        binding.momentContainer.userNameText.text = momentDetailDataModel?.addedBy?.name.orEmpty()
        binding.momentContainer.momentCountText.text = momentDetailDataModel?.momentCount ?: "0"
        binding.momentContainer.favoriteCountText.text = momentDetailDataModel?.reactionCount.orEmpty()
        binding.momentContainer.commentCountText.text = momentDetailDataModel?.commentCount.orEmpty()
        binding.momentContainer.lessDescriptionText.text = momentDetailDataModel?.description.orEmpty()
        binding.momentContainer.moreDescriptionText.text = momentDetailDataModel?.description.orEmpty()
        if (momentDetailDataModel?.addedBy?._id.orEmpty() == AppConstants.getUserID() || momentDetailDataModel?.isAddedBySpouse == true)
            binding.momentContainer.moreImage.visibility = View.VISIBLE
        else binding.momentContainer.moreImage.visibility = View.INVISIBLE
        if (momentDetailDataModel?.isReacted == true) {
            when (momentDetailDataModel?.reactedInfo?.emojiType) {
                ReactionPageName.LAUGH.name -> {
                    binding.momentContainer.favoriteImage.setImageResource(R.drawable.ic_laugh_reaction)
                }
                ReactionPageName.SAD.name -> {
                    binding.momentContainer.favoriteImage.setImageResource(R.drawable.ic_sad_reaction)
                }
                else -> {
                    binding.momentContainer.favoriteImage.setImageResource(R.drawable.ic_heart_reaction)
                }
            }
        } else {
            binding.momentContainer.favoriteImage.setImageResource(R.drawable.ic_favorite)
        }
        if (momentDetailDataModel?.addedBy?.image.isNullOrEmpty())
            binding.momentContainer.userImage.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.momentContainer.userImage.context,
                    R.drawable.ic_no_kid_icon
                )
            )
        else binding.momentContainer.userImage.loadCircleImageFromURL(momentDetailDataModel?.addedBy?.image.orEmpty())
        val kidsAdapter = MomentKidsListAdapter(onItemClick = { kidsModel ->
            val loggedInUserId = AppConstants.getUserID()
            val kidParents = kidsModel.parents.orEmpty()
            val isParentFound = kidParents.contains(loggedInUserId)
            if (isParentFound) {
                findNavController().navigate(
                    MomentDetailFragmentDirections.actionKidsProfileFragment(
                        kidId = kidsModel._id.orEmpty()
                    )
                )
            }
        })
        binding.momentContainer.kidsList.adapter = kidsAdapter
        kidsAdapter.submitList(momentDetailDataModel?.kidId)
        val commentAdapter = CommentListAdapter(
            onDeleteClick = { position: Int, item: CommentModel ->
                binding.progressIndicator.showProgressbar()
                bookmarkViewModel.deleteComment(position = position, commentId = item.commentBy?._id.orEmpty())
            }, onReportClick = { _: Int, item: CommentModel ->
                findNavController().navigate(
                    HomeFragmentDirections.actionReportFragment(
                        type = ReportType.COMMENT.name,
                        commentId = item.commentBy?._id.orEmpty()
                    )
                )
            }, onUserClick = { commentModel ->
                val commentId = commentModel.commentBy?._id.orEmpty()
                if (commentId == AppConstants.getUserID()) {
                    findNavController().navigate(HomeFragmentDirections.actionProfileFragment())
                } else {
                    val isOtherParent = momentDetailDataModel?.parents?.any { it._id == commentId } ?: false
                    findNavController().navigate(HomeFragmentDirections.actionOtherUserProfileFragment(userID = commentId, isOtherParent = isOtherParent))
                }
            }
        )
        binding.momentContainer.commentList.adapter = commentAdapter
        commentAdapter.submitList(AppConstants.createCommentListFromMomentDetail(momentDetailDataModel))
        binding.momentContainer.viewAllCommentText.text =
            binding.momentContainer.viewAllCommentText.context.getString(R.string.view_all_comment, momentDetailDataModel?.commentCount.orEmpty())
        if (momentDetailDataModel?.comments.isNullOrEmpty()) {
            binding.momentContainer.commentsContainer.visibility = View.GONE
            binding.momentContainer.bottomSpace.visibility = View.GONE
        } else {
            binding.momentContainer.commentsContainer.visibility = View.VISIBLE
            binding.momentContainer.bottomSpace.visibility = View.VISIBLE
        }

        val mediaContent = momentDetailDataModel?.mediaContent.orEmpty()
        binding.momentContainer.headerImageViewPager.run {
            offscreenPageLimit = mediaContent.size
            if (mediaContent.size > 1) {
                clipToPadding = false
                setPadding(0, 0, resourceProvider.getDimension(R.dimen.margin_40).toInt(), 0)
                pageMargin = resourceProvider.getDimension(R.dimen.margin_10).toInt()
            } else {
                setPadding(0, 0, 0, 0)
                pageMargin = 0
            }
            visibility = if (mediaContent.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.momentContainer.lessDescriptionText.text = momentDetailDataModel?.description.orEmpty()
        binding.momentContainer.moreDescriptionText.text = momentDetailDataModel?.description.orEmpty()
        binding.momentContainer.readMoreText.run {
            visibility = if (momentDetailDataModel?.description.orEmpty().length > binding.momentContainer.lessDescriptionText.textSize) View.VISIBLE else View.GONE
            setOnClickListener {
                if (text.toString() == context.getString(R.string.read_more)) {
                    binding.momentContainer.moreDescriptionText.visibility = View.VISIBLE
                    binding.momentContainer.lessDescriptionText.visibility = View.GONE
                    text = context.getString(R.string.view_less)
                } else {
                    binding.momentContainer.moreDescriptionText.visibility = View.GONE
                    binding.momentContainer.lessDescriptionText.visibility = View.VISIBLE
                    text = context.getString(R.string.read_more)
                }
            }
        }

        momentImageList.addAll(mediaContent)
        refreshAdapter()
    }

    private fun getMomentDetail() {
        binding.shimmerContainer.visibility = View.VISIBLE
        binding.momentContainer.root.visibility = View.GONE
        momentDetailViewModel.getMomentID(momentId = momentId)
    }

    private fun initializeClickListener() {
        binding.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.momentContainer.favoriteImage.setOnClickListener {
            val popupReactionBinding = PopupReactionBinding.inflate(LayoutInflater.from(it.context))
            val popupWindow =
                PopupWindow(popupReactionBinding.root, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            popupWindow.isOutsideTouchable = true
            val leftToRight = resourceProvider.getDimension(R.dimen.margin_17).toInt()
            val bottomToTop = resourceProvider.getDimension(R.dimen.margin_70).toInt()
            popupWindow.showAsDropDown(binding.momentContainer.favoriteImage, leftToRight, -bottomToTop)
            popupReactionBinding.heartImage.setOnClickListener {
                popupWindow.dismiss()
                binding.progressIndicator.showProgressbar()
                bookmarkViewModel.addReaction(
                    position = AppConstants.DEFAULT_POSITION,
                    emojiType = ReactionPageName.HEART.name,
                    momentId = momentDetailDataModel?._id.toString()
                )
            }
            popupReactionBinding.laughImage.setOnClickListener {
                popupWindow.dismiss()
                binding.progressIndicator.showProgressbar()
                bookmarkViewModel.addReaction(
                    position = AppConstants.DEFAULT_POSITION,
                    emojiType = ReactionPageName.LAUGH.name,
                    momentId = momentDetailDataModel?._id.toString()
                )
            }
            popupReactionBinding.sadImage.setOnClickListener {
                popupWindow.dismiss()
                binding.progressIndicator.showProgressbar()
                bookmarkViewModel.addReaction(
                    position = AppConstants.DEFAULT_POSITION,
                    emojiType = ReactionPageName.SAD.name,
                    momentId = momentDetailDataModel?._id.toString()
                )
            }
        }
        binding.momentContainer.commentImage.setOnClickListener {
            val parentOne = momentDetailDataModel?.parents?.firstOrNull()?._id.orEmpty()
            val parentTwo = momentDetailDataModel?.parents?.lastOrNull()?._id.orEmpty()
            findNavController().navigate(MomentDetailFragmentDirections.actionCommentFragment(momentID = momentDetailDataModel?._id.orEmpty(), parentOneId = parentOne, parentTwoId = parentTwo))
        }
        binding.momentContainer.commentCountText.setOnClickListener {
            val parentOne = momentDetailDataModel?.parents?.firstOrNull()?._id.orEmpty()
            val parentTwo = momentDetailDataModel?.parents?.lastOrNull()?._id.orEmpty()
            findNavController().navigate(MomentDetailFragmentDirections.actionCommentFragment(momentID = momentDetailDataModel?._id.orEmpty(), parentOneId = parentOne, parentTwoId = parentTwo))
        }
        binding.momentContainer.favoriteCountText.setOnClickListener {
            findNavController().navigate(MomentDetailFragmentDirections.actionReactionListFragment(momentID = momentDetailDataModel?._id.orEmpty()))
        }
        binding.momentContainer.viewAllCommentText.setOnClickListener {
            val parentOne = momentDetailDataModel?.parents?.firstOrNull()?._id.orEmpty()
            val parentTwo = momentDetailDataModel?.parents?.lastOrNull()?._id.orEmpty()
            findNavController().navigate(MomentDetailFragmentDirections.actionCommentFragment(momentID = momentDetailDataModel?._id.orEmpty(), parentOneId = parentOne, parentTwoId = parentTwo))
        }
        binding.momentContainer.shareImage.setOnClickListener { view ->
            momentDetailViewModel.shareMoment(momentDetailDataModel?._id.orEmpty())
            AppConstants.shareTheAPP(
                requireActivity(),
                title = resourceProvider.getString(R.string.share),
                extraMessage = momentDetailDataModel?.description.orEmpty(),
                appUrl = momentDetailDataModel?.shortLink.orEmpty()
            )
        }
        binding.momentContainer.bookmarkImage.setOnClickListener {
            if (momentDetailDataModel?.isBookmarked == true) {
                DialogUtils.showDialogWithCallBack(
                    requireContext(),
                    message = resourceProvider.getString(R.string.delete_from_bookmark),
                    title = resourceProvider.getString(R.string.delete), positiveButtonText = resourceProvider.getString(R.string.yes),
                    negativeButtonText = resourceProvider.getString(R.string.cancel),
                    positiveButtonCallback = {
                        binding.progressIndicator.showProgressbar()
                        bookmarkViewModel.bookMarkMoment(position = AppConstants.DEFAULT_POSITION, momentId = momentDetailDataModel?._id.orEmpty())
                    }
                )
            } else {
                binding.progressIndicator.showProgressbar()
                bookmarkViewModel.bookMarkMoment(position = AppConstants.DEFAULT_POSITION, momentId = momentDetailDataModel?._id.orEmpty())
            }
        }
        binding.momentContainer.userImage.setOnClickListener {
            if (momentDetailDataModel?.addedBy?._id.orEmpty() == AppConstants.getUserID())
                findNavController().navigate(MomentDetailFragmentDirections.actionProfileFragment())
            else {
                val isOtherParent = momentDetailDataModel?.parents?.any { it._id == momentDetailDataModel?.addedBy?._id } ?: false
                findNavController().navigate(MomentDetailFragmentDirections.actionOtherUserProfileFragment(userID = momentDetailDataModel?.addedBy?._id.orEmpty(), isOtherParent = isOtherParent))
            }
        }
        binding.momentContainer.userNameText.setOnClickListener {
            if (momentDetailDataModel?.addedBy?._id.orEmpty() == AppConstants.getUserID())
                findNavController().navigate(MomentDetailFragmentDirections.actionProfileFragment())
            else {
                val isOtherParent = momentDetailDataModel?.parents?.any { it._id == momentDetailDataModel?.addedBy?._id } ?: false
                findNavController().navigate(MomentDetailFragmentDirections.actionOtherUserProfileFragment(userID = momentDetailDataModel?.addedBy?._id.orEmpty(), isOtherParent = isOtherParent))
            }

        }
        binding.momentContainer.moreImage.setOnClickListener { view ->
            val popUp = PopupMenu(binding.momentContainer.moreImage.context, view)
            if (momentDetailDataModel?.isAddedBySpouse == true)
                popUp.menuInflater.inflate(R.menu.spouse_menu, popUp.menu)
            else if (momentDetailDataModel?.addedBy?._id.orEmpty() == AppConstants.getUserID())
                popUp.menuInflater.inflate(R.menu.user_menu, popUp.menu)
            else popUp.menuInflater.inflate(R.menu.other_user_menu, popUp.menu)
            if (momentDetailDataModel?.isAddedBySpouse == true || momentDetailDataModel?.addedBy?._id.orEmpty() == AppConstants.getUserID())
                popUp.menu.findItem(R.id.actionMarkImportant).title = binding.momentContainer.moreImage.context.getString(R.string.mark_as_important_moment)
            popUp.show()
            popUp.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.actionEditMoment -> {
                        findNavController().navigate(
                            MomentDetailFragmentDirections.actionAddMomentFragment(
                                isEdit = true,
                                momentID = momentDetailDataModel?._id.orEmpty()
                            )
                        )
                    }
                    R.id.actionAddYourKid -> {
                        findNavController().navigate(
                            MomentDetailFragmentDirections.actionAddMomentFragment(
                                isSpouseAdded = true,
                                momentID = momentDetailDataModel?._id.orEmpty()
                            )
                        )
                    }
                    R.id.actionMarkImportant -> {
                        binding.progressIndicator.showProgressbar()
                        bookmarkViewModel.markMomentAsImportant(
                            position = AppConstants.DEFAULT_POSITION,
                            momentId = momentDetailDataModel?._id.orEmpty()
                        )
                    }
                    R.id.actionCopyUrl -> {
                        AppConstants.copyURL(activity, momentDetailDataModel?.shortLink)
                    }
                    R.id.actionBlock -> {
//                        binding.progressIndicator.showProgressbar()
//                        homeViewModel.blockUser(userId = momentDetailDataModel?.addedBy?._id.orEmpty(), action = APIConstants.BLOCKED)
                    }
                    R.id.actionReport -> {
                        findNavController().navigate(
                            MomentDetailFragmentDirections.actionReportFragment(
                                type = ReportType.MOMENT.name,
                                momentId = momentDetailDataModel?._id.orEmpty()
                            )
                        )
                    }

                }
                true
            }
        }

    }

    private fun setAdapter() {
        imageViewAdapter = ImageSliderAdapter(
            resourceProvider = resourceProvider,
            imageList = momentImageList,
            context = requireContext(),
            onMediaClick = { type: String, url: String, isLocal: Boolean ->
                if (type == MediaType.IMAGE.name || type == MediaType.OG_IMAGE.name) {
                    findNavController().navigate(MomentDetailFragmentDirections.actionUserImageDialog(url, isLocal = isLocal))
                } else if (type == MediaType.VIDEO.name) {
                    findNavController().navigate(MomentDetailFragmentDirections.actionVideoPlayerFragment(isLocal = isLocal, url = url))
                }
            }
        )
        binding.momentContainer.headerImageViewPager.adapter = imageViewAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshAdapter() {
        imageViewAdapter.notifyDataSetChanged()
        val dots = arrayOfNulls<ImageView>(momentImageList.size)
        binding.momentContainer.sliderDots.removeAllViews()
        if (dots.isNotEmpty() && dots.size > 1) {
            buildViewPagerSlidingDotPanels(binding.momentContainer.sliderDots, dots)
            setViewPagerPageChangeListener(binding.momentContainer.headerImageViewPager, dots)
        }
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

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

}