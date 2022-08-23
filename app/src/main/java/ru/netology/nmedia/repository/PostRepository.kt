package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun getAllAsync(callback: Callback<List<Post>>)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun deleteAsync(id: Long, callback: Callback<Unit>)
    fun likeAsync(post: Post, callback: Callback<Post>)
    fun unlikeAsync(post: Post, callback: Callback<Post>)

    fun getAll(): List<Post>
    fun save(post: Post): Post
    fun delete(id: Long)

    interface Callback<T> {
        fun onSuccess(result: T) {}
        fun onError(e: Exception) {}
    }
}
