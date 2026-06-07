package com.androvate.mfsbkash.View



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.SessionManager
import com.androvate.mfsbkash.Viewmodel.AuthViewModel
import com.androvate.mfsbkash.databinding.FragmentLoginBinding
import com.androvate.mfsbkash.model.Resource
import com.androvate.mfsbkash.model.UserRole
import com.androvate.mfsbkash.showToast


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Auto-navigate if already logged in
        if (viewModel.isLoggedIn()) {
            viewModel.fetchCurrentUser()
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (phone.isEmpty()) {
                binding.tilPhone.error = "Enter phone number"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = "Enter password"
                return@setOnClickListener
            }
            binding.tilPhone.error = null
            binding.tilPassword.error = null
            viewModel.login(phone, password)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    val user = result.data!!
                    SessionManager.saveUser(requireContext(), user)
                    navigateByRole(user.role)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    requireContext().showToast(result.message ?: "Login failed")
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val user = result.data!!
                SessionManager.saveUser(requireContext(), user)
                navigateByRole(user.role)
            }
        }
    }

    private fun navigateByRole(role: String) {
        val action = when (role) {
            UserRole.ADMIN -> R.id.action_loginFragment_to_adminDashboardFragment
            UserRole.AGENT -> R.id.action_loginFragment_to_agentDashboardFragment
            else -> R.id.action_loginFragment_to_userDashboasrdFragment
        }
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}