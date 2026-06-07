package com.androvate.mfsbkash.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.data.model.User
import com.androvate.mfsbkash.databinding.FragmentManageAgentsBinding
import com.androvate.mfsbkash.utils.showToast

class ManageAgentsFragment : Fragment() {
    private var _binding: FragmentManageAgentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: UserManageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageAgentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        adapter = UserManageAdapter(
            onToggleStatus = { user, isActive ->
                showToggleConfirmDialog(user, isActive)
            },
            onAdjustBalance = { user ->
                showAdjustBalanceDialog(user)
            },
            onDelete = { user ->
                showDeleteConfirmDialog(user)
            }
        )
        binding.rvUsers.adapter = adapter

        observeViewModel()
        viewModel.fetchAllAgents()
    }

    private fun observeViewModel() {
        viewModel.agents.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvUsers.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = result.data ?: emptyList()
                    if (list.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvUsers.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvUsers.visibility = View.VISIBLE
                        adapter.updateList(list)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = result.message ?: "Error loading agents"
                }
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    requireContext().showToast(result.data ?: "Done")
                    viewModel.fetchAllAgents()
                }
                is Resource.Error -> requireContext().showToast(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    private fun showToggleConfirmDialog(user: User, isActive: Boolean) {
        val action = if (isActive) "Activate" else "Deactivate"
        AlertDialog.Builder(requireContext())
            .setTitle("$action Agent")
            .setMessage("$action agent account for ${user.name} (${user.phone})?")
            .setPositiveButton(action) { _, _ ->
                viewModel.toggleUserStatus(user.uid, isActive)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.fetchAllAgents()
            }
            .setCancelable(false)
            .show()
    }

    private fun showAdjustBalanceDialog(user: User) {
        val ctx = requireContext()
        val dp = resources.displayMetrics.density

        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24 * dp).toInt(), (16 * dp).toInt(), (24 * dp).toInt(), 0)
        }

        val tvCurrent = TextView(ctx).apply {
            text = "Current float balance: ৳${String.format("%.2f", user.balance)}"
            textSize = 13f
            setTextColor(0xFF4B5563.toInt())
        }
        layout.addView(tvCurrent)

        val radioGroup = RadioGroup(ctx).apply {
            orientation = RadioGroup.HORIZONTAL
            setPadding(0, (12 * dp).toInt(), 0, (8 * dp).toInt())
        }
        val rbAdd = RadioButton(ctx).apply { text = "Add Float"; id = View.generateViewId(); isChecked = true }
        val rbDeduct = RadioButton(ctx).apply { text = "Deduct Float"; id = View.generateViewId() }
        radioGroup.addView(rbAdd)
        radioGroup.addView(rbDeduct)
        layout.addView(radioGroup)

        val etAmount = EditText(ctx).apply {
            hint = "Enter amount (৳)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
        }
        layout.addView(etAmount)

        AlertDialog.Builder(ctx)
            .setTitle("Adjust Float — ${user.name}")
            .setView(layout)
            .setPositiveButton("Apply") { _, _ ->
                val amtStr = etAmount.text.toString().trim()
                if (amtStr.isEmpty()) { ctx.showToast("Enter an amount"); return@setPositiveButton }
                val amount = amtStr.toDoubleOrNull()
                if (amount == null || amount <= 0) { ctx.showToast("Invalid amount"); return@setPositiveButton }
                viewModel.adjustBalance(user.uid, user.balance, amount, rbAdd.isChecked)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Agent")
            .setMessage(
                "Remove agent ${user.name} (${user.phone})?\n\n" +
                        "Float balance of ৳${String.format("%.2f", user.balance)} will be lost. " +
                        "This cannot be undone."
            )
            .setPositiveButton("Remove") { _, _ ->
                viewModel.deleteUser(user.uid)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}