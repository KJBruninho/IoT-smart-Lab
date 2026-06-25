package com.example.myapplication.data.api

import android.content.ContentValues.TAG
import android.util.Log
import com.example.myapplication.data.local.TokenStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val TAG = "IOT_ROOM_DEBUG"
    private const val BASE_URL = "https://iotroom.paradise-byzantine.ts.net/"

    fun provideAuthApi(tokenStore: TokenStore): AuthApi {
        return provideRetrofit(tokenStore).create(AuthApi::class.java)
    }

    fun provideDashboardApi(tokenStore: TokenStore): DashboardApi {
        return provideRetrofit(tokenStore).create(DashboardApi::class.java)
    }

    fun provideAlertasApi(tokenStore: TokenStore): AlertasApi {
        return provideRetrofit(tokenStore).create(AlertasApi::class.java)
    }

    fun provideControloApi(tokenStore: TokenStore): ControloApi {
        return provideRetrofit(tokenStore).create(ControloApi::class.java)
    }

    private fun provideRetrofit(tokenStore: TokenStore): Retrofit {
        val client = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(authInterceptor(tokenStore))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun authInterceptor(tokenStore: TokenStore): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val path = originalRequest.url.encodedPath
            val fullUrl = originalRequest.url.toString()

            val requestBuilder = originalRequest.newBuilder()

            val isLoginRequest = path == "/auth/login"

            Log.d(TAG, "REQUEST: ${originalRequest.method} $fullUrl")
            Log.d(TAG, "REQUEST: path=$path isLogin=$isLoginRequest")

            if (!isLoginRequest) {
                val token = runBlocking {
                    tokenStore.token.firstOrNull()
                }

                if (!token.isNullOrBlank()) {
                    Log.d(TAG, "REQUEST: token enviado")
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    requestBuilder.addHeader("Cookie", "access_token=$token")
                } else {
                    Log.d(TAG, "REQUEST: sem token para enviar")
                }
            } else {
                Log.d(TAG, "REQUEST: login limpo, sem Authorization/Cookie")
            }

            val response = chain.proceed(requestBuilder.build())

            Log.d(TAG, "RESPONSE: ${response.code} ${response.request.url}")
            Log.d(TAG, "RESPONSE: Location=${response.header("Location")}")
            Log.d(TAG, "RESPONSE: Set-Cookie count=${response.headers("Set-Cookie").size}")

            response
        }
    }
}