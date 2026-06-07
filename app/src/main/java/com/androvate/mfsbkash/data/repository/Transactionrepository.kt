package com.androvate.mfsbkash.data.repository


import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.data.model.Transaction
import com.androvate.mfsbkash.data.model.TransactionStatus
import com.androvate.mfsbkash.data.model.TransactionType
import com.androvate.mfsbkash.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val txCollection = db.collection("transactions")

    companion object {
        const val SEND_MONEY_FEE_RATE = 0.0185
        const val CASH_OUT_FEE_RATE = 0.018
        const val MIN_TRANSACTION = 10.0
        const val MAX_TRANSACTION = 25000.0
    }

    suspend fun getUserByPhone(phone: String): Resource<User> {
        return try {
            val snap = usersCollection.whereEqualTo("phone", phone).get().await()
            if (snap.isEmpty) Resource.Error("User not found")
            else {
                val user = snap.documents[0].toObject(User::class.java)
                if (user != null) Resource.Success(user) else Resource.Error("User data error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun sendMoney(sender: User, receiverPhone: String, amount: Double, pin: String): Resource<Transaction> {
        return try {
            if (sender.pin != pin) return Resource.Error("Incorrect PIN")
            if (amount < MIN_TRANSACTION) return Resource.Error("Minimum amount is ৳$MIN_TRANSACTION")
            if (amount > MAX_TRANSACTION) return Resource.Error("Maximum amount is ৳$MAX_TRANSACTION")

            val fee = if (amount >= 1000) amount * SEND_MONEY_FEE_RATE else 5.0
            val totalDeduction = amount + fee

            if (sender.balance < totalDeduction) return Resource.Error("Insufficient balance")

            val receiverResult = getUserByPhone(receiverPhone)
            if (receiverResult is Resource.Error) return Resource.Error(receiverResult.message ?: "Receiver not found")
            val receiver = receiverResult.data!!

            if (receiver.uid == sender.uid) return Resource.Error("Cannot send money to yourself")

            val txId = "TXN${UUID.randomUUID().toString().take(8).uppercase()}"

            db.runTransaction { tx ->
                val senderRef = usersCollection.document(sender.uid)
                val receiverRef = usersCollection.document(receiver.uid)

                tx.update(senderRef, "balance", sender.balance - totalDeduction)
                tx.update(receiverRef, "balance", receiver.balance + amount)

                val transaction = Transaction(
                    transactionId = txId,
                    type = TransactionType.SEND_MONEY,
                    amount = amount,
                    senderId = sender.uid,
                    senderPhone = sender.phone,
                    receiverId = receiver.uid,
                    receiverPhone = receiver.phone,
                    status = TransactionStatus.SUCCESS,
                    timestamp = System.currentTimeMillis(),
                    fee = fee,
                    balanceAfter = sender.balance - totalDeduction
                )
                tx.set(txCollection.document(txId), transaction)

                // Record receive transaction
                val receiveTxId = "TXN${UUID.randomUUID().toString().take(8).uppercase()}"
                val receiveTx = transaction.copy(
                    transactionId = receiveTxId,
                    type = TransactionType.RECEIVE_MONEY,
                    balanceAfter = receiver.balance + amount
                )
                tx.set(txCollection.document(receiveTxId), receiveTx)
            }.await()

            val txDoc = txCollection.document(txId).get().await()
            Resource.Success(txDoc.toObject(Transaction::class.java)!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Transaction failed")
        }
    }

    suspend fun cashIn(agent: User, userPhone: String, amount: Double): Resource<Transaction> {
        return try {
            if (amount < MIN_TRANSACTION) return Resource.Error("Minimum amount is ৳$MIN_TRANSACTION")
            if (agent.balance < amount) return Resource.Error("Agent has insufficient balance")

            val userResult = getUserByPhone(userPhone)
            if (userResult is Resource.Error) return Resource.Error(userResult.message ?: "User not found")
            val user = userResult.data!!

            val txId = "TXN${UUID.randomUUID().toString().take(8).uppercase()}"

            db.runTransaction { tx ->
                val agentRef = usersCollection.document(agent.uid)
                val userRef = usersCollection.document(user.uid)
                tx.update(agentRef, "balance", agent.balance - amount)
                tx.update(userRef, "balance", user.balance + amount)

                val transaction = Transaction(
                    transactionId = txId,
                    type = TransactionType.CASH_IN,
                    amount = amount,
                    senderId = agent.uid,
                    senderPhone = agent.phone,
                    receiverId = user.uid,
                    receiverPhone = user.phone,
                    agentId = agent.uid,
                    status = TransactionStatus.SUCCESS,
                    timestamp = System.currentTimeMillis(),
                    fee = 0.0,
                    balanceAfter = user.balance + amount
                )
                tx.set(txCollection.document(txId), transaction)
            }.await()

            val txDoc = txCollection.document(txId).get().await()
            Resource.Success(txDoc.toObject(Transaction::class.java)!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Cash In failed")
        }
    }

    suspend fun cashOut(user: User, agentPhone: String, amount: Double, pin: String): Resource<Transaction> {
        return try {
            if (user.pin != pin) return Resource.Error("Incorrect PIN")
            if (amount < MIN_TRANSACTION) return Resource.Error("Minimum amount is ৳$MIN_TRANSACTION")

            val fee = amount * CASH_OUT_FEE_RATE
            val totalDeduction = amount + fee
            if (user.balance < totalDeduction) return Resource.Error("Insufficient balance")

            val agentResult = getUserByPhone(agentPhone)
            if (agentResult is Resource.Error) return Resource.Error("Agent not found")
            val agent = agentResult.data!!
            if (agent.role != "agent") return Resource.Error("Invalid agent number")

            val txId = "TXN${UUID.randomUUID().toString().take(8).uppercase()}"

            db.runTransaction { tx ->
                val userRef = usersCollection.document(user.uid)
                val agentRef = usersCollection.document(agent.uid)
                tx.update(userRef, "balance", user.balance - totalDeduction)
                tx.update(agentRef, "balance", agent.balance + amount)

                val transaction = Transaction(
                    transactionId = txId,
                    type = TransactionType.CASH_OUT,
                    amount = amount,
                    senderId = user.uid,
                    senderPhone = user.phone,
                    receiverId = agent.uid,
                    receiverPhone = agent.phone,
                    agentId = agent.uid,
                    status = TransactionStatus.SUCCESS,
                    timestamp = System.currentTimeMillis(),
                    fee = fee,
                    balanceAfter = user.balance - totalDeduction
                )
                tx.set(txCollection.document(txId), transaction)
            }.await()

            val txDoc = txCollection.document(txId).get().await()
            Resource.Success(txDoc.toObject(Transaction::class.java)!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Cash Out failed")
        }
    }

    suspend fun deposit(admin: User, userPhone: String, amount: Double): Resource<Transaction> {
        return try {
            val userResult = getUserByPhone(userPhone)
            if (userResult is Resource.Error) return Resource.Error(userResult.message ?: "User not found")
            val user = userResult.data!!

            val txId = "TXN${UUID.randomUUID().toString().take(8).uppercase()}"

            db.runTransaction { tx ->
                val userRef = usersCollection.document(user.uid)
                tx.update(userRef, "balance", user.balance + amount)

                val transaction = Transaction(
                    transactionId = txId,
                    type = TransactionType.DEPOSIT,
                    amount = amount,
                    senderId = admin.uid,
                    senderPhone = "Admin",
                    receiverId = user.uid,
                    receiverPhone = user.phone,
                    status = TransactionStatus.SUCCESS,
                    timestamp = System.currentTimeMillis(),
                    fee = 0.0,
                    balanceAfter = user.balance + amount
                )
                tx.set(txCollection.document(txId), transaction)
            }.await()

            val txDoc = txCollection.document(txId).get().await()
            Resource.Success(txDoc.toObject(Transaction::class.java)!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Deposit failed")
        }
    }

    suspend fun getTransactionHistory(userId: String): Resource<List<Transaction>> {
        return try {
            val sentSnap = txCollection
                .whereEqualTo("senderId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get().await()
            val receivedSnap = txCollection
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get().await()

            val all = mutableListOf<Transaction>()
            sentSnap.documents.mapNotNullTo(all) { it.toObject(Transaction::class.java) }
            receivedSnap.documents.mapNotNullTo(all) { it.toObject(Transaction::class.java) }
            all.sortByDescending { it.timestamp }

            Resource.Success(all.distinctBy { it.transactionId }.take(50))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching history")
        }
    }

    suspend fun getAllTransactions(): Resource<List<Transaction>> {
        return try {
            val snap = txCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get().await()
            val list = snap.documents.mapNotNull { it.toObject(Transaction::class.java) }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching transactions")
        }
    }
}