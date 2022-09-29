package com.hushbunny.app.ui.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemCommentListBinding
import com.hushbunny.app.ui.model.CommentModel
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.BaseListAdapter
import com.hushbunny.app.uitls.DateFormatUtils.getTimeAgo
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadCircleImageFromURL
import com.hushbunny.app.uitls.dialog.DialogUtils

class CommentListAdapter(
    private val onDeleteClick: ((Int, CommentModel) -> Unit)? = null,
    private val onReportClick: ((Int, CommentModel) -> Unit)? = null,
    private val onUserClick: ((CommentModel) -> Unit)? = null
) :
    BaseListAdapter<CommentModel, ItemCommentListBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemCommentListBinding {
        return ItemCommentListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bind(binding: ItemCommentListBinding, item: CommentModel, position: Int) {
        if (item.commentBy?.image.isNullOrEmpty()) {
            binding.userImage.setImageDrawable(ContextCompat.getDrawable(binding.userImage.context, R.drawable.ic_no_kid_icon))
        } else {
            binding.userImage.loadCircleImageFromURL(item.commentBy?.image.orEmpty())
        }
        binding.moreImage.setOnClickListener {
            val popUp = PopupMenu(binding.moreImage.context, it)
            popUp.menuInflater.inflate(R.menu.comment_menu, popUp.menu)
            popUp.show()
            popUp.menu.findItem(R.id.actionDelete).isVisible = (item.commentBy?._id.orEmpty() == AppConstants.getUserID() || item.momentId?.addedBy?._id.orEmpty() == AppConstants.getUserID())
            popUp.menu.findItem(R.id.actionReport).isVisible = item.commentBy?._id != AppConstants.getUserID()
            popUp.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.actionDelete -> {
                        DialogUtils.showDialogWithCallBack(
                            binding.moreImage.context,
                            message = binding.moreImage.context.getString(R.string.delete_comment),
                            title = binding.moreImage.context.getString(R.string.comments),
                            positiveButtonText = binding.moreImage.context.getString(R.string.yes),
                            negativeButtonText = binding.moreImage.context.getString(R.string.cancel),
                            positiveButtonCallback = {
                                onDeleteClick?.invoke(position, item)
                            }
                        )
                    }
                    R.id.actionReport -> {
                        onReportClick?.invoke(position, item)
                    }
                }
                true
            }
        }
        binding.userNameText.text = item.commentBy?.name
        binding.descriptionText.text = item.comment
        binding.moreDescriptionText.text = item.comment
        binding.dateTimeText.text = item.updatedAt?.getTimeAgo()
        if (item.comment.orEmpty().length > 190)
            binding.readMoreText.visibility = View.VISIBLE
        else binding.readMoreText.visibility = View.GONE
        binding.userImage.setOnClickListener {
            onUserClick?.invoke(item)
        }
        binding.userNameText.setOnClickListener {
            onUserClick?.invoke(item)
        }
        binding.readMoreText.setOnClickListener {
            if (binding.readMoreText.text.toString() == binding.readMoreText.context.getString(R.string.read_more)) {
                binding.moreDescriptionText.visibility = View.VISIBLE
                binding.descriptionText.visibility = View.GONE
                binding.readMoreText.text = binding.readMoreText.context.getString(R.string.view_less)
            } else {
                binding.moreDescriptionText.visibility = View.GONE
                binding.descriptionText.visibility = View.VISIBLE
                binding.readMoreText.text = binding.readMoreText.context.getString(R.string.read_more)
            }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<CommentModel>() {
        override fun areItemsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
            return oldItem == newItem
        }
    }
}