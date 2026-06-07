package com.androvate.mfsbkash.Viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androvate.mfsbkash.model.Resource
import com.androvate.mfsbkash.model.User
import com.androvate.mfsbkash.repository.UserRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _users = MutableLiveData<Resource<List<User>>>()
    val users: LiveData<Resource<List<User>>> = _users

    private val _agents = MutableLiveData<Resource<List<User>>>()
    val agents: LiveData<Resource<List<User>>> = _agents

    private val _allMembers = MutableLiveData<Resource<List<User>>>()
    val allMembers: LiveData<Resource<List<User>>> = _allMembers

    private val _actionResult = MutableLiveData<Resource<Boolean>>()
    val actionResult: LiveData<Resource<Boolean>> = _actionResult

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

    fun fetchAllMembers() {
        _allMembers.value = Resource.Loading()
        viewModelScope.launch {
            _allMembers.value = userRepository.getAllUsersAndAgents()
        }
    }

    fun toggleUserStatus(uid: String, isActive: Boolean) {
        _actionResult.value = Resource.Loading()
        viewModelScope.launch {
            _actionResult.value = userRepository.toggleUserStatus(uid, isActive)
        }
    }

    fun fetchStats() {
        _stats.value = Resource.Loading()
        viewModelScope.launch {
            _stats.value = userRepository.getTotalStats()
        }
    }
}