package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

//    suspend fun updateUser(login: String, pass: String): AuthState? {
//        return PostsApi.retrofitService.updateUser(login, pass).body()
//    }

    fun updateUser(login: String, pass: String): AuthState? {
        var responseAuthState: AuthState? = null

        viewModelScope.launch {
            responseAuthState = PostsApi.retrofitService.updateUser(login, pass).body()
        }

        return responseAuthState
    }

    fun saveToken(token: String, id: Long) {
        AppAuth.getInstance().saveAuth(token, id)
    }

}