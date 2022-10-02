package ru.netology.nmedia.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nmedia.R

fun ImageView.loadImage(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .fitCenter()
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .transform(*transforms)
        .timeout(10_000)
        .into(this)

fun ImageView.loadAvatar(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    loadImage(url, CircleCrop(), *transforms)
