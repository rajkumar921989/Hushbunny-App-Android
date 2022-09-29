package com.hushbunny.app.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.databinding.ItemReportListBinding
import com.hushbunny.app.ui.model.ReportListingModel
import com.hushbunny.app.uitls.BaseListAdapter

class ReportAdapter(private val onItemClick: ((ReportListingModel) -> Unit)? = null) :
    BaseListAdapter<ReportListingModel, ItemReportListBinding>(ItemDiffCallback()) {
    var selectedPosition = -1
    override fun createBinding(parent: ViewGroup): ItemReportListBinding {
        return ItemReportListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bind(binding: ItemReportListBinding, item: ReportListingModel, position: Int) {
        binding.radioButton.text = item.reason
        binding.radioButton.isChecked = selectedPosition == position
        binding.radioButton.setOnClickListener {
            selectedPosition = position
            onItemClick?.invoke(item)
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<ReportListingModel>() {
        override fun areItemsTheSame(oldItem: ReportListingModel, newItem: ReportListingModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ReportListingModel, newItem: ReportListingModel): Boolean {
            return oldItem == newItem
        }
    }
}