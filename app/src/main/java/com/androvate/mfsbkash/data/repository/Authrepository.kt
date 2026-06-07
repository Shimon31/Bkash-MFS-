package com.androvate.mfsbkash.data.repository

import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun login(phone: String, password: String): Resource<User> {
        return try {
            val email = "${phone}@bkash.mfs"
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Login failed")

            val userDoc = usersCollection.document(uid).get().await()
            val user = userDoc.toObject(User::class.java)

            when {
                user == null -> Resource.Error("User data not found in database")
                !user.isActive -> {
                    auth.signOut()
                    Resource.Error("Account is deactivated. Contact admin.")
                }
                else -> Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun registerUser(
        name: String,
        phone: String,
        password: String,
        pin: String,
        role: String = "user",
        agentId: String = ""
    ): Resource<User> {
        return try {
            val email = "${phone}@bkash.mfs"
            val existing = usersCollection.whereEqualTo("phone", phone).get().await()
            if (!existing.isEmpty) return Resource.Error("Phone number already registered")

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Registration failed")

            val user = User(
                uid = uid,
                name = name,
                phone = phone,
                email = email,
                role = role,
                balance = 0.0,
                pin = pin,
                isActive = true,
                agentId = agentId,
                createdAt = System.currentTimeMillis()
            )
            usersCollection.document(uid).set(user).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun seedAdminIfNeeded(
        adminPhone: String,
        adminPassword: String,
        adminPin: String,
        adminName: String = "Super Admin"
    ): Resource<User> {
        return try {
            val existing = usersCollection.whereEqualTo("phone", adminPhone).get().await()
            if (!existing.isEmpty) {
                return Resource.Error("Admin account already exists. Login with existing credentials.")
            }
            registerUser(adminName, adminPhone, adminPassword, adminPin, "admin")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Admin creation failed")
        }
    }

    suspend fun getCurrentUser(): Resource<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("Not logged in")
            val doc = usersCollection.document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching user")
        }
    }

    fun logout() = auth.signOut()
    fun isLoggedIn() = auth.currentUser != null
    fun getCurrentUid() = auth.currentUser?.uid
}