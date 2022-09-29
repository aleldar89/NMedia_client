package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.error.*
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.entity.toEntity
import java.io.IOException


class PostRepositoryImpl(private val postDao: PostDao): PostRepository {

    override val data = postDao.getAll()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val posts = response.body() ?: throw RuntimeException("body is null")

            postDao.insert(posts.map(PostEntity.Companion::fromDto))

            // меняет статус на "показывать"
            postDao.showAll()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun showAll() {
        try {
            postDao.showAll()
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getNewerCount(latestPostId: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostsApi.retrofitService.getNewer(latestPostId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun getById(id: Long): Post {
        try {
            return postDao.getById(id).toDto()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(post: Post) {
        try {
            postDao.likeById(post.id)

            val response = PostsApi.retrofitService.likeById(post.id)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun dislikeById(post: Post) {
        try {
            postDao.likeById(post.id)

            val response = PostsApi.retrofitService.dislikeById(post.id)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val postEntity = PostEntity.fromDto(post)
            postDao.save(postEntity)

            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            else {
                val _post = response.body()
                if (_post != null) {
                    postDao.updatePostId(_post.id)
                }
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)

            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw Exception(response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun localSave(post: Post) {
        try {
            val postEntity = PostEntity.fromDto(post)
            postDao.saveOld(postEntity)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun localRemoveById(id: Long) {
        try {
            postDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun selectLast(): Post {
        try {
            return postDao.selectLast().toDto()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}