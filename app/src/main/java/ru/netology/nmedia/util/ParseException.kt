package ru.netology.nmedia.util

import java.io.IOException
import java.net.ConnectException

fun parseException (e: Exception) = when (e) {
    is ConnectException -> "Internet error"
    is IOException -> "Server error"
    else -> "Unknown error"
}