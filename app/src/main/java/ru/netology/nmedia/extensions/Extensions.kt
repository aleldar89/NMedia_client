package ru.netology.nmedia.extensions

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
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
        .timeout(30_000)
        .into(this)

fun ImageView.loadAvatar(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    loadImage(url, CircleCrop(), *transforms)

fun View.createToast(@StringRes textId: Int) =
    Toast.makeText(
        context,
        context?.getString(textId),
        Toast.LENGTH_SHORT
    ).show()

fun String.timeDescription() =
    when (this.toLong() / 86400) {
        in 0..1 -> "Сегодня"
        in 1..2 -> "Вчера"
        else -> "На прошлой неделе"
    }