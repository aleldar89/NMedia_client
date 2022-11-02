package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val apiService: ApiService
) : ViewModel() {

    private val noPhoto = MediaModel()
    private val _media = MutableLiveData(noPhoto)
    val media: LiveData<MediaModel>
        get() = _media

    fun changePhoto(uri: Uri, file: File) {
        _media.value = MediaModel(uri, file)
    }

    private val _responseAuthState = MutableLiveData<AuthState?>(null)
    val responseAuthState: LiveData<AuthState?>
        get() = _responseAuthState

    private val _error = SingleLiveEvent<Exception>()
    val error: LiveData<Exception>
        get() = _error

    fun saveToken(token: String, id: Long) {
        appAuth.saveAuth(token, id)
    }

    fun registerUser(login: String, pass: String, name: String) {
        viewModelScope.launch {
            try {
                _responseAuthState.value = apiService.registerUser(login, pass, name).body()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun registerWithPhoto(login: String, pass: String, name: String, file: File) {
        viewModelScope.launch {
            try {
                _responseAuthState.value = apiService.registerWithPhoto(
                    login.toRequestBody("text/plain".toMediaType()),
                    pass.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
                ).body()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

}