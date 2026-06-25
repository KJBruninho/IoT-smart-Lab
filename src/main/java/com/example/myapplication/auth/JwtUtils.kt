package com.example.myapplication.auth

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    fun isExpired(token: String?): Boolean {
        if (token.isNullOrBlank()) {
            return true
        }

        return try {
            val parts = token.split(".")

            if (parts.size < 2) {
                return true
            }

            val payload = parts[1]
            val decodedBytes = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )

            val json = JSONObject(String(decodedBytes))
            val expSeconds = json.optLong("exp", 0)

            if (expSeconds == 0L) {
                return true
            }

            val nowSeconds = System.currentTimeMillis() / 1000

            nowSeconds >= expSeconds
        } catch (e: Exception) {
            true
        }
    }
}