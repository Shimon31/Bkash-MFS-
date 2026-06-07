package com.androvate.mfsbkash.ui.common


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.data.model.Transaction
import com.androvate.mfsbkash.data.model.User
import com.androvate.mfsbkash.data.repository.TransactionRepository

import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {

    private val txRepository = TransactionRepository()

    private val _transactionResult = MutableLiveData<Resource<Transaction>>()
    val transactionResult: LiveData<Resource<Transaction>> = _transactionResult

    private val _history = MutableLiveData<Resource<List<Transaction>>>()
    val history: LiveData<Resource<List<Transaction>>> = _history

    private val _allTransactions = MutableLiveData<Resource<List<Transaction>>>()
    val allTransactions: LiveData<Resource<List<Transaction>>> = _allTransactions

    fun sendMoney(sender: User, receiverPhone: String, amount: Double, pin: String) {

        _transactionResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = txRepository.sendMoney(sender, receiverPhone, amount, pin)
                _transactionResult.value = result
            } catch (e: Exception) {
                _transactionResult.value =
                    Resource.Error(e.message ?: "Send money failed")
            }
        }
    }

    fun cashIn(agent: User, userPhone: String, amount: Double) {

        _transactionResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = txRepository.cashIn(agent, userPhone, amount)
                _transactionResult.value = result
            } catch (e: Exception) {
                _transactionResult.value =
                    Resource.Error(e.message ?: "Cash In failed")
            }
        }
    }


    fun cashOut(user: User, agentPhone: String, amount: Double, pin: String) {

        _transactionResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = txRepository.cashOut(user, agentPhone, amount, pin)
                _transactionResult.value = result
            } catch (e: Exception) {
                _transactionResult.value =
                    Resource.Error(e.message ?: "Cash Out failed")
            }
        }
    }


    fun deposit(admin: User, userPhone: String, amount: Double) {

        _transactionResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = txRepository.deposit(admin, userPhone, amount)
                _transactionResult.value = result
            } catch (e: Exception) {
                _transactionResult.value =
                    Resource.Error(e.message ?: "Deposit failed")
            }
        }
    }


    fun fetchHistory(userId: String) {

        _history.value = Resource.Loading()

        viewModelScope.launch {
            try {
                _history.value = txRepository.getTransactionHistory(userId)
            } catch (e: Exception) {
                _history.value =
                    Resource.Error(e.message ?: "History failed")
            }
        }
    }

    fun fetchAllTransactions() {

        _allTransactions.value = Resource.Loading()

        viewModelScope.launch {
            try {
                _allTransactions.value = txRepository.getAllTransactions()
            } catch (e: Exception) {
                _allTransactions.value =
                    Resource.Error(e.message ?: "Failed")
            }
        }
    }
}