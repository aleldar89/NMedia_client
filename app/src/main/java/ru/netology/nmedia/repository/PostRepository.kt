package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
//    val data: Flow<List<Post>>
    val data: Flow<PagingData<Post>>
    suspend fun getAll()
    fun getNewerCount(latestPostId: Long): Flow<Int>
    suspend fun showAll()
    suspend fun getById(id: Long): Post
    suspend fun removeById(id: Long)
    suspend fun likeById(post: Post)
    suspend fun dislikeById(post: Post)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, file: File)

    suspend fun localSave(post: Post)
    suspend fun localRemoveById(id: Long)
    suspend fun selectLast(): Post
}
