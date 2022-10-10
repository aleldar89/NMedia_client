package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import java.io.IOException
import java.net.ConnectException

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0L,
    authorAvatar = "",
    likedByMe = false,
    ownedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    //локальная БД
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    val data: LiveData<FeedModel> = AppAuth.getInstance()
        .data
        .flatMapLatest { auth ->
            repository.data
                .map {
                    FeedModel(
                        posts = it.map { post ->
                            post.copy(ownedByMe = auth?.id == post.authorId)
                        },
                        empty = it.isEmpty(),
                    )
                }
        }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val noPhoto = MediaModel()

    private val _media = MutableLiveData(noPhoto)
    val media: LiveData<MediaModel>
        get() = _media

    private val emptyNewerCount = MutableLiveData<Int>()

    val newerCount: LiveData<Int> = data.switchMap {
        if (it.posts.isEmpty()) {
            return@switchMap emptyNewerCount
        }

        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

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
            _dataState.value = FeedModelState(loading = true)
            try {
                repository.getAll()
                _dataState.value = FeedModelState(error = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
                _error.value = e
            }
        }
    }

    fun clearPhoto() {
        _media.value = null
    }

    fun changePhoto(uri: Uri, file: File) {
        _media.value = MediaModel(uri, file)
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.showAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            val old = repository.getById(id)
            try {
                repository.removeById(id)
                _dataState.value = FeedModelState(error = false)
            } catch (e: Exception) {
                try {
                    repository.save(old)
                } catch (e: Exception) {
                    repository.localSave(old)
                }
                _dataState.value = FeedModelState(error = true)
                _error.value = e
            }
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    when (val mediaModel = _media.value) {
                        null -> repository.save(it)
                        else -> mediaModel.file?.let { file ->
                            repository.saveWithAttachment(it, file)
                        }
                    }
                    _dataState.value = FeedModelState(error = false)
                } catch (e: Exception) {
                    val last = repository.selectLast()
                    try {
                        repository.removeById(last.id)
                    } catch (e: Exception) {
                        repository.localRemoveById(last.id)
                    }
                    _dataState.value = FeedModelState(error = true)
                    _error.value = e
                }
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            val old = repository.getById(post.id)
            try {
                repository.likeById(post)
                _dataState.value = FeedModelState(error = false)
            } catch (e: Exception) {
                try {
                    repository.save(old)
                } catch (e: Exception) {
                    repository.localSave(old)
                }
                _dataState.value = FeedModelState(error = true)
                _error.value = e
            }
        }
    }

    fun dislikeById(post: Post) {
        viewModelScope.launch {
            val old = repository.getById(post.id)
            try {
                repository.dislikeById(post)
                _dataState.value = FeedModelState(error = false)
            } catch (e: Exception) {
                try {
                    repository.save(old)
                } catch (e: Exception) {
                    repository.localSave(old)
                }
                _dataState.value = FeedModelState(error = true)
                _error.value = e
            }
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

    fun swipeRefresh() {
        viewModelScope.launch {
            _dataState.value = FeedModelState(refreshing = true)
            try {
                repository.getAll()
                _dataState.value = FeedModelState()
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