package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun likeById(post: Post) {
        val old = _data.value?.posts.orEmpty()
        repository.likeByIdAsync(post, object : PostRepository.Callback<Post> {
            override fun onSuccess(result: Post) {
                val posts = old + result.copy(
                    likedByMe = !result.likedByMe,
                    likes = if (result.likedByMe) result.likes - 1 else result.likes + 1
                )
                _data.postValue(FeedModel(posts = posts))
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }

    fun save() {
        val old = _data.value?.posts.orEmpty()
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<List<Post>> {
                override fun onSuccess(result: List<Post>) {
                    _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
                }

                override fun onError(e: Exception) {
                    _data.postValue(_data.value?.copy(posts = old))
                }
            })
            _postCreated.postValue(Unit)

            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        val posts = _data.value?.posts.orEmpty()
            .filter { it.id != id }
        repository.removeByIdAsync(id, object : PostRepository.Callback<Any> {
            override fun onSuccess(result: Any) {
                _data.postValue(_data.value?.copy(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }
}
