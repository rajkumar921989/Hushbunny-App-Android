package com.hushbunny.app.ui.setting

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentSettingsBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.dialog.DialogUtils
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class SettingFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var userActionRepository: UserActionRepository

    private val settingViewModel: SettingViewModel by viewModelBuilderFragmentScope {
        SettingViewModel(
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
        _binding = FragmentSettingsBinding.bind(view)
        setAdapter()
        initializeClickListener()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.VISIBLE)
    }

    private fun setObserver() {
        settingViewModel.userActionResponseObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
                        FirebaseMessaging.getInstance().token
                        AppConstants.navigateToLoginPage(requireActivity())
                    }.addOnCanceledListener {
                        AppConstants.navigateToLoginPage(requireActivity())
                    }.addOnFailureListener {
                        AppConstants.navigateToLoginPage(requireActivity())
                    }
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
    }

    private fun initializeClickListener() {
        binding.logoutButton.setOnClickListener {
            val dialog = SettingActionDialog(requireContext(), resourceProvider.getString(R.string.logout)) {
                binding.progressIndicator.showProgressbar()
                settingViewModel.updateUserAction(resourceProvider.getString(R.string.logout))
            }
            dialog.show()
        }
        binding.deactivateAccountButton.setOnClickListener {
            val dialog = SettingActionDialog(requireContext(), resourceProvider.getString(R.string.deactivate_account)) {
                binding.progressIndicator.showProgressbar()
                settingViewModel.updateUserAction(resourceProvider.getString(R.string.deactivate_account))
            }
            dialog.show()
        }
        binding.deleteAccountButton.setOnClickListener {
            val dialog = SettingActionDialog(requireContext(), resourceProvider.getString(R.string.delete_account)) {
                binding.progressIndicator.showProgressbar()
                settingViewModel.updateUserAction(resourceProvider.getString(R.string.delete_account))
            }
            dialog.show()
        }
        binding.seeHowHushbunnyWorksButton.setOnClickListener {
            findNavController().navigate(SettingFragmentDirections.actionIntroScreenFragment())
        }
        binding.shareAppButton.setOnClickListener {
            AppConstants.shareTheAPP(
                requireActivity(),
                title = resourceProvider.getString(R.string.share_the_app),
                extraMessage = "",
                appUrl = resourceProvider.getString(R.string.share_the_app_link)
            )
        }
    }

    private fun setAdapter() {
        val adapter = SettingAdapter(onItemClick = {
            when (it) {
                resourceProvider.getString(R.string.edit_profile) -> findNavController().navigate(SettingFragmentDirections.actionEditProfileFragment())
                resourceProvider.getString(R.string.change_password) -> findNavController().navigate(SettingFragmentDirections.actionChangePasswordFragment())
                resourceProvider.getString(R.string.kids_list) -> findNavController().navigate(SettingFragmentDirections.actionKidsListFragment())
                resourceProvider.getString(R.string.in_app_notifications_settings) -> findNavController().navigate(
                    SettingFragmentDirections.actionInAppNotificationFragment(
                        it
                    )
                )
                resourceProvider.getString(R.string.email_settings) -> findNavController().navigate(
                    SettingFragmentDirections.actionInAppNotificationFragment(
                        it
                    )
                )
                resourceProvider.getString(R.string.blocked_list) -> findNavController().navigate(SettingFragmentDirections.actionBlockedUserFragment())
                resourceProvider.getString(R.string.bookmarks) -> findNavController().navigate(SettingFragmentDirections.actionBookMarkFragment(it))
                resourceProvider.getString(R.string.important_moments) -> findNavController().navigate(
                    SettingFragmentDirections.actionBookMarkFragment(it)
                )
            }

        })
        binding.settingList.adapter = adapter
        adapter.submitList(AppConstants.getSettingList(resourceProvider = resourceProvider))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}