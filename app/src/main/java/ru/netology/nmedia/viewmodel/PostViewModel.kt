package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.switchMap
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.*
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

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

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    val isAuthorized: Boolean
        get() = appAuth
            .data
            .value
            ?.token != null

    private val _authorization = MutableLiveData(isAuthorized)
    val authorization: LiveData<Boolean>
        get() = _authorization

    private val noPhoto = MediaModel()
    private val _media = MutableLiveData(noPhoto)
    val media: LiveData<MediaModel>
        get() = _media

    private val _error = SingleLiveEvent<Exception>()
    val error: LiveData<Exception>
        get() = _error

    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val data: Flow<PagingData<FeedItem>> = appAuth.data
        .flatMapLatest { auth ->
            repository.data
                .map { posts->
                    posts.map {
                        if (it is Post)
                            it.copy(ownedByMe = auth?.id == it.authorId)
                        else
                            it
                    }
                }
        }.flowOn(Dispatchers.Default)

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                repository.getAll()
            } catch (e: Exception) {
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

    fun removeById(id: Long) {
        viewModelScope.launch {
            val old = repository.getById(id)
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                try {
                    repository.save(old)
                } catch (e: Exception) {
                    repository.localSave(old)
                }
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
                } catch (e: Exception) {
                    val last = repository.selectLast()
                    try {
                        repository.removeById(last.id)
                    } catch (e: Exception) {
                        repository.localRemoveById(last.id)
                    }
                    _error.value = e
                }
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
        clearPhoto()
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            val old = repository.getById(post.id)
            try {
                repository.likeById(post)
            } catch (e: Exception) {
                try {
                    repository.save(old)
                } catch (e: Exception) {
                    repository.localSave(old)
                }
                _error.value = e
            }
        }
    }

    fun dislikeById(post: Post) {
        viewModelScope.launch {
            val old = repository.getById(post.id)
            try {
                repository.dislikeById(post)
            } catch (e: Exception) {
                try {
                    repository.save(old)
                } catch (e: Exception) {
                    repository.localSave(old)
                }
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

}