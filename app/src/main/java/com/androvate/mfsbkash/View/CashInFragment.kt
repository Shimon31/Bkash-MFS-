package com.androvate.mfsbkash.View

import com.androvate.mfsbkash.SessionManager
import com.androvate.mfsbkash.Viewmodel.TransactionViewModel
import com.androvate.mfsbkash.databinding.FragmentCashInBinding
import com.androvate.mfsbkash.formatCurrency
import com.androvate.mfsbkash.showSuccessDialog
import com.androvate.mfsbkash.showToast

package com.bkash.mfs.ui.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController


class CashInFragment : Fragment() {
    private var _binding: FragmentCashInBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCashInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val agent = SessionManager.getUser(requireContext())
        binding.tvAgentBalance.text = "Your Balance: ${agent?.balance?.formatCurrency()}"

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnCashIn.setOnClickListener {
            val userPhone = binding.etUserPhone.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()

            if (userPhone.isEmpty() || userPhone.length < 11) {
                binding.tilUserPhone.error = "Enter valid phone number"; return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                binding.tilAmount.error = "Enter amount"; return@setOnClickListener
            }
            binding.tilUserPhone.error = null
            binding.tilAmount.error = null

            val amount = amountStr.toDoubleOrNull() ?: return@setOnClickListener
            if (agent != null) viewModel.cashIn(agent, userPhone, amount)
        }

        viewModel.transactionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCashIn.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCashIn.isEnabled = true
                    val tx = result.data!!
                    requireContext().showSuccessDialog(
                        title = "Cash In Successful!",
                        message = "৳${tx.amount} added to ${tx.receiverPhone}\nTxn ID: ${tx.transactionId}",
                        onDismiss = { findNavController().navigateUp() }
                    )
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCashIn.isEnabled = true
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