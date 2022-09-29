package com.hushbunny.app.ui.kids

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentKidsListBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.home.HomeViewModel
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class KidsListFragment : Fragment(R.layout.fragment_kids_list) {
    private var _binding: FragmentKidsListBinding? = null
    private val binding get() = _binding!!
    private lateinit var kidsAdapter: KidsListAdapter

    @Inject
    lateinit var addKidsRepository: HomeRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    private val kidsViewModel: HomeViewModel by viewModelBuilderFragmentScope {
        HomeViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            homeRepository = addKidsRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentKidsListBinding.bind(view)
        setAdapter()
        setObserver()
        getKidsList()
        initializeClickListener()
    }

    private fun initializeClickListener() {
        binding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.kids_list)

        binding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.addMoreKidButton.setOnClickListener {
            findNavController().navigate(KidsListFragmentDirections.actionAddKidFragment(isEditKid = false))
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.emptyViewContainer.addKidButton.setOnClickListener {
            findNavController().navigate(KidsListFragmentDirections.actionAddKidFragment(isEditKid = false))
        }
        binding.pullRefresh.setOnRefreshListener {
            binding.pullRefresh.isRefreshing = false
            binding.kidsList.visibility = View.GONE
            getKidsList()
        }
    }

    private fun getKidsList() {
        binding.shimmerContainer.visibility = View.VISIBLE
        kidsViewModel.getKidsList()
    }

    private fun setObserver() {
        kidsViewModel.kidsListObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                binding.shimmerContainer.visibility = View.GONE
                when (response) {
                    is KidsStatusInfo.UserList -> {
                        if (response.userList.size == 1) {
                            binding.emptyViewContainer.welcomeMessageText.text =
                                resourceProvider.getString(R.string.lets_bloom_your_family_tree_starting_with_your_kids)
                            binding.emptyViewContainer.container.visibility = View.VISIBLE
                            binding.emptyViewContainer.addMomentButton.visibility = View.GONE
                            binding.kidsList.visibility = View.GONE
                            binding.addMoreKidButton.visibility = View.GONE
                        } else {
                            binding.kidsList.visibility = View.VISIBLE
                            binding.addMoreKidButton.visibility = View.VISIBLE
                            binding.emptyViewContainer.container.visibility = View.GONE
                            kidsAdapter.submitList(response.userList.subList(fromIndex = 1, response.userList.size))
                        }
                    }
                    else -> {
                        activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                    }
                }
            }
        }
    }

    private fun setAdapter() {
        kidsAdapter = KidsListAdapter(resourceProvider = resourceProvider, onEditProfileClick = {
            findNavController().navigate(KidsListFragmentDirections.actionAddKidFragment(isEditKid = true, kidsDetail = it))
        }, onViewProfileClick = {
            findNavController().navigate(KidsListFragmentDirections.actionKidsProfileFragment(kidId = it._id.orEmpty()))
        })
        binding.kidsList.adapter = kidsAdapter
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