package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val idKey = "ID_KEY"
    private val tokenKey = "TOKEN_KEY"

    private val _data = MutableStateFlow<AuthState?>(null)
    val data: StateFlow<AuthState?> = _data.asStateFlow()

    init {
        val id = prefs.getLong(idKey, 0).takeIf {
            prefs.contains(idKey)
        }
        val token = prefs.getString(tokenKey, null)

        if (token != null && id != null) {
            _data.value = AuthState(id,token)
        } else {
            prefs.edit { clear() }
        }
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): ApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).apply {
            launch {
                try {
                    val pushToken = (token ?: FirebaseMessaging.getInstance().token.await())
                        .let(::PushToken)

                    val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                    entryPoint.getApiService().sendPushToken(pushToken)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Synchronized
    fun saveAuth(token: String, id: Long) {
        _data.value = AuthState(token = token, id = id)
        prefs.edit {
            putLong(idKey, id)
            putString(tokenKey, token)
        }
        sendPushToken()
    }

    @Synchronized
    fun clearAuth() {
        _data.value = null
        prefs.edit { clear() }
        sendPushToken()
    }

}