package com.hushbunny.app.ui.moment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemMomentKidsListBinding
import com.hushbunny.app.ui.model.MomentKidsModel
import com.hushbunny.app.uitls.BaseListAdapter
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadCircleImageFromURL

class MomentKidsListAdapter(private val onItemClick: ((MomentKidsModel) -> Unit)? = null) :
    BaseListAdapter<MomentKidsModel, ItemMomentKidsListBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemMomentKidsListBinding {
        return ItemMomentKidsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bind(binding: ItemMomentKidsListBinding, item: MomentKidsModel, position: Int) {
        binding.nameText.text = item.name
        if (item.image.isNullOrEmpty())
            binding.kidImage.setImageDrawable(ContextCompat.getDrawable(binding.kidImage.context, R.drawable.ic_no_kid_icon))
        else binding.kidImage.loadCircleImageFromURL(item.image.orEmpty())
        binding.root.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<MomentKidsModel>() {
        override fun areItemsTheSame(oldItem: MomentKidsModel, newItem: MomentKidsModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MomentKidsModel, newItem: MomentKidsModel): Boolean {
            return oldItem == newItem
        }
    }
}