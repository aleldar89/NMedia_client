package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel : ViewModel() {
    val data = AppAuth.getInstance()
        .data
        .asLiveData()

    val isAuthorized: Boolean
        get() = data.value?.token != null
}