package com.hushbunny.app.ui.moment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.hushbunny.app.R
import com.hushbunny.app.databinding.ItemImageViewBinding
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.enumclass.MediaType
import com.hushbunny.app.ui.model.MomentMediaModel
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.ImageViewAndFileUtils.loadImageFromURL
import com.hushbunny.app.uitls.browse

class ImageSliderAdapter(
    private val isAddMoment: Boolean = false,
    private val resourceProvider: ResourceProvider,
    private val onDeleteClick: ((MomentMediaModel) -> Unit)? = null,
    private val onMediaClick: ((String, String, Boolean) -> Unit)? = null,
    private val imageList: List<MomentMediaModel>,
    private val context: Context
) : PagerAdapter() {
    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageSliderItemBinding = ItemImageViewBinding.inflate(LayoutInflater.from(context))
        imageList[position].run {
            val width = resourceProvider.getDimension(R.dimen.view_320).toInt()
            val height = resourceProvider.getDimension(R.dimen.view_270).toInt()
            if (this.type == MediaType.VIDEO.name) {
                imageSliderItemBinding.playImage.visibility = View.VISIBLE
                val imageUrl = if (this.isUploaded) this.thumbnail.orEmpty().replace(APIConstants.VIDEO_BASE_URL, "") else this.thumbnail.orEmpty()
                imageSliderItemBinding.momentImage.loadImageFromURL(imageUrl = imageUrl, isVideo = true, isLocal = !this.isUploaded)
            } else {
                imageSliderItemBinding.playImage.visibility = View.GONE
                val imageUrl = if (this.isUploaded) this.original.orEmpty().replace(APIConstants.IMAGE_BASE_URL, "") else this.original.orEmpty()
                imageSliderItemBinding.momentImage.loadImageFromURL(imageUrl = imageUrl, isLocal = !this.isUploaded)
            }
            if (isAddMoment)
                imageSliderItemBinding.deleteImage.visibility = View.VISIBLE
            else imageSliderItemBinding.deleteImage.visibility = View.GONE
            imageSliderItemBinding.deleteImage.setOnClickListener {
                onDeleteClick?.invoke(this)
            }
            imageSliderItemBinding.momentImage.setOnClickListener {
                val url = if (this.type == MediaType.VIDEO.name) {
                    if (this.isUploaded)
                        this.original.orEmpty().replace(APIConstants.VIDEO_BASE_URL, "")
                    else this.thumbnail.orEmpty()
                } else {
                    if (this.isUploaded)
                        this.original.orEmpty().replace(APIConstants.IMAGE_BASE_URL, "")
                    else this.original.orEmpty()
                }
                onMediaClick?.invoke(this.type.orEmpty(), url, !this.isUploaded)
            }
            imageSliderItemBinding.linkImage.run {
                visibility = if(text.isNullOrEmpty()) View.GONE else View.VISIBLE
                setOnClickListener {
                    this.context.browse(url = text.orEmpty())
                }
            }
        }
        container.addView(imageSliderItemBinding.root, 0)
        return imageSliderItemBinding.root
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as View)
    }
}
