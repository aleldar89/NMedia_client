package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.auth.AppAuth

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

//    suspend fun updateUser(login: String, pass: String): String {
//        return PostsApi.retrofitService.updateUser(login, pass).toString()
//    }

    var tokenString: String? = null

    fun updateUser(login: String, pass: String) {
        viewModelScope.launch {
            tokenString = PostsApi.retrofitService.updateUser(login, pass).toString()
        }
    }

    fun saveToken(token: String, id: Long) {
        AppAuth.getInstance().saveAuth(token, id)
    }

}