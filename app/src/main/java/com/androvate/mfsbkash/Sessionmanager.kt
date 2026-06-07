package com.androvate.mfsbkash


import android.content.Context
import android.content.SharedPreferences
import com.androvate.mfsbkash.model.User
import com.google.gson.Gson


object SessionManager {
    private const val PREF_NAME = "bkash_session"
    private const val KEY_USER = "current_user"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, user: User) {
        getPrefs(context).edit().putString(KEY_USER, Gson().toJson(user)).apply()
    }

    fun getUser(context: Context): User? {
        val json = getPrefs(context).getString(KEY_USER, null) ?: return null
        return try {
            Gson().fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}