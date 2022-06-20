package com.hushbunny.app.ui.blockeduser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemBlockedUserBinding
import com.hushbunny.app.ui.model.BlockedUserResponseModel
import com.hushbunny.app.uitls.BaseListAdapter
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL

class BlockedUserAdapter(private val onItemClick: ((BlockedUserResponseModel) -> Unit)? = null) :
    BaseListAdapter<BlockedUserResponseModel, ItemBlockedUserBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemBlockedUserBinding {
        return ItemBlockedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bind(binding: ItemBlockedUserBinding, item: BlockedUserResponseModel, position: Int) {
        binding.userNameText.text = item.name
        if (item.image.isNullOrEmpty() || item.image == "default.png")
            binding.userImage.setImageDrawable(ContextCompat.getDrawable(binding.userImage.context, R.drawable.ic_no_kid_icon))
        else binding.userImage.loadImageFromURL(item.image)
        binding.unblockButton.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<BlockedUserResponseModel>() {
        override fun areItemsTheSame(oldItem: BlockedUserResponseModel, newItem: BlockedUserResponseModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: BlockedUserResponseModel, newItem: BlockedUserResponseModel): Boolean {
            return oldItem == newItem
        }
    }
}