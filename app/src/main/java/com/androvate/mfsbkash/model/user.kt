package com.androvate.mfsbkash.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = UserRole.USER,
    val balance: Double = 0.0,
    val pin: String = "",
    val isActive: Boolean = true,
    val agentId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val profileImageUrl: String = ""
) : Parcelable

object UserRole {
    const val ADMIN = "admin"
    const val AGENT = "agent"
    const val USER = "user"
}
