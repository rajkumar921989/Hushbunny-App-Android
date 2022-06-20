package com.hushbunny.app.ui.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.databinding.ItemSettingsBinding
import com.hushbunny.app.ui.model.SettingsModel
import com.hushbunny.app.uitls.BaseListAdapter

class SettingAdapter(private val onItemClick: ((String) -> Unit)? = null) :
    BaseListAdapter<SettingsModel, ItemSettingsBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemSettingsBinding {
        return ItemSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bind(binding: ItemSettingsBinding, item: SettingsModel, position: Int) {
        binding.settingNameText.text = item.name
        binding.descriptionText.text = item.description
        binding.container.setOnClickListener {
            onItemClick?.invoke(item.name)
        }
        binding.settingImage.setImageResource(item.image)
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<SettingsModel>() {
        override fun areItemsTheSame(oldItem: SettingsModel, newItem: SettingsModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SettingsModel, newItem: SettingsModel): Boolean {
            return oldItem == newItem
        }
    }
}