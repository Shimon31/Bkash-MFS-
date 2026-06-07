package com.androvate.mfsbkash.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.data.model.User
import com.androvate.mfsbkash.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Resource<User>>()
    val loginResult: LiveData<Resource<User>> = _loginResult

    private val _registerResult = MutableLiveData<Resource<User>>()
    val registerResult: LiveData<Resource<User>> = _registerResult

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    private val _adminSeedResult = MutableLiveData<Resource<User>>()
    val adminSeedResult: LiveData<Resource<User>> = _adminSeedResult

    fun login(phone: String, password: String) {
        _loginResult.value = Resource.Loading()
        viewModelScope.launch {
            _loginResult.value = authRepository.login(phone, password)
        }
    }

    fun registerUser(name: String, phone: String, password: String, pin: String) {
        _registerResult.value = Resource.Loading()
        viewModelScope.launch {
            _registerResult.value = authRepository.registerUser(name, phone, password, pin, "user")
        }
    }

    fun registerAgent(name: String, phone: String, password: String, pin: String) {
        _registerResult.value = Resource.Loading()
        viewModelScope.launch {
            _registerResult.value = authRepository.registerUser(name, phone, password, pin, "agent")
        }
    }

    fun seedAdmin(phone: String, password: String, pin: String, name: String) {
        _adminSeedResult.value = Resource.Loading()
        viewModelScope.launch {
            _adminSeedResult.value = authRepository.seedAdminIfNeeded(phone, password, pin, name)
        }
    }

    fun fetchCurrentUser() {
        _currentUser.value = Resource.Loading()
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    fun logout() = authRepository.logout()
    fun isLoggedIn() = authRepository.isLoggedIn()
    fun getCurrentUid() = authRepository.getCurrentUid()
}