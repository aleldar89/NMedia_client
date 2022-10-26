package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class AuthentificationViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val apiService: ApiService
) : ViewModel() {

    private val _responseAuthState = MutableLiveData<AuthState?>(null)
    val responseAuthState: LiveData<AuthState?>
        get() = _responseAuthState

    private val _error = SingleLiveEvent<Exception>()
    val error: LiveData<Exception>
        get() = _error

    fun updateUser(login: String, pass: String) {
        viewModelScope.launch {
            try {
                _responseAuthState.value = apiService.updateUser(login, pass).body()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun saveToken(token: String, id: Long) {
        appAuth.saveAuth(token, id)
    }

}

