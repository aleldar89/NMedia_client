package ru.netology.nmedia.extensions

import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import java.time.LocalDate
import java.time.Period

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

@RequiresApi(Build.VERSION_CODES.O)
fun Post.createDate() = LocalDate
    .parse("1970-01-01")
    .plus(Period.of(0, 0, this.published.toInt() / 86400))
    .toString()

@RequiresApi(Build.VERSION_CODES.O)
fun Post.days(): Int {
    val postDate = LocalDate
        .parse(
            LocalDate.parse("1970-01-01")
                .plus(Period.of(0, 0, this.published.toInt() / 86400))
                .toString()
        )
    val currentDate = LocalDate.now()
    return Period.between(currentDate, postDate).days
}