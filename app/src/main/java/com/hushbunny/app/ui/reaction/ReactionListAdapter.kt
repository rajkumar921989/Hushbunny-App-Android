package com.hushbunny.app.ui.reaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemReactionListBinding
import com.hushbunny.app.ui.enumclass.ReactionPageName
import com.hushbunny.app.ui.model.KidsResponseModel
import com.hushbunny.app.ui.model.ReactionModel
import com.hushbunny.app.uitls.BaseListAdapter
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadCircleImageFromURL

class ReactionListAdapter(private val onItemClick: ((ReactionModel) -> Unit)? = null) :
    BaseListAdapter<ReactionModel, ItemReactionListBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemReactionListBinding {
        return ItemReactionListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bind(binding: ItemReactionListBinding, item: ReactionModel, position: Int) {
        if (item.reactedBy?.image.isNullOrEmpty()) {
            binding.userImage.setImageDrawable(ContextCompat.getDrawable(binding.userImage.context, R.drawable.ic_no_kid_icon))
        } else {
            binding.userImage.loadCircleImageFromURL(item.reactedBy?.image.orEmpty())
        }
        binding.userNameText.text = item.reactedBy?.name
        when (item.emojiType) {
            ReactionPageName.HEART.name -> {
                binding.reactionImage.setImageDrawable(ContextCompat.getDrawable(binding.reactionImage.context, R.drawable.ic_heart_reaction))
            }
            ReactionPageName.LAUGH.name -> {
                binding.reactionImage.setImageDrawable(ContextCompat.getDrawable(binding.reactionImage.context, R.drawable.ic_laugh_reaction))
            }
            ReactionPageName.SAD.name -> {
                binding.reactionImage.setImageDrawable(ContextCompat.getDrawable(binding.reactionImage.context, R.drawable.ic_sad_reaction))
            }
        }
        binding.root.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<ReactionModel>() {
        override fun areItemsTheSame(oldItem: ReactionModel, newItem: ReactionModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ReactionModel, newItem: ReactionModel): Boolean {
            return oldItem == newItem
        }
    }
}