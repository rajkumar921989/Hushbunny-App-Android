package com.hushbunny.app.ui.moment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentMomentDateBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.DateFormatUtils.convertDateIntoAppDateFormat
import java.util.*
import javax.inject.Inject

class MomentDateFragment : Fragment(R.layout.fragment_moment_date) {
    private var _binding: FragmentMomentDateBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: MomentDateFragmentArgs by navArgs()

    @Inject
    lateinit var resourceProvider: ResourceProvider


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMomentDateBinding.bind(view)
        initializeClickListener()
        initView()

    }

    private fun initView() {
        binding.todayContainer.nameText.text = resourceProvider.getString(R.string.today)
        if (navigationArgs.isCurrentDate) {
            binding.otherDateContainer.nameText.text = resourceProvider.getString(R.string.select_date_of_this_moment)
            binding.todayContainer.radioButton.isChecked = true
            binding.otherDateContainer.radioButton.isChecked = false
        } else {
            binding.otherDateContainer.nameText.text =
                navigationArgs.otherDate.ifEmpty { resourceProvider.getString(R.string.select_date_of_this_moment) }
            binding.todayContainer.radioButton.isChecked = false
            binding.otherDateContainer.radioButton.isChecked = true
        }
    }


    private fun initializeClickListener() {
        binding.closeImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.todayContainer.container.setOnClickListener {
            binding.todayContainer.radioButton.isChecked = true
            binding.otherDateContainer.radioButton.isChecked = false
        }
        binding.otherDateContainer.container.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    binding.todayContainer.radioButton.isChecked = false
                    binding.otherDateContainer.radioButton.isChecked = true
                    binding.otherDateContainer.nameText.text = "$dayOfMonth/${monthOfYear + 1}/$year".convertDateIntoAppDateFormat()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }
        binding.submitButton.setOnClickListener {
            val bundle = bundleOf()
            bundle.putString(AppConstants.DATE, if(binding.todayContainer.radioButton.isChecked) setCurrentDate() else binding.otherDateContainer.nameText.text.toString())
            setFragmentResult(AppConstants.MOMENT_DATE, bundle)
            findNavController().popBackStack()
        }
    }

    private fun setCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}".convertDateIntoAppDateFormat()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

}