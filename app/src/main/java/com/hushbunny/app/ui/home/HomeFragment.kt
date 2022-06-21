package com.hushbunny.app.ui.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentHomeBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var kidsAdapter: KidsAdapter

    @Inject
    lateinit var homeRepository: HomeRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    var currentPage = 1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    private val homeViewModel: HomeViewModel by viewModelBuilderFragmentScope {
        HomeViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            homeRepository = homeRepository
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        setAdapter()
        getKidsList()
        setObserver()
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.VISIBLE)
    }

    private fun initClickListener() {
        binding.emptyViewContainer.container.visibility = View.VISIBLE
        binding.emptyViewContainer.welcomeMessageText.text =
            resourceProvider.getString(R.string.home_page_welcome_message, AppConstants.getUserFirstName())
        binding.emptyViewContainer.addKidButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionAddKidFragment(isEditKid = false))
        }
    }

    private fun setAdapter() {
        kidsAdapter = KidsAdapter(addKidsClick = {
            findNavController().navigate(HomeFragmentDirections.actionAddKidFragment(isEditKid = false))
        }, kidsClick = {
            findNavController().navigate(HomeFragmentDirections.actionAddKidFragment(isEditKid = true, kidsDetail = it))
        }, addSpouseClick = {
            findNavController().navigate(HomeFragmentDirections.actionInviteSpouseFragment(kidId = it))
        })
        binding.kidsList.adapter = kidsAdapter
    }

    private fun setObserver() {
        homeViewModel.kidsListObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it) {
                is KidsStatusInfo.UserList -> {
                    kidsAdapter.submitList(it.userList)
                }
                else -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
            }
        }
    }

    private fun getKidsList() {
        binding.progressIndicator.showProgressbar()
        homeViewModel.getKidsList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}