package com.hushbunny.app.ui.blockeduser

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentBlockedListBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.sealedclass.BlockedUserList
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class BlockedUserFragment : Fragment(R.layout.fragment_blocked_list) {

    private var _binding: FragmentBlockedListBinding? = null
    private val binding get() = _binding!!
    private lateinit var blockedUserAdapter: BlockedUserAdapter

    @Inject
    lateinit var homeRepository: HomeRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider

    private val blockedUserViewModel: BlockedUserViewModel by viewModelBuilderFragmentScope {
        BlockedUserViewModel(
            ioDispatcher = Dispatchers.IO,
            homeRepository = homeRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBlockedListBinding.bind(view)
        binding.headerContainer
        setAdapter()
        setObserver()
        initClickListener()
        getBlockedUserList()
    }

    private fun getBlockedUserList() {
        binding.progressIndicator.showProgressbar()
        blockedUserViewModel.getBlockedUserList()
    }

    private fun setAdapter() {
        blockedUserAdapter = BlockedUserAdapter(onItemClick = {
            binding.progressIndicator.showProgressbar()
            blockedUserViewModel.blockAndUnblockUser(userId = it._id.orEmpty(), action = APIConstants.UNBLOCKED)

        })
        binding.blockList.adapter = blockedUserAdapter
    }

    private fun initClickListener() {
        binding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.blocked_list)
        binding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }

    private fun setObserver() {
        blockedUserViewModel.blockedUserListObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it) {
                is BlockedUserList.UserList -> {
                    blockedUserAdapter.submitList(it.userList)
                    binding.blockList.visibility = View.VISIBLE
                    binding.noUserFoundText.visibility = View.GONE
                }
                is BlockedUserList.Error -> {
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it.message,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
                else -> {
                    binding.blockList.visibility = View.GONE
                    binding.noUserFoundText.visibility = View.VISIBLE
                }
            }
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
}