package com.hushbunny.app.ui.comment

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
import com.hushbunny.app.databinding.FragmentCommentBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.ReportType
import com.hushbunny.app.ui.model.CommentModel
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.CommentDeletedResponseInfo
import com.hushbunny.app.ui.sealedclass.CommentResponseInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CommentFragment : Fragment(R.layout.fragment_comment) {
    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    lateinit var commentListAdapter: CommentListAdapter
    private val navigationArgs: CommentFragmentArgs by navArgs()
    private var commentList = ArrayList<CommentModel>()
    private var isLoading = true
    var currentPage = 1
    var momentId = ""

    @Inject
    lateinit var momentRepository: MomentRepository

    @Inject
    lateinit var fileUploadRepository: FileUploadRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    private val commentViewModel: CommentViewModel by viewModelBuilderFragmentScope {
        CommentViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            momentRepository = momentRepository
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
        _binding = FragmentCommentBinding.bind(view)
        currentPage = 1
        initClickListener()
        setAdapter()
        setObserver()
        getCommentList(true)
    }

    private fun setObserver() {
        commentViewModel.reportObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {

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
        commentViewModel.commentListObserver.observe(viewLifecycleOwner) {
            binding.commentInput.setText("")
            binding.progressIndicator.hideProgressbar()
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is CommentResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                    is CommentResponseInfo.NoComment -> {
                        isLoading = true
                        loadCommentList()
                    }
                    is CommentResponseInfo.HaveError -> {
                        isLoading = true
                        DialogUtils.showErrorDialog(
                            requireActivity(),
                            buttonText = resourceProvider.getString(R.string.ok),
                            message = response.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )
                    }
                    is CommentResponseInfo.HaveCommentList -> {
                        if (response.type == "Post Comment" || currentPage == 1) {
                            commentList.clear()
                            currentPage = 1
                        }
                        isLoading = response.CommentList.size < 30
                        commentList.addAll(response.CommentList)
                        loadCommentList()
                    }
                }
            }
        }
        commentViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
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
        commentViewModel.deleteCommentObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it) {
                is CommentDeletedResponseInfo.CommentDelete -> {
                    commentList.removeAt(it.position)
                    loadCommentList()
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

    private fun loadCommentList() {
        binding.commentListShimmer.visibility = View.GONE
        if (commentList.isNotEmpty()) {
            binding.noUserFoundText.visibility = View.GONE
            binding.commentList.visibility = View.VISIBLE
            commentListAdapter.submitList(commentList.toList())
        } else {
            binding.commentList.visibility = View.GONE
            binding.noUserFoundText.visibility = View.VISIBLE
        }
    }

    private fun setAdapter() {
        commentListAdapter = CommentListAdapter(onDeleteClick = { position: Int, item: CommentModel ->
            binding.progressIndicator.showProgressbar()
            commentViewModel.deleteComment(position, commentId = item._id.orEmpty())
        }, onReportClick = { position: Int, item: CommentModel ->
            findNavController().navigate(
                CommentFragmentDirections.actionReportFragment(
                    type = ReportType.COMMENT.name,
                    commentId = item._id.orEmpty()
                )
            )
        }, onUserClick = {
            if (it.commentBy?._id.orEmpty() == AppConstants.getUserID()) {
                findNavController().navigate(CommentFragmentDirections.actionProfileFragment())
            } else {
                findNavController().navigate(CommentFragmentDirections.actionOtherUserProfileFragment(it.commentBy?._id.orEmpty()))
            }
        })
        binding.commentList.adapter = commentListAdapter
    }

    private fun initClickListener() {
        binding.headerContainer.backImage.setImageResource(R.drawable.ic_close)
        binding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.comments)
        binding.noUserFoundText.text = resourceProvider.getString(R.string.no_comment_found)
        binding.pullRefresh.setOnRefreshListener {
            binding.pullRefresh.isRefreshing = false
            currentPage = 1
            commentList.clear()
            getCommentList(true)
        }
        binding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.sendCommentImage.setOnClickListener {
            binding.root.hideKeyboard()
            commentViewModel.addNewComment(comment = binding.commentInput.text.toString().trim(), momentId = navigationArgs.momentID)
        }
        binding.commentList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.commentList.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount == lastVisibleItem) {
                    ++currentPage
                    getCommentList(false)
                }
            }
        })
    }

    private fun getCommentList(isRequiredShimmer: Boolean) {
        if (isRequiredShimmer) {
            binding.commentList.visibility = View.GONE
            binding.commentListShimmer.visibility = View.VISIBLE
        }
        isLoading = true
        commentViewModel.getCommentList(currentPage = currentPage, momentId = momentId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }
}