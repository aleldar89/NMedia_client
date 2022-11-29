package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import okio.IOException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError


@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val response = when (loadType) {

                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id != null)
                        apiService.getAfter(id, state.config.pageSize)
                    else
                        apiService.getLatest(state.config.pageSize)
                }

                LoadType.PREPEND -> return MediatorResult.Success(true)

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            if (body.isEmpty()) {
                return MediatorResult.Success(true)
            }

            appDb.withTransaction {

                when (loadType) {
                    LoadType.REFRESH -> {
                        val id = postRemoteKeyDao.max()
                        if (id != null) {
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(PostRemoteKeyEntity.KeyType.AFTER, body.first().id),
                                    PostRemoteKeyEntity(PostRemoteKeyEntity.KeyType.BEFORE, body.last().id)
                                )
                            )
                        } else {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(PostRemoteKeyEntity.KeyType.AFTER, body.first().id)
                            )
                        }
                    }

                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(PostRemoteKeyEntity.KeyType.AFTER, body.first().id)
                        )
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(PostRemoteKeyEntity.KeyType.BEFORE, body.last().id
                            )
                        )
                    }
                }

                postDao.insert(body.map(PostEntity::fromDto))
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }

}