package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity


class PostRepositoryImpl(
    private val postDao: PostDao,
): PostRepository {

    override val data: LiveData<List<Post>> = postDao.getAll().map {
        it.map(PostEntity::toDto)
    }

    override suspend fun getAll() {
        val response = PostsApi.retrofitService.getAll()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")

        postDao.insert(posts.map(PostEntity.Companion::fromDto))
    }

    override suspend fun removeById(id: Long) {
        postDao.removeById(id)

        val response = PostsApi.retrofitService.removeById(id)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

    override suspend fun likeById(post: Post) {
        postDao.likeById(post.id)

        val response = PostsApi.retrofitService.likeById(post.id)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

    override suspend fun dislikeById(post: Post) {
        postDao.likeById(post.id)

        val response = PostsApi.retrofitService.dislikeById(post.id)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

    override suspend fun save(post: Post) {
        val postEntity = PostEntity.fromDto(post)
        postDao.save(postEntity)

        val response = PostsApi.retrofitService.save(post)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

// остатки от предыдущего ДЗ

//    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
//        PostApiHolder.api.save(post)
//            .enqueue(object : Callback<Post> {
//                override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                    response.body()?.let { callback.onSuccess(it) }
//                }
//
//                override fun onFailure(call: Call<Post>, t: Throwable) {
//                    callback.onError(RuntimeException(t))
//                }
//            })
//    }
}

//    override suspend fun likeById(post: Post) {
//        postDao.likeById(post.id)
//
//        val likeState = data.value?.get(post.id.toInt())?.likedByMe
//
//        val response = if (likeState == false)
//            PostsApi.retrofitService.likeById(post.id)
//        else
//            PostsApi.retrofitService.dislikeById(post.id)
//
//        if (!response.isSuccessful) {
//            throw RuntimeException(response.message())
//        }
//    }

//    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
//        PostApiHolder.api.getAll()
//            .enqueue(object : Callback<List<Post>> {
//                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
//                    response.body()?.let { callback.onSuccess(it) }
//                }
//
//                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
//                    callback.onError(RuntimeException(t))
//                }
//            })
//    }
//
//    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
//        PostApiHolder.api.save(post)
//            .enqueue(object : Callback<Post> {
//                override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                    response.body()?.let { callback.onSuccess(it) }
//                }
//
//                override fun onFailure(call: Call<Post>, t: Throwable) {
//                    callback.onError(RuntimeException(t))
//                }
//            })
//    }
//
//    override fun likeAsync(post: Post, callback: PostRepository.Callback<Post>) {
//        PostApiHolder.api.like(post.id)
//            .enqueue(object : Callback<Post> {
//                override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                    response.body()?.let { callback.onSuccess(it) }
//                }
//
//                override fun onFailure(call: Call<Post>, t: Throwable) {
//                    callback.onError(RuntimeException(t))
//                }
//            })
//    }
//
//    override fun unlikeAsync(post: Post, callback: PostRepository.Callback<Post>) {
//        PostApiHolder.api.unlike(post.id)
//            .enqueue(object : Callback<Post> {
//                override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                    response.body()?.let { callback.onSuccess(it) }
//                }
//
//                override fun onFailure(call: Call<Post>, t: Throwable) {
//                    callback.onError(RuntimeException(t))
//                }
//            })
//    }
//
//    override fun deleteAsync(id: Long, callback: PostRepository.Callback<Unit>) {
//        PostApiHolder.api.delete(id)
//            .enqueue(object : Callback<Unit> {
//                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
//                    response.body()?.let { callback.onSuccess(it) }
//                }
//
//                override fun onFailure(call: Call<Unit>, t: Throwable) {
//                    callback.onError(RuntimeException(t))
//                }
//            })
//    }


//    override fun getAll(): List<Post> {
//        return PostApiHolder.api.getAll()
//            .execute()
//            .let {
//                if (!it.isSuccessful) {
//                    error(it.message())
//                }
//                it.body() ?: throw RuntimeException("Body is null")
//            }
//    }
//
//    override fun delete(id: Long) {
//        val result = PostApiHolder.api.delete(id)
//            .execute()
//        if (!result.isSuccessful) {
//            error(result.message())
//        }
//
//        val postResult = result.body() ?: error("Body is null")
//    }
//
//    override fun save(post: Post): Post {
//        val result = PostApiHolder.api.save(post)
//            .execute()
//        if (!result.isSuccessful) {
//            error(result.message())
//        }
//
//        return result.body() ?: error("Body is null")
//    }