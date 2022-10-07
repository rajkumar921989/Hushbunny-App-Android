package com.hushbunny.app.ui.notifications

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemNotificationBinding
import com.hushbunny.app.ui.enumclass.NotificationType
import com.hushbunny.app.ui.enumclass.ReactionPageName
import com.hushbunny.app.ui.model.NotificationModel
import com.hushbunny.app.uitls.BaseListAdapter
import com.hushbunny.app.uitls.DateFormatUtils.getTimeAgo
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL

class NotificationAdapter(
    private val onAcceptClick: ((NotificationModel) -> Unit)? = null,
    private val onItemClick: ((NotificationModel) -> Unit)? = null,
    private val onRejectClick: ((NotificationModel) -> Unit)? = null
) :
    BaseListAdapter<NotificationModel, ItemNotificationBinding>(ItemDiffCallback()) {
    override fun createBinding(parent: ViewGroup): ItemNotificationBinding {
        return ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bind(binding: ItemNotificationBinding, item: NotificationModel, position: Int) {
        binding.userNameText.text = Html.fromHtml(item.content.replace("%<", "<b>").replace(">%", "</b>"), Html.FROM_HTML_MODE_LEGACY)
        binding.timeText.text = item.dateTime.getTimeAgo()
        if (item.isRead) {
            binding.root.background = ContextCompat.getDrawable(binding.root.context, R.drawable.read_notification_background)
        } else {
            binding.root.background = ContextCompat.getDrawable(binding.root.context, R.drawable.un_read_notification_background)
        }
        binding.userImage.visibility = View.VISIBLE
        binding.logoImage.visibility = View.GONE
        if (item.isRequiredHushBunnyLogo) {
            binding.userImage.visibility = View.GONE
            binding.logoImage.visibility = View.VISIBLE
        } else if (item.image.isEmpty())
            binding.userImage.setImageDrawable(ContextCompat.getDrawable(binding.userImage.context, R.drawable.ic_no_kid_icon))
        else binding.userImage.loadImageFromURL(item.image)

        when(item.type) {
            NotificationType.OTHER_PARENT_INVITATION.name,
            NotificationType.REMINDER_OTHER_PARENT_INVITATION.name,
            NotificationType.FINAL_REMINDER_OTHER_PARENT_INVITATION.name -> {
                if(item.status.equals("PENDING", true)) {
                    binding.acceptRejectGroup.visibility = View.VISIBLE
                    binding.commentText.visibility = View.GONE
                } else {
                    binding.acceptRejectGroup.visibility = View.GONE
                    binding.commentText.visibility = View.VISIBLE
                    binding.commentText.text = binding.commentText.context.getString(R.string.invitation_append, item.status.lowercase())
                }
            }
            else -> {
                binding.acceptRejectGroup.visibility = View.GONE
                binding.commentText.visibility = View.GONE
            }
        }

        if (item.type.equals(NotificationType.IMPORTANT_MOMENT.name, true)) {
            binding.emojiImage.visibility = View.VISIBLE
            binding.emojiImage.setImageDrawable(ContextCompat.getDrawable(binding.emojiImage.context, R.drawable.ic_important_marked))
        } else if (item.type.equals(NotificationType.REACTION_ON_MOMENT.name, true) ||
            item.type.equals(NotificationType.SAD_REACTION_ON_MOMENT.name, true) ||
            item.type.equals(NotificationType.LAUGH_REACTION_ON_MOMENT.name, true) ||
            item.type.equals(NotificationType.LOVE_REACTION_ON_MOMENT.name, true)
        ) {
            binding.emojiImage.visibility = View.VISIBLE
            when (item.emojiType) {
                ReactionPageName.LAUGH.name -> {
                    binding.emojiImage.setImageDrawable(ContextCompat.getDrawable(binding.emojiImage.context, R.drawable.ic_laugh_reaction))
                }
                ReactionPageName.SAD.name -> {
                    binding.emojiImage.setImageDrawable(ContextCompat.getDrawable(binding.emojiImage.context, R.drawable.ic_sad_reaction))
                }
                else -> {
                    binding.emojiImage.setImageDrawable(ContextCompat.getDrawable(binding.emojiImage.context, R.drawable.ic_heart_reaction))
                }
            }
        } else binding.emojiImage.visibility = View.GONE
        binding.acceptButton.setOnClickListener {
            onAcceptClick?.invoke(item)
        }
        binding.rejectButton.setOnClickListener {
            onRejectClick?.invoke(item)
        }
        binding.root.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<NotificationModel>() {
        override fun areItemsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem == newItem
        }
    }
}