package com.hushbunny.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemAddKidViewBinding
import com.hushbunny.app.ui.model.KidsResponseModel
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseListAdapter
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL

class KidsAdapter(
    private val isAddMoment: Boolean = false,
    private val addKidsClick: ((String) -> Unit)? = null,
    private val addSpouseClick: ((String) -> Unit)? = null,
    private val kidsClick: ((KidsResponseModel) -> Unit)? = null
) :
    BaseListAdapter<KidsResponseModel, ItemAddKidViewBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemAddKidViewBinding {
        return ItemAddKidViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bind(binding: ItemAddKidViewBinding, item: KidsResponseModel, position: Int) {
        binding.addKidText.text = item.name
        if (item.isSelected) {
            binding.kidsContainer.background =
                ContextCompat.getDrawable(binding.addKidImage.context, R.drawable.drawable_white_background_with_pink_outline)
        } else {
            binding.kidsContainer.background = ContextCompat.getDrawable(binding.addKidImage.context, R.drawable.kids_background)
        }
        if (!isAddMoment && item.isSpouseAdded == false) {
            binding.addSpouseGroup.visibility = View.VISIBLE
        } else {
            binding.addSpouseGroup.visibility = View.GONE
        }
        binding.addKidImage.visibility = View.GONE
        binding.userImage.visibility = View.GONE
        binding.addKidImageContainer.background = ContextCompat.getDrawable(binding.addKidImage.context, R.drawable.drawable_login_background_button)
        if (item.type == APIConstants.KIDS_LIST) {
            if (item.image.isNullOrEmpty()) {
                binding.addKidImage.visibility = View.VISIBLE
                binding.addKidImage.setImageDrawable(ContextCompat.getDrawable(binding.addKidImage.context, R.drawable.ic_no_kid_icon))
            } else {
                binding.addKidImageContainer.background = ContextCompat.getDrawable(binding.addKidImage.context, R.drawable.image_background)
                binding.userImage.visibility = View.VISIBLE
                binding.userImage.loadImageFromURL(item.image)
            }
        } else {
            binding.addKidImage.setImageDrawable(ContextCompat.getDrawable(binding.addKidImage.context, R.drawable.ic_add_kid))
            binding.addKidImage.visibility = View.VISIBLE
        }
        binding.kidsContainer.setOnClickListener {
            if (item.type == APIConstants.ADD_KID) {
                addKidsClick?.invoke(APIConstants.ADD_KID)
            } else {
                if (isAddMoment) {
                    item.isSelected = !item.isSelected
                    notifyItemChanged(position)
                } else {
                    kidsClick?.invoke(item)
                }
            }
        }
        binding.addSpouseButton.setOnClickListener {
            addSpouseClick?.invoke(item._id.orEmpty())
        }
    }

    fun getKidsList(): ArrayList<String> {
        val selectedKidsList = arrayListOf<String>()
        currentList.filter { it.isSelected }.forEach {
            selectedKidsList.add(it._id.orEmpty())
        }
        return selectedKidsList
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