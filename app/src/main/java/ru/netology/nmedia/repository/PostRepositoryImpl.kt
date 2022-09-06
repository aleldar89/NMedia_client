package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApiHolder
import ru.netology.nmedia.dto.Post


class PostRepositoryImpl: PostRepository {

    override fun getAll(): List<Post> {
        return PostApiHolder.api.getAll()
            .execute()
            .let {
                if (!it.isSuccessful) {
                    error(it.message())
                }
                it.body() ?: throw RuntimeException("Body is null")
            }
    }

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostApiHolder.api.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    response.body()?.let { callback.onSuccess(it) }
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }
            })
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostApiHolder.api.save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    response.body()?.let { callback.onSuccess(it) }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }
            })
    }

    override fun likeAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostApiHolder.api.like(post.id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    response.body()?.let { callback.onSuccess(it) }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }
            })
    }

    override fun unlikeAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostApiHolder.api.unlike(post.id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    response.body()?.let { callback.onSuccess(it) }
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }
            })
    }

    override fun deleteAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostApiHolder.api.delete(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    response.body()?.let { callback.onSuccess(it) }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(RuntimeException(t))
                }
            })
    }

    override fun delete(id: Long) {
        val result = PostApiHolder.api.delete(id)
            .execute()
        if (!result.isSuccessful) {
            error(result.message())
        }

        val postResult = result.body() ?: error("Body is null")
    }

    override fun save(post: Post): Post {
        val result = PostApiHolder.api.save(post)
            .execute()
        if (!result.isSuccessful) {
            error(result.message())
        }

        return result.body() ?: error("Body is null")
    }

}