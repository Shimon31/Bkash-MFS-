package com.androvate.mfsbkash.View.agent


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.Viewmodel.AuthViewModel
import com.androvate.mfsbkash.databinding.FragmentRegisterAgentBinding
import com.androvate.mfsbkash.model.Resource
import com.androvate.mfsbkash.showToast

class RegisterAgentFragment : Fragment() {
    private var _binding: FragmentRegisterAgentBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterAgentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnRegisterAgent.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            if (name.isEmpty()) { binding.tilName.error = "Enter name"; return@setOnClickListener }
            if (phone.isEmpty() || phone.length < 11) { binding.tilPhone.error = "Enter valid 11-digit phone"; return@setOnClickListener }
            if (password.isEmpty() || password.length < 6) { binding.tilPassword.error = "Password 6+ chars"; return@setOnClickListener }
            if (pin.isEmpty() || pin.length != 5) { binding.tilPin.error = "PIN must be 5 digits"; return@setOnClickListener }

            binding.tilName.error = null
            binding.tilPhone.error = null
            binding.tilPassword.error = null
            binding.tilPin.error = null

            authViewModel.registerAgent(name, phone, password, pin)
        }

        authViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegisterAgent.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegisterAgent.isEnabled = true
                    requireContext().showToast("Agent registered successfully!")
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegisterAgent.isEnabled = true
                    requireContext().showToast(result.message ?: "Failed")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}