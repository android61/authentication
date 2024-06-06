package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.error.ApiException
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.File
import java.io.IOException

class AppAuth private constructor(context: Context) {
    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"

        private var INSTANCE: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(INSTANCE) {
            "init() must be called before getInstance()"
        }

        fun init(context: Context) {
            INSTANCE = AppAuth(context)
        }
    }

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState?>

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        _state = if (token == null || !prefs.contains(ID_KEY)) {
            prefs.edit { clear() }
            MutableStateFlow(null)
        } else {
            MutableStateFlow(AuthState(id, token))
        }
    }

    val state = _state.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        _state.value = AuthState(id, token)
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit { clear() }
        _state.value = null
    }

    suspend fun update(login: String, password: String) {
        try {
            val response = PostsApi.retrofitService.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            println(e)
            throw UnknownException
        }
    }

    suspend fun register(login: String, password: String, name: String) {
        try {
            val response = PostsApi.retrofitService.registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val newAuth = response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(newAuth.id, newAuth.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    suspend fun registerWithPhoto(login: String, password: String, name: String, file: File) {
        try {
            val fileData = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
            val response = PostsApi.retrofitService.registerWithPhoto(
                login.toRequestBody(),
                password.toRequestBody(),
                name.toRequestBody(),
                fileData
            )

            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw UnknownException
        }
    }
}