package com.androvate.mfsbkash.ui.admin


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.utils.SessionManager
import com.androvate.mfsbkash.ui.common.TransactionViewModel
import com.androvate.mfsbkash.databinding.FragmentAdminDepositBinding
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.utils.showSuccessDialog
import com.androvate.mfsbkash.utils.showToast


class AdminDepositFragment : Fragment() {
    private var _binding: FragmentAdminDepositBinding? = null
    private val binding get() = _binding!!
    private val txViewModel: TransactionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDepositBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnDeposit.setOnClickListener {
            val phone = binding.etUserPhone.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()
            val note = binding.etNote.text.toString().trim()

            if (phone.isEmpty() || phone.length < 11) {
                binding.tilPhone.error = "Enter valid phone"; return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                binding.tilAmount.error = "Enter amount"; return@setOnClickListener
            }
            binding.tilPhone.error = null
            binding.tilAmount.error = null

            val amount = amountStr.toDoubleOrNull() ?: return@setOnClickListener
            val admin = SessionManager.getUser(requireContext()) ?: return@setOnClickListener
            txViewModel.deposit(admin, phone, amount)
        }

        txViewModel.transactionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnDeposit.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnDeposit.isEnabled = true
                    val tx = result.data!!
                    requireContext().showSuccessDialog(
                        title = "Deposit Successful!",
                        message = "৳${tx.amount} deposited to ${tx.receiverPhone}\nTxn ID: ${tx.transactionId}",
                        onDismiss = { findNavController().navigateUp() }
                    )
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnDeposit.isEnabled = true
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