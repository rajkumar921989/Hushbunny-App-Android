package com.hushbunny.app.ui.reaction

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentReactionListingBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.ReactionPageName
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.ReactionResponseInfo
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderActivityScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ReactionListingFragment : Fragment(R.layout.fragment_reaction_listing) {
    private var _binding: FragmentReactionListingBinding? = null
    private val binding get() = _binding!!
    var pageName = ""
    var momentId = ""
    private var isLoading = true
    private lateinit var reactionListAdapter: ReactionListAdapter

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var momentRepository: MomentRepository

    private val reactionViewModel: ReactionViewModel by viewModelBuilderActivityScope {
        ReactionViewModel(
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
        pageName = arguments?.getString(PAGE_NAME).orEmpty()
        momentId = arguments?.getString(MOMENT_ID).orEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReactionListingBinding.bind(view)
        initClickListener()
        setAdapter()
        setObserver()
        if (pageName == ReactionPageName.ALL.name)
            getReactionList(true)
    }

    private fun setAdapter() {
        reactionListAdapter = ReactionListAdapter(onItemClick = {
            if (it.reactedBy?._id.orEmpty() == AppConstants.getUserID())
                findNavController().navigate(ReactionLandingFragmentDirections.actionProfileFragment())
            else findNavController().navigate(ReactionLandingFragmentDirections.actionOtherUserProfileFragment(userID = it.reactedBy?._id.orEmpty()))
        })
        binding.reactionList.adapter = reactionListAdapter
    }

    private fun setObserver() {
        reactionViewModel.reactionListObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.reactionShimmer.visibility = View.GONE
                when (response) {
                    is ReactionResponseInfo.HaveReactionList -> {
                        isLoading = response.CommentList.size < 30
                        reactionViewModel.reactionList.addAll(response.CommentList)
                        reactionViewModel.setReactionCount(response.reactionCount)
                        loadReactionList(pageName)
                    }
                    is ReactionResponseInfo.ApiError -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                    is ReactionResponseInfo.HaveError -> {
                        isLoading = true
                        loadReactionList(pageName)
                        DialogUtils.showErrorDialog(
                            requireActivity(),
                            buttonText = resourceProvider.getString(R.string.ok),
                            message = response.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )

                    }
                    is ReactionResponseInfo.NoReaction -> {
                        reactionViewModel.setReactionCount(response.reactionCount)
                        isLoading = true
                        loadReactionList(pageName)
                    }
                }
            }

        }
        reactionViewModel.reactionTypeObserver.observe(viewLifecycleOwner) {
            if (!binding.reactionShimmer.isVisible)
                loadReactionList(it)
        }
    }

    private fun initClickListener() {
        binding.reactionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.reactionList.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount == lastVisibleItem) {
                    if (pageName == ReactionPageName.ALL.name) {
                        ++reactionViewModel.currentPage
                        getReactionList(false)
                    }
                }
            }
        })
    }

    private fun getReactionList(isRequiredShimmer: Boolean) {
        if (isRequiredShimmer) {
            binding.reactionShimmer.visibility = View.VISIBLE
            binding.reactionList.visibility = View.GONE
        }
        isLoading = true
        reactionViewModel.getReactionList(currentPage = reactionViewModel.currentPage, momentId = momentId)
    }

    private fun loadReactionList(pageName: String) {
        val reactionList = reactionViewModel.getReactionList(pageName)
        if (reactionList.isNotEmpty()) {
            binding.reactionList.visibility = View.VISIBLE
            binding.noReactionFoundText.visibility = View.GONE
            reactionListAdapter.submitList(reactionList)
        } else {
            binding.reactionList.visibility = View.GONE
            binding.noReactionFoundText.visibility = View.VISIBLE
            binding.noReactionFoundText.text = reactionViewModel.getReactionMessage()
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PAGE_NAME = "page_name"
        private const val MOMENT_ID = "moment_id"
        fun getInstance(pageName: String, moment_id: String): ReactionListingFragment {
            val args = Bundle()
            args.putString(PAGE_NAME, pageName)
            args.putString(MOMENT_ID, moment_id)
            val fragment = ReactionListingFragment()
            fragment.arguments = args
            return fragment
        }
    }
}