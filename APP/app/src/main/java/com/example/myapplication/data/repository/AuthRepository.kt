package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.AuthApi
import com.example.myapplication.data.local.TokenStore
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {
    companion object {
        private const val TAG = "IOT_ROOM_DEBUG"
    }

    val token: Flow<String?> = tokenStore.token

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            Log.d(TAG, "LOGIN: início")
            Log.d(TAG, "LOGIN: email=$email")

            tokenStore.clearToken()
            Log.d(TAG, "LOGIN: token antigo limpo")

            val response = authApi.login(
                email = email,
                password = password
            )

            val rawResponse = response.raw()
            val httpCode = response.code()
            val location = rawResponse.header("Location")
            val cookies = rawResponse.headers("Set-Cookie")

            Log.d(TAG, "LOGIN: HTTP=$httpCode")
            Log.d(TAG, "LOGIN: Location=$location")
            Log.d(TAG, "LOGIN: cookies count=${cookies.size}")

            cookies.forEachIndexed { index, cookie ->
                Log.d(TAG, "LOGIN: cookie[$index]=${mascararCookie(cookie)}")
            }

            val accessToken = extrairAccessToken(cookies)

            if (accessToken.isNullOrBlank()) {
                val message = "Login sem access_token. HTTP $httpCode. Location: $location. Cookies: ${cookies.map { mascararCookie(it) }}"
                Log.e(TAG, "LOGIN: falhou -> $message")
                return Result.failure(Exception(message))
            }

            Log.d(TAG, "LOGIN: access_token recebido=${mascararToken(accessToken)}")

            tokenStore.saveToken(accessToken)

            Log.d(TAG, "LOGIN: token guardado com sucesso")

            Result.success(Unit)
        } catch (e: HttpException) {
            val message = "HTTP ${e.code()} - ${e.message()}"
            Log.e(TAG, "LOGIN HTTP ERROR: $message", e)
            Result.failure(Exception(message))
        } catch (e: IOException) {
            val message = "Erro de ligação: ${e.message}"
            Log.e(TAG, "LOGIN IO ERROR: $message", e)
            Result.failure(Exception(message))
        } catch (e: Exception) {
            val message = "Erro inesperado: ${e.message}"
            Log.e(TAG, "LOGIN ERROR: $message", e)
            Result.failure(Exception(message))
        }
    }

    suspend fun logout() {
        Log.d(TAG, "LOGOUT: limpar token")
        tokenStore.clearToken()
    }

    private fun extrairAccessToken(cookies: List<String>): String? {
        return cookies
            .asSequence()
            .flatMap { it.split(";").asSequence() }
            .map { it.trim() }
            .firstOrNull { it.startsWith("access_token=") }
            ?.substringAfter("access_token=")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun mascararToken(token: String): String {
        if (token.length <= 20) {
            return "***"
        }

        return token.take(12) + "..." + token.takeLast(8)
    }

    private fun mascararCookie(cookie: String): String {
        return cookie
            .replace(
                Regex("access_token=([^;]+)"),
                "access_token=***"
            )
            .replace(
                Regex("refresh_token=([^;]+)"),
                "refresh_token=***"
            )
            .replace(
                Regex("JSESSIONID=([^;]+)"),
                "JSESSIONID=***"
            )
    }
}