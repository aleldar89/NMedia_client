package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: Callback<List<Post>>)
    fun likeByIdAsync(post: Post, callback: Callback<Post>)
    fun saveAsync(post: Post, callback: Callback<List<Post>>)
    fun removeByIdAsync(id: Long, callback: Callback<Any>)

    interface Callback<T> {
        fun onSuccess(result: T) {}
        fun onError(e: Exception) {}
    }
}
