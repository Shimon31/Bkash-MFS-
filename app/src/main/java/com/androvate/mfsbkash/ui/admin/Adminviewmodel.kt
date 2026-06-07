package com.androvate.mfsbkash.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.data.model.User
import com.androvate.mfsbkash.data.repository.UserRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _users = MutableLiveData<Resource<List<User>>>()
    val users: LiveData<Resource<List<User>>> = _users

    private val _agents = MutableLiveData<Resource<List<User>>>()
    val agents: LiveData<Resource<List<User>>> = _agents

    private val _actionResult = MutableLiveData<Resource<String>>()
    val actionResult: LiveData<Resource<String>> = _actionResult

    private val _stats = MutableLiveData<Resource<Map<String, Any>>>()
    val stats: LiveData<Resource<Map<String, Any>>> = _stats


    fun fetchAllUsers() {
        _users.value = Resource.Loading()
        viewModelScope.launch {
            _users.value = userRepository.getAllUsers()
        }
    }

    fun fetchAllAgents() {
        _agents.value = Resource.Loading()
        viewModelScope.launch {
            _agents.value = userRepository.getAllAgents()
        }
    }

    fun fetchStats() {
        _stats.value = Resource.Loading()
        viewModelScope.launch {
            _stats.value = userRepository.getTotalStats()
        }
    }

    fun toggleUserStatus(uid: String, isActive: Boolean) {
        _actionResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = userRepository.toggleUserStatus(uid, isActive)
            _actionResult.value = when (result) {
                is Resource.Success -> Resource.Success(
                    if (isActive) "Account activated" else "Account deactivated"
                )
                is Resource.Error -> Resource.Error(result.message ?: "Failed")
                else -> Resource.Loading()
            }
        }
    }

    fun adjustBalance(uid: String, currentBalance: Double, amount: Double, isAdd: Boolean) {
        _actionResult.value = Resource.Loading()
        viewModelScope.launch {
            val newBalance = if (isAdd) currentBalance + amount else currentBalance - amount
            if (newBalance < 0) {
                _actionResult.value = Resource.Error("Balance cannot go negative")
                return@launch
            }
            val result = userRepository.updateUserBalance(uid, newBalance)
            _actionResult.value = when (result) {
                is Resource.Success -> Resource.Success(
                    "Balance updated to ৳${String.format("%.2f", newBalance)}"
                )
                is Resource.Error -> Resource.Error(result.message ?: "Failed")
                else -> Resource.Loading()
            }
        }
    }

    fun deleteUser(uid: String) {
        _actionResult.value = Resource.Loading()
        viewModelScope.launch {
            val result = userRepository.deleteUser(uid)
            _actionResult.value = when (result) {
                is Resource.Success -> Resource.Success("User deleted")
                is Resource.Error -> Resource.Error(result.message ?: "Delete failed")
                else -> Resource.Loading()
            }
        }
    }
}