package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiServiceHolder
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.util.SingleLiveEvent

class AuthentificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _responseAuthState = MutableLiveData<AuthState?>(null)
    val responseAuthState: LiveData<AuthState?>
        get() = _responseAuthState

    private val _error = SingleLiveEvent<Exception>()
    val error: LiveData<Exception>
        get() = _error

    fun updateUser(login: String, pass: String) {
        viewModelScope.launch {
            try {
                _responseAuthState.value = ApiServiceHolder.service.updateUser(login, pass).body()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun saveToken(token: String, id: Long) {
        AppAuth.getInstance().saveAuth(token, id)
    }

}

