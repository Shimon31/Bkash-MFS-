package com.androvate.mfsbkash.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.data.model.UserRole
import com.androvate.mfsbkash.databinding.FragmentLoginBinding
import com.androvate.mfsbkash.utils.SessionManager
import com.androvate.mfsbkash.utils.showToast

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            if (phone.isEmpty()) { binding.tilPhone.error = "Enter phone number"; return@setOnClickListener }
            if (password.isEmpty()) { binding.tilPassword.error = "Enter password"; return@setOnClickListener }
            binding.tilPhone.error = null
            binding.tilPassword.error = null
            viewModel.login(phone, password)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.tvAppTitle.setOnLongClickListener {
            showAdminSetupDialog()
            true
        }
    }

    private fun showAdminSetupDialog() {
        val ctx = requireContext()
        val dp = resources.displayMetrics.density

        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24 * dp).toInt(), (16 * dp).toInt(), (24 * dp).toInt(), 0)
        }

        fun makeField(hint: String, defaultVal: String = "", isPassword: Boolean = false) =
            EditText(ctx).apply {
                this.hint = hint
                setText(defaultVal)
                if (isPassword) inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (12 * dp).toInt() }
            }.also { layout.addView(it) }

        val etName = makeField("Admin Name", "Super Admin")
        val etPhone = makeField("Phone", "01000000000")
        val etPassword = makeField("Password", "Admin@123", true)
        val etPin = makeField("5-digit PIN", "00000")

        AlertDialog.Builder(ctx)
            .setTitle("First-Time Admin Setup")
            .setMessage("Creates the admin account once. Skip if already done.")
            .setView(layout)
            .setPositiveButton("Create Admin") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                val pin = etPin.text.toString().trim()
                if (name.isEmpty() || phone.length < 11 || pass.length < 6 || pin.length != 5) {
                    ctx.showToast("Fill all fields correctly")
                    return@setPositiveButton
                }
                viewModel.seedAdmin(phone, pass, pin, name)
            }
            .setNegativeButton("Cancel", null)
            .show()
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

        viewModel.adminSeedResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> requireContext().showToast("Creating admin account…")
                is Resource.Success -> requireContext().showToast(
                    "✓ Admin created! You can now login."
                )
                is Resource.Error -> requireContext().showToast(result.message ?: "Failed")
            }
        }
    }

    private fun navigateByRole(role: String) {
        val action = when (role) {
            UserRole.ADMIN -> R.id.action_loginFragment_to_adminDashboardFragment
            UserRole.AGENT -> R.id.action_loginFragment_to_agentDashboardFragment
            else           -> R.id.action_loginFragment_to_userDashboardFragment
        }
        findNavController().navigate(
            action, null,
            NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .setLaunchSingleTop(true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}