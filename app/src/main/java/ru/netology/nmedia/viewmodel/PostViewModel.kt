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
    authorAvatar = "",
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

    private val _exceptionMessage = SingleLiveEvent<String?>()
    val exceptionMessage: LiveData<String?>
        get() = _exceptionMessage

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.value = FeedModel(posts = result, empty = result.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
                _exceptionMessage.value = e.message
            }
        })
    }

    fun like(post: Post) {
        val old = _data.value?.posts.orEmpty()
        repository.likeAsync(post, object : PostRepository.Callback<Post> {
            override fun onSuccess(result: Post) {
                val posts = old.map {
                    if (it.id == result.id) result else it
                }
                _data.value = FeedModel(posts = posts)
            }

            override fun onError(e: Exception) {
                _data.value = _data.value?.copy(posts = old)
                _exceptionMessage.value = e.message
            }
        })
    }

    fun unlike(post: Post) {
        val old = _data.value?.posts.orEmpty()
        repository.unlikeAsync(post, object : PostRepository.Callback<Post> {
            override fun onSuccess(result: Post) {
                val posts = old.map {
                    if (it.id == result.id) result else it
                }
                _data.value = FeedModel(posts = posts)
            }

            override fun onError(e: Exception) {
                _data.value = _data.value?.copy(posts = old)
                _exceptionMessage.value = e.message
            }
        })
    }

    fun save() {
        val old = _data.value?.posts.orEmpty()
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(result: Post) {
                    val posts = listOf(result) + old
                    _data.value = FeedModel(posts = posts)
                }

                override fun onError(e: Exception) {
                    _data.value = _data.value?.copy(posts = old)
                    _exceptionMessage.value = e.message
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

    fun delete(id: Long) {
        val old = _data.value?.posts.orEmpty()
        val posts = _data.value?.posts.orEmpty()
            .filter { it.id != id }
        repository.deleteAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(result: Unit) {
                _data.value = _data.value?.copy(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = _data.value?.copy(posts = old)
                _exceptionMessage.value = e.message
            }
        })
    }

}