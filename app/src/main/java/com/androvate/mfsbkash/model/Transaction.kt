package com.androvate.mfsbkash.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val transactionId: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val senderId: String = "",
    val senderPhone: String = "",
    val receiverId: String = "",
    val receiverPhone: String = "",
    val agentId: String = "",
    val status: String = TransactionStatus.SUCCESS,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val fee: Double = 0.0,
    val balanceAfter: Double = 0.0
) : Parcelable

object TransactionType {
    const val DEPOSIT = "Deposit"
    const val WITHDRAW = "Withdraw"
    const val SEND_MONEY = "Send Money"
    const val RECEIVE_MONEY = "Receive Money"
    const val CASH_IN = "Cash In"
    const val CASH_OUT = "Cash Out"
}

object TransactionStatus {
    const val SUCCESS = "Success"
    const val PENDING = "Pending"
    const val FAILED = "Failed"
}