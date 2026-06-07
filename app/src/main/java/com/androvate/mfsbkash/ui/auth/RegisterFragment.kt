package com.androvate.mfsbkash.ui.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.databinding.FragmentRegisterBinding
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.utils.showToast
import kotlin.getValue


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPass = binding.etConfirmPassword.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            if (name.isEmpty()) {
                binding.tilName.error = "Enter name"; return@setOnClickListener
            }
            if (phone.isEmpty() || phone.length < 11) {
                binding.tilPhone.error = "Enter valid phone (11 digits)"; return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 6) {
                binding.tilPassword.error = "Password must be 6+ chars"; return@setOnClickListener
            }
            if (password != confirmPass) {
                binding.tilConfirmPassword.error =
                    "Passwords don't match"; return@setOnClickListener
            }
            if (pin.isEmpty() || pin.length != 5) {
                binding.tilPin.error = "PIN must be 5 digits"; return@setOnClickListener
            }

            clearErrors()
            viewModel.registerUser(name, phone, password, pin)
        }

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilPhone.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tilPin.error = null
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    requireContext().showToast("Registration successful! Please login.")
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    requireContext().showToast(result.message ?: "Registration failed")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}