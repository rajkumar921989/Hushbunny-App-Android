package com.hushbunny.app.ui.kids

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemKidsListBinding
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.KidsResponseModel
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseListAdapter
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL

class KidsListAdapter(
    private val resourceProvider: ResourceProvider,
    private val onViewProfileClick: ((KidsResponseModel) -> Unit)? = null,
    private val onEditProfileClick: ((KidsResponseModel) -> Unit)? = null
) :
    BaseListAdapter<KidsResponseModel, ItemKidsListBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemKidsListBinding {
        return ItemKidsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bind(binding: ItemKidsListBinding, item: KidsResponseModel, position: Int) {
        binding.nameText.text = item.name
        binding.nickNameText.text = item.nickName
        if (item.birthCountryISO2.orEmpty().isNotEmpty() && item.birtCity.orEmpty().isNotEmpty())
            binding.kidsDetailText.text = "${item.birthCountryISO2},${item.birtCity}"
        else if (item.birthCountryISO2.orEmpty().isNotEmpty())
            binding.kidsDetailText.text = item.birthCountryISO2
        else if (item.birthCountryISO2.orEmpty().isNotEmpty() && item.birtCity.orEmpty().isNotEmpty())
            binding.kidsDetailText.text = item.birtCity
        else binding.kidsDetailText.visibility = View.GONE
        if (item.image.isNullOrEmpty())
            binding.addKidImage.setImageDrawable(ContextCompat.getDrawable(binding.addKidImage.context, R.drawable.ic_no_kid_icon))
        else {
            val widthAndHeight = resourceProvider.getDimension(R.dimen.view_70).toInt()
            binding.addKidImage.loadImageFromURL("h_${widthAndHeight},w_${widthAndHeight}/${item.image.replace(APIConstants.IMAGE_BASE_URL, "")}")
        }
        binding.viewProfileButton.setOnClickListener {
            onViewProfileClick?.invoke(item)
        }
        binding.editProfileButton.setOnClickListener {
            onEditProfileClick?.invoke(item)
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<KidsResponseModel>() {
        override fun areItemsTheSame(oldItem: KidsResponseModel, newItem: KidsResponseModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: KidsResponseModel, newItem: KidsResponseModel): Boolean {
            return oldItem == newItem
        }
    }
}