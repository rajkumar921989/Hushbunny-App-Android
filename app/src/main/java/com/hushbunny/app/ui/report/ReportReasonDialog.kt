package com.hushbunny.app.ui.report

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hushbunny.app.R
import com.hushbunny.app.databinding.DialogReportBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.ReportListingModel
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.ui.sealedclass.ReportReasonInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.dialog.SuccessDialog
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ReportReasonDialog : DialogFragment(R.layout.dialog_report) {
    private var _binding: DialogReportBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: ReportReasonDialogArgs by navArgs()
    lateinit var reportAdapter: ReportAdapter
    var selectedReason: ReportListingModel? = null

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var momentRepository: MomentRepository

    private val reportViewModel: ReportViewModel by viewModelBuilderFragmentScope {
        ReportViewModel(
            ioDispatcher = Dispatchers.IO,
            momentRepository = momentRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun getTheme(): Int {
        return R.style.AppDialogStyle
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState).apply {
            val window = this.window;
            val width = (context.resources.displayMetrics.widthPixels * 0.80).toInt()
            val height = (context.resources.displayMetrics.heightPixels * 0.40).toInt()
            window?.setLayout(width, height)
            window?.setGravity(Gravity.CENTER)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DialogReportBinding.bind(view)
        initClickListener()
        setAdapter()
        setObserver()
        getReport()
    }

    private fun getReport() {
        binding.reportListShimmer.visibility = View.VISIBLE
        binding.reportReasonList.visibility = View.GONE
        reportViewModel.getReportReason(
            type = navigationArgs.type
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver() {
        reportViewModel.reportObserver.observe(viewLifecycleOwner) {
            binding.reportListShimmer.visibility = View.GONE
            binding.reportReasonList.visibility = View.VISIBLE
            when (it) {
                is ReportReasonInfo.HaveList -> {
                    reportAdapter.submitList(it.reasonList)
                    reportAdapter.notifyDataSetChanged()
                }
                is ReportReasonInfo.Error -> {
                    DialogUtils.showErrorDialog(
                        requireActivity(),
                        buttonText = resourceProvider.getString(R.string.ok),
                        message = it.message,
                        title = resourceProvider.getString(R.string.app_name)
                    )
                }
                is ReportReasonInfo.ApiError -> {
                    activity?.let { it1 -> DialogUtils.sessionExpiredDialog(it1) }
                }
            }
        }
        reportViewModel.createReportObserver.observe(viewLifecycleOwner) {
            binding.progressIndicator.hideProgressbar()
            when (it.statusCode) {
                APIConstants.API_RESPONSE_200 -> {
                    val dialog = SuccessDialog(requireContext())
                    dialog.show()
                    dialog.setMessage(resourceProvider.getString(R.string.report_successfully))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        findNavController().popBackStack()
                    }, 2000)
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

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {
        reportAdapter = ReportAdapter(onItemClick = {
            selectedReason = it
            reportAdapter.notifyDataSetChanged()
        })
        binding.reportReasonList.adapter = reportAdapter
    }

    private fun initClickListener() {
        binding.confirmButton.setOnClickListener {
            if (selectedReason?._id.orEmpty().isEmpty()) {
                DialogUtils.showErrorDialog(
                    requireActivity(),
                    buttonText = resourceProvider.getString(R.string.ok),
                    message = resourceProvider.getString(R.string.reason_error_message),
                    title = resourceProvider.getString(R.string.app_name)
                )
            } else {
                binding.progressIndicator.showProgressbar()
                reportViewModel.postReport(
                    reasonId = selectedReason?._id.orEmpty(),
                    commentId = navigationArgs.commentId,
                    momentId = navigationArgs.momentId,
                    reason = selectedReason?.reason,
                    userId = navigationArgs.userId, type = navigationArgs.type
                )
            }

        }
    }


}