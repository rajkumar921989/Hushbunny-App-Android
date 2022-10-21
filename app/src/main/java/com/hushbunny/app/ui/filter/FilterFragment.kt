package com.hushbunny.app.ui.filter

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentFilterBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.enumclass.FilterType
import com.hushbunny.app.ui.model.FilterModel
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.DateFormatUtils.convertIntoFilterDateFormat
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class FilterFragment : Fragment(R.layout.fragment_filter) {
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: FilterFragmentArgs by navArgs()
    var type = ""

    @Inject
    lateinit var resourceProvider: ResourceProvider
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilterBinding.bind(view)
        initView()
        updateFilterModel()
        initializeClickListener()
    }

    private fun initView() {
        binding.header.clearFilterButton.visibility = View.VISIBLE
        binding.header.pageTitle.text = resourceProvider.getString(R.string.filter)
        binding.header.backImage.setImageResource(R.drawable.ic_close)
    }

    private fun initializeClickListener() {
        binding.filterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?, position: Int, id: Long
            ) {
                when (position) {
                    1 -> {
                        clearSelection()
                        type = FilterType.DATE.name
                        updateFilterSelection(FilterType.DATE.name)
                    }
                    2 -> {
                        clearSelection()
                        type = FilterType.MONTH.name
                        updateFilterSelection(FilterType.MONTH.name)
                    }
                    3 -> {
                        clearSelection()
                        type = FilterType.YEAR.name
                        updateFilterSelection(FilterType.YEAR.name)
                    }
                    4 -> {
                        clearSelection()
                        type = FilterType.DATE_RANGE.name
                        updateFilterSelection(FilterType.DATE_RANGE.name)
                    }
                    5 -> {
                        clearSelection()
                        type = FilterType.MONTH_RANGE.name
                        updateFilterSelection(FilterType.MONTH_RANGE.name)
                    }
                    6 -> {
                        clearSelection()
                        type = FilterType.YEAR_RANGE.name
                        updateFilterSelection(FilterType.YEAR_RANGE.name)
                    }
                    else -> {
                        type = FilterType.NO_SELECTION.name
                        updateFilterSelection(FilterType.NO_SELECTION.name)
                        clearSelection()
                    }
                }
                binding.filterSelectionText.text = binding.filterTypeSpinner.selectedItem.toString()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }
        binding.header.clearFilterButton.setOnClickListener {
            type = FilterType.NO_SELECTION.name
            updateFilterSelection(FilterType.NO_SELECTION.name)
            clearSelection()
        }
        binding.filterSelectionText.setOnClickListener {
            binding.filterTypeSpinner.performClick()
        }
        binding.dateText.setOnClickListener {
            showCalender(type)
        }
        binding.fromDateText.setOnClickListener {
            when (type) {
                FilterType.MONTH_RANGE.name -> showCalender(FilterType.FROM_MONTH_RANGE.name)
                FilterType.YEAR_RANGE.name -> showCalender(FilterType.FROM_YEAR_RANGE.name)
                else -> showCalender(FilterType.FROM_DATE_RANGE.name)
            }
        }
        binding.toDateText.setOnClickListener {
            when (type) {
                FilterType.MONTH_RANGE.name -> showCalender(FilterType.TO_MONTH_RANGE.name)
                FilterType.YEAR_RANGE.name -> showCalender(FilterType.TO_YEAR_RANGE.name)
                else -> showCalender(FilterType.TO_DATE_RANGE.name)
            }
        }
        binding.header.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.submitButton.setOnClickListener {
            if ((type == FilterType.YEAR_RANGE.name && binding.fromDateText.text.toString().trim().isEmpty())
                || (type == FilterType.MONTH_RANGE.name && binding.fromDateText.text.toString().trim().isEmpty())
                || (type == FilterType.DATE_RANGE.name && binding.fromDateText.text.toString().trim().isEmpty())
            ) {
                DialogUtils.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = resourceProvider.getString(R.string.from_date_error),
                    title = resourceProvider.getString(R.string.app_name)
                )
            } else if ((type == FilterType.YEAR_RANGE.name && binding.toDateText.text.toString().trim().isEmpty())
                || (type == FilterType.MONTH_RANGE.name && binding.toDateText.text.toString().trim().isEmpty())
                || (type == FilterType.DATE_RANGE.name && binding.toDateText.text.toString().trim().isEmpty())
            ) {
                DialogUtils.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = resourceProvider.getString(R.string.to_date_error),
                    title = resourceProvider.getString(R.string.app_name)
                )
            } else if (type == FilterType.DATE.name && binding.dateText.text.toString().trim().isEmpty()) {
                DialogUtils.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = resourceProvider.getString(R.string.date_error),
                    title = resourceProvider.getString(R.string.app_name)
                )
            } else if (type == FilterType.MONTH.name && binding.dateText.text.toString().trim().isEmpty()) {
                DialogUtils.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = resourceProvider.getString(R.string.month_error),
                    title = resourceProvider.getString(R.string.app_name)
                )
            } else if (type == FilterType.YEAR.name && binding.dateText.text.toString().trim().isEmpty()) {
                DialogUtils.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = resourceProvider.getString(R.string.year_error),
                    title = resourceProvider.getString(R.string.app_name)
                )
            } else {
                val filterModel = FilterModel(
                    type = type,
                    date = binding.dateText.text.toString().trim(),
                    fromDate = binding.fromDateText.text.toString().trim(),
                    toDate = binding.toDateText.text.toString().trim()
                )
                val bundle = bundleOf()
                bundle.putSerializable(APIConstants.FILTER_MODEL, filterModel)
                setFragmentResult(APIConstants.IS_FILTER_APPLIED, bundle)
                findNavController().popBackStack()
            }
        }
    }

    private fun updateFilterModel() {
        when (navigationArgs.filterModel?.type) {
            FilterType.DATE.name -> binding.filterTypeSpinner.setSelection(1)
            FilterType.MONTH.name -> binding.filterTypeSpinner.setSelection(2)
            FilterType.YEAR.name -> binding.filterTypeSpinner.setSelection(3)
            FilterType.DATE_RANGE.name -> binding.filterTypeSpinner.setSelection(4)
            FilterType.MONTH_RANGE.name -> binding.filterTypeSpinner.setSelection(5)
            FilterType.YEAR_RANGE.name -> binding.filterTypeSpinner.setSelection(6)
            else -> binding.filterTypeSpinner.setSelection(0)
        }
        Handler(Looper.getMainLooper()).postDelayed({
                binding.dateText.text = navigationArgs.filterModel?.date.orEmpty()
                binding.fromDateText.text = navigationArgs.filterModel?.fromDate.orEmpty()
                binding.toDateText.text = navigationArgs.filterModel?.toDate.orEmpty()
            }, 200)
    }

    private fun clearSelection() {
        binding.filterSelectionText.text = resourceProvider.getString(R.string.select_filter_here)
        binding.dateText.text = ""
        binding.fromDateText.text = ""
        binding.toDateText.text = ""
    }

    private fun updateFilterSelection(type: String) {
        binding.dateGroup.visibility = View.GONE
        binding.dateRangeGroup.visibility = View.GONE
        binding.header.clearFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.filter_text_color))
        binding.header.clearFilterButton.isClickable = false
        binding.header.clearFilterButton.isEnabled = false
        when (type) {
            FilterType.DATE.name -> {
                binding.dateText.hint = resourceProvider.getString(R.string.date_of_birth_hint)
                binding.dateGroup.visibility = View.VISIBLE
                binding.header.clearFilterButton.isClickable = true
                binding.header.clearFilterButton.isEnabled = true
                binding.header.clearFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
            }
            FilterType.MONTH.name -> {
                binding.dateText.hint = resourceProvider.getString(R.string.month_filter_hint)
                binding.dateGroup.visibility = View.VISIBLE
                binding.header.clearFilterButton.isClickable = true
                binding.header.clearFilterButton.isEnabled = true
                binding.header.clearFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
            }
            FilterType.YEAR.name -> {
                binding.dateText.hint = resourceProvider.getString(R.string.year_filter_hint)
                binding.dateGroup.visibility = View.VISIBLE
                binding.header.clearFilterButton.isClickable = true
                binding.header.clearFilterButton.isEnabled = true
                binding.header.clearFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
            }
            FilterType.DATE_RANGE.name -> {
                binding.fromDateText.hint = resourceProvider.getString(R.string.date_of_birth_hint)
                binding.toDateText.hint = resourceProvider.getString(R.string.date_of_birth_hint)
                binding.dateRangeGroup.visibility = View.VISIBLE
                binding.header.clearFilterButton.isClickable = true
                binding.header.clearFilterButton.isEnabled = true
                binding.header.clearFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
            }
            FilterType.MONTH_RANGE.name -> {
                binding.fromDateText.hint = resourceProvider.getString(R.string.month_filter_hint)
                binding.toDateText.hint = resourceProvider.getString(R.string.month_filter_hint)
                binding.dateRangeGroup.visibility = View.VISIBLE
                binding.header.clearFilterButton.isClickable = true
                binding.header.clearFilterButton.isEnabled = true
                binding.header.clearFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
            }
            FilterType.YEAR_RANGE.name -> {
                binding.fromDateText.hint = resourceProvider.getString(R.string.year_filter_hint)
                binding.toDateText.hint = resourceProvider.getString(R.string.year_filter_hint)
                binding.dateRangeGroup.visibility = View.VISIBLE
                binding.header.clearFilterButton.isClickable = true
                binding.header.clearFilterButton.isEnabled = true
                binding.header.clearFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_gray))
            }
        }
    }

    private fun showDateMonthYearPicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        context?.let { withContext ->
            SpinnerDatePickerDialogBuilder()
                .context(withContext)
                .callback { _, year, monthOfYear, dayOfMonth ->
                    textView.text = "$dayOfMonth/${monthOfYear + 1}/$year".convertIntoFilterDateFormat(SimpleDateFormat("dd MMM, yyyy"))
                }
                .onCancel {  }
                .showTitle(false)
                .defaultDate(calendar.get(
                    Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
                .build()
                .show()
        }
    }

    private fun showMonthYearPicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        context?.let { withContext ->
            SpinnerDatePickerDialogBuilder()
                .context(withContext)
                .callback { _, year, monthOfYear, dayOfMonth ->
                    textView.text = "$dayOfMonth/${monthOfYear + 1}/$year".convertIntoFilterDateFormat(SimpleDateFormat("MMM, yyyy"))
                }
                .onCancel {  }
                .showDaySpinner(false)
                .showTitle(false)
                .defaultDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
                .build()
                .show()
        }
    }

    private fun showYearPicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        context?.let { withContext ->
            SpinnerDatePickerDialogBuilder()
                .context(withContext)
                .callback { _, year, monthOfYear, dayOfMonth ->
                    textView.text = "$dayOfMonth/${monthOfYear + 1}/$year".convertIntoFilterDateFormat(SimpleDateFormat("yyyy"))
                }
                .onCancel {  }
                .showDaySpinner(false)
                .showTitle(false)
                .showMonthSpinner(false)
                .defaultDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
                .build()
                .show()
        }
    }

    private fun showCalender(type: String) {
        when (type) {
            FilterType.DATE.name -> {
                showDateMonthYearPicker(binding.dateText)
            }
            FilterType.MONTH.name -> {
                showMonthYearPicker(binding.dateText)
            }
            FilterType.YEAR.name -> {
                showYearPicker(binding.dateText)
            }
            FilterType.FROM_DATE_RANGE.name -> {
                showDateMonthYearPicker(binding.fromDateText)
            }
            FilterType.TO_DATE_RANGE.name -> {
                showDateMonthYearPicker(binding.toDateText)
            }
            FilterType.FROM_MONTH_RANGE.name -> {
                showMonthYearPicker(binding.fromDateText)
            }
            FilterType.TO_MONTH_RANGE.name -> {
                showMonthYearPicker(binding.toDateText)
            }
            FilterType.FROM_YEAR_RANGE.name -> {
                showYearPicker(binding.fromDateText)
            }
            FilterType.TO_YEAR_RANGE.name -> {
                showYearPicker(binding.toDateText)
            }
        }
    }
}