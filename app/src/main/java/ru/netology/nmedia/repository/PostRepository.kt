package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAll()
    suspend fun getById(id: Long): Post
    suspend fun removeById(id: Long)
    suspend fun likeById(post: Post)
    suspend fun dislikeById(post: Post)
    suspend fun save(post: Post)
}
