package com.hushbunny.app.uitls

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * A generic ViewHolder that works with a [ViewBinding].
 * @param <T> The type of the ViewBinding.
</T> */
class BaseViewHolder<out T : ViewBinding> constructor(val binding: T) :
    RecyclerView.ViewHolder(binding.root)
