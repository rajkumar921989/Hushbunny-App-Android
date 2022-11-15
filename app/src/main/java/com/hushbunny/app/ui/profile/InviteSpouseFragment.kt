package com.hushbunny.app.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentShareWithSpouseBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.model.InviteInfoModel
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.ImageViewAndFileUtils.hideKeyboard
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class InviteSpouseFragment : Fragment(R.layout.fragment_share_with_spouse) {
    private var _binding: FragmentShareWithSpouseBinding? = null
    private val binding get() = _binding!!
    private var type = APIConstants.EMAIL
    private var inviteInfoModel: InviteInfoModel? = null
    private val navigationArgs: InviteSpouseFragmentArgs by navArgs()

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var inviteSpouseRepository: UserActionRepository

    private val inviteSpouseViewModel: EditProfileViewModel by viewModelBuilderFragmentScope {
        EditProfileViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            userActionRepository = inviteSpouseRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShareWithSpouseBinding.bind(view)
        inviteInfoModel = navigationArgs.inviteInfo
        initData()
        initClickListener()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    private fun setObserver() {
        inviteSpouseViewModel.errorValidationObserver.observe(viewLifecycleOwner) {
            when (it) {
                APIConstants.SUCCESS -> {
                    binding.progressIndicator.showProgressbar()
                    binding.root.hideKeyboard()
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
        inviteSpouseViewModel.inviteSpouseObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    setFragmentResult(APIConstants.IS_SPOUSE_INVITED, bundleOf(APIConstants.SUCCESS to true))
                    findNavController().popBackStack()
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
    }

    private fun initData() {
        inviteInfoModel?.let { data ->
            binding.emailContainer.emailInput.setText(data.email)
            binding.mobileNumberContainer.mobileNumberInput.setText(data.phoneNumber)
            binding.nameInput.setText(data.name)
        }
    }

    private fun initClickListener() {
        binding.tabContainer.emilButton.setOnClickListener {
            binding.emailContainer.container.visibility = View.VISIBLE
            binding.mobileNumberContainer.container.visibility = View.GONE
            type = APIConstants.EMAIL
            clearInput()
            binding.tabContainer.emilButton.background = ContextCompat.getDrawable(requireActivity(), R.drawable.drawable_button_white)
            binding.tabContainer.mobileNumberButton.background = null
            initData()
        }
        binding.tabContainer.mobileNumberButton.setOnClickListener {
            binding.emailContainer.container.visibility = View.GONE
            binding.mobileNumberContainer.container.visibility = View.VISIBLE
            type = APIConstants.PHONE_NUMBER
            binding.tabContainer.emilButton.background = null
            binding.tabContainer.mobileNumberButton.background = ContextCompat.getDrawable(requireActivity(), R.drawable.drawable_button_white)
            clearInput()
            initData()
        }
        binding.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.sendInviteButton.setOnClickListener {
            inviteSpouseViewModel.inviteSpouse(
                type = type,
                callingCode = binding.mobileNumberContainer.countrySelection.selectedCountryCodeWithPlus,
                email = binding.emailContainer.emailInput.text.toString().trim(),
                phoneNumber = binding.mobileNumberContainer.mobileNumberInput.text.toString().trim(),
                name = binding.nameInput.text.toString().trim(), kidId = navigationArgs.kidId
            )
        }
    }

    private fun clearInput() {
        binding.emailContainer.emailInput.setText("")
        binding.mobileNumberContainer.mobileNumberInput.setText("")
        binding.nameInput.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}