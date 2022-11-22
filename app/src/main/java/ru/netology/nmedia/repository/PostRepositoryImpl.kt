package ru.netology.nmedia.repository

import androidx.paging.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.*
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.extensions.timeDescription
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
        )
    ).flow.map {
        it.map(PostEntity::toDto)
            .insertSeparators { previous, _ ->
                if (previous?.id?.rem(5) == 0L) {
                    Ad(kotlin.random.Random.nextLong(), "figma.jpg")
                } else {
                    null
                }
            }
            .insertSeparators { previous, _ ->
                if (previous is Post) {
                    Timing(
                        kotlin.random.Random.nextLong(),
                        previous.published.timeDescription())
                } else {
                    null
                }
            }
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val posts = response.body() ?: throw RuntimeException("body is null")

            postDao.insert(posts.map(PostEntity.Companion::fromDto))
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

            val response = apiService.likeById(post.id)
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

            val response = apiService.dislikeById(post.id)
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

            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            } else {
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

    override suspend fun saveWithAttachment(post: Post, file: File) {
        try {
            val media = upload(file)

            val response = apiService.save(
                post.copy(
                    attachment = Attachment(
                        url = media.id,
                        type = AttachmentType.IMAGE
                    )
                )
            )

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            } else {
                val _post = response.body()
                if (_post != null) {
                    postDao.updatePostId(_post.id)
                }
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)

            val response = apiService.removeById(id)
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

    private suspend fun upload(file: File): Media {
        val media = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody()
        )

        val response = apiService.upload(media)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }
}