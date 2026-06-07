package com.androvate.mfsbkash.repository


import com.androvate.mfsbkash.model.Resource
import com.androvate.mfsbkash.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            val snap = usersCollection.whereEqualTo("role", "user").get().await()
            val list = snap.documents.mapNotNull { it.toObject(User::class.java) }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun getAllAgents(): Resource<List<User>> {
        return try {
            val snap = usersCollection.whereEqualTo("role", "agent").get().await()
            val list = snap.documents.mapNotNull { it.toObject(User::class.java) }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun getAllUsersAndAgents(): Resource<List<User>> {
        return try {
            val snap = usersCollection.get().await()
            val list = snap.documents
                .mapNotNull { it.toObject(User::class.java) }
                .filter { it.role != "admin" }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun toggleUserStatus(uid: String, isActive: Boolean): Resource<Boolean> {
        return try {
            usersCollection.document(uid).update("isActive", isActive).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun getUserById(uid: String): Resource<User> {
        return try {
            val doc = usersCollection.document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun updateUserBalance(uid: String, balance: Double): Resource<Boolean> {
        return try {
            usersCollection.document(uid).update("balance", balance).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun getTotalStats(): Resource<Map<String, Any>> {
        return try {
            val allSnap = usersCollection.get().await()
            val allUsers = allSnap.documents.mapNotNull { it.toObject(User::class.java) }
            val totalUsers = allUsers.count { it.role == "user" }
            val totalAgents = allUsers.count { it.role == "agent" }
            val totalBalance = allUsers.sumOf { it.balance }
            Resource.Success(mapOf(
                "totalUsers" to totalUsers,
                "totalAgents" to totalAgents,
                "totalBalance" to totalBalance
            ))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }
}