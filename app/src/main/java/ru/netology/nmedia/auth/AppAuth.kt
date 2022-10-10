package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppAuth private constructor(context: Context){
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _data = MutableStateFlow<AuthState?>(null)
    val data: StateFlow<AuthState?> = _data.asStateFlow()

    companion object {
        const val ID_KEY = "ID_KEY"
        const val TOKEN_KEY = "TOKEN_KEY"

        private var INSTANCE: AppAuth? = null

        fun initAuth(context: Context) {
            INSTANCE = AppAuth(context)
        }

        fun  getInstance(): AppAuth = checkNotNull(INSTANCE) {
            "You forgot call initAuth!"
        }
    }

    init {
        val id = prefs.getLong(ID_KEY, 0).takeIf {
            prefs.contains(ID_KEY)
        }
        val token = prefs.getString(TOKEN_KEY, null)

        if (token != null && id != null) {
            _data.value = AuthState(id,token)
        } else {
            prefs.edit { clear() }
        }
    }

    @Synchronized
    fun saveAuth(token: String, id: Long) {
        _data.value = AuthState(token = token, id = id)
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
    }

    @Synchronized
    fun clearAuth() {
        _data.value = null
        prefs.edit { clear() }
    }

}