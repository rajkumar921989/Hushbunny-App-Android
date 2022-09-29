package com.hushbunny.app.ui.setting

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentInAppNotificationBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.dialog.DialogUtils
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class InAppNotificationSettingFragment : Fragment(R.layout.fragment_in_app_notification) {

    private var _binding: FragmentInAppNotificationBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: InAppNotificationSettingFragmentArgs by navArgs()

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
        _binding = FragmentInAppNotificationBinding.bind(view)
        initClickListener()
        updateToggleCheck()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    private fun updateToggleCheck() {
        when (navigationArgs.type) {
            resourceProvider.getString(R.string.in_app_notifications_settings) -> {
                binding.importantNotificationsSwitch.isChecked = PrefsManager.get().getBoolean(AppConstants.IN_APP_IMPORTANT_NOTIFICATION, false)
                binding.optionalNotificationsSwitch.isChecked = PrefsManager.get().getBoolean(AppConstants.IN_APP_OPTIONAL_NOTIFICATION, false)
            }
            resourceProvider.getString(R.string.email_settings) -> {
                binding.importantNotificationsSwitch.isChecked = PrefsManager.get().getBoolean(AppConstants.EMAIL_IMPORTANT_NOTIFICATION, false)
                binding.optionalNotificationsSwitch.isChecked = PrefsManager.get().getBoolean(AppConstants.EMAIL_OPTIONAL_NOTIFICATION, false)
            }
        }
        updateTintColor()
    }

    private fun updateTintColor() {
        if (binding.importantNotificationsSwitch.isChecked) {
            binding.importantNotificationsSwitch.thumbTintList = AppCompatResources.getColorStateList(requireContext(), R.color.button_color_pink)
        } else {
            binding.importantNotificationsSwitch.thumbTintList = AppCompatResources.getColorStateList(requireContext(), R.color.switch_default_color)
        }
        if (binding.optionalNotificationsSwitch.isChecked) {
            binding.optionalNotificationsSwitch.thumbTintList = AppCompatResources.getColorStateList(requireContext(), R.color.button_color_pink)
        } else {
            binding.optionalNotificationsSwitch.thumbTintList = AppCompatResources.getColorStateList(requireContext(), R.color.switch_default_color)
        }
    }

    private fun setObserver() {
        settingViewModel.notificationObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    AppConstants.saveUserDetail(it.data)
                    updateToggleCheck()
                }
                else -> {
                    updateToggleCheck()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initClickListener() {
        binding.headerContainer.pageTitle.text = navigationArgs.type

        binding.importantNotificationsSwitch.setOnClickListener {
            updateTintColor()
            updateNotificationSettings()
        }
        binding.optionalNotificationsSwitch.setOnClickListener {
            updateTintColor()
            updateNotificationSettings()
        }
        binding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()

        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.mandatorySwitch.setOnTouchListener { _, event ->
            event.actionMasked == MotionEvent.ACTION_MOVE
        }
        binding.importantNotificationsSwitch.setOnTouchListener { _, event ->
            event.actionMasked == MotionEvent.ACTION_MOVE
        }
        binding.optionalNotificationsSwitch.setOnTouchListener { _, event ->
            event.actionMasked == MotionEvent.ACTION_MOVE
        }

    }

    private fun updateNotificationSettings() {
        binding.progressIndicator.showProgressbar()
        settingViewModel.updateNotificationSetting(
            type = if (navigationArgs.type == resourceProvider.getString(R.string.in_app_notifications_settings)) APIConstants.IN_APP else APIConstants.EMAIL,
            important = binding.importantNotificationsSwitch.isChecked,
            optional = binding.optionalNotificationsSwitch.isChecked
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}