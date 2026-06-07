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
            _transactionResult.value = txRepository.sendMoney(sender, receiverPhone, amount, pin)
        }
    }

    fun cashIn(agent: User, userPhone: String, amount: Double) {
        _transactionResult.value = Resource.Loading()
        viewModelScope.launch {
            _transactionResult.value = txRepository.cashIn(agent, userPhone, amount)
        }
    }

    fun cashOut(user: User, agentPhone: String, amount: Double, pin: String) {
        _transactionResult.value = Resource.Loading()
        viewModelScope.launch {
            _transactionResult.value = txRepository.cashOut(user, agentPhone, amount, pin)
        }
    }

    fun deposit(admin: User, userPhone: String, amount: Double) {
        _transactionResult.value = Resource.Loading()
        viewModelScope.launch {
            _transactionResult.value = txRepository.deposit(admin, userPhone, amount)
        }
    }

    fun fetchHistory(userId: String) {
        _history.value = Resource.Loading()
        viewModelScope.launch {
            _history.value = txRepository.getTransactionHistory(userId)
        }
    }

    fun fetchAllTransactions() {
        _allTransactions.value = Resource.Loading()
        viewModelScope.launch {
            _allTransactions.value = txRepository.getAllTransactions()
        }
    }
}