package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import java.net.ConnectException

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
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    val data: LiveData<FeedModel> = repository.data.map(::FeedModel)

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    private val _error = SingleLiveEvent<Exception>()
    val error: LiveData<Exception>
        get() = _error

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getAll()
                _state.value = FeedModelState(error = false)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
                _error.value = e
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                _state.value = FeedModelState(error = false)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
                _error.value = e
            }
        }
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                repository.likeById(post)
                _state.value = FeedModelState(error = false)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
                _error.value = e
            }
        }
    }

    fun dislikeById(post: Post) {
        viewModelScope.launch {
            try {
                repository.dislikeById(post)
                _state.value = FeedModelState(error = false)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
                _error.value = e
            }
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    repository.save(it)
                    _state.value = FeedModelState(error = false)
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                    _error.value = e
                }
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
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

    fun swipeRefresh() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)
            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun parseException (e: Exception) = when (e) {
        is ConnectException -> "Internet error"
        is IOException -> "Server error"
        else -> "Unknown error"
    }

}

// остатки от предыдущего ДЗ

//    fun save() {
//        edited.value?.let {
//            viewModelScope.launch {
//                repository.save(it)
//                // todo try catch
//                _postCreated.postValue(Unit)
//            }
//        }
//        edited.value = empty
//    }

//    fun like(post: Post) {
//        viewModelScope.launch {
//            try {
//                repository.likeById(post)
//                _state.value = FeedModelState()
//            } catch (e: Exception) {
//                _state.value = FeedModelState(error = true)
//                _exceptionMessage.value = e.message
//            }
//        }
//    }

//    fun save() {
//        val old = _data.value?.posts.orEmpty()
//        edited.value?.let {
//            repository.saveAsync(it, object : PostRepository.Callback<Post> {
//                override fun onSuccess(result: Post) {
//                    val posts = listOf(result) + old
//                    _data.value = FeedModel(posts = posts)
//                }
//
//                override fun onError(e: Exception) {
//                    _data.value = _data.value?.copy(posts = old)
//                    _exceptionMessage.value = e.message
//                }
//            })
//            _postCreated.postValue(Unit)
//
//            edited.value = empty
//        }
//    }
//
//fun like(post: Post) {
//    val old = _data.value?.posts.orEmpty()
//    repository.likeAsync(post, object : PostRepository.Callback<Post> {
//        override fun onSuccess(result: Post) {
//            val posts = old.map {
//                if (it.id == result.id) result else it
//            }
//            _data.value = FeedModel(posts = posts)
//        }
//
//        override fun onError(e: Exception) {
//            _data.value = _data.value?.copy(posts = old)
//            _exceptionMessage.value = e.message
//        }
//    })
//}
//
//fun unlike(post: Post) {
//    val old = _data.value?.posts.orEmpty()
//    repository.unlikeAsync(post, object : PostRepository.Callback<Post> {
//        override fun onSuccess(result: Post) {
//            val posts = old.map {
//                if (it.id == result.id) result else it
//            }
//            _data.value = FeedModel(posts = posts)
//        }
//
//        override fun onError(e: Exception) {
//            _data.value = _data.value?.copy(posts = old)
//            _exceptionMessage.value = e.message
//        }
//    })
//}
//
//fun delete(id: Long) {
//    val old = _data.value?.posts.orEmpty()
//    val posts = _data.value?.posts.orEmpty()
//        .filter { it.id != id }
//    repository.deleteAsync(id, object : PostRepository.Callback<Unit> {
//        override fun onSuccess(result: Unit) {
//            _data.value = _data.value?.copy(posts = posts, empty = posts.isEmpty())
//        }
//
//        override fun onError(e: Exception) {
//            _data.value = _data.value?.copy(posts = old)
//            _exceptionMessage.value = e.message
//        }
//    })
//}