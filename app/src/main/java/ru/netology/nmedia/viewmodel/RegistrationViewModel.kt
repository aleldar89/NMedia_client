package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.auth.*

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val _responseAuthState = MutableLiveData<AuthState?>(null)
    val responseAuthState: LiveData<AuthState?>
        get() = _responseAuthState

    fun updateUser(login: String, pass: String) {
        viewModelScope.launch {
            _responseAuthState.value = PostsApi.retrofitService.updateUser(login, pass).body()
        }
    }

    fun saveToken(token: String, id: Long) {
        AppAuth.getInstance().saveAuth(token, id)
    }

}