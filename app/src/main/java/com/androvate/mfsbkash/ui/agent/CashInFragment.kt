package com.androvate.mfsbkash.ui.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.utils.SessionManager
import com.androvate.mfsbkash.ui.common.TransactionViewModel
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.databinding.FragmentCashInBinding
import com.androvate.mfsbkash.utils.formatCurrency
import com.androvate.mfsbkash.utils.showSuccessDialog
import com.androvate.mfsbkash.utils.showToast

class CashInFragment : Fragment() {

    private var _binding: FragmentCashInBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCashInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val agent = SessionManager.getUser(requireContext())

        binding.tvAgentBalance.text =
            "Balance: ${agent?.balance ?: 0.0}"

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCashIn.setOnClickListener {

            val phone = binding.etUserPhone.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()

            if (phone.isEmpty()) {
                binding.tilUserPhone.error = "Enter phone"
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.tilAmount.error = "Enter valid amount"
                return@setOnClickListener
            }

            binding.tilUserPhone.error = null
            binding.tilAmount.error = null

            if (agent != null) {
                viewModel.cashIn(agent, phone, amount)
            }
        }

        observeCashIn()
    }

    private fun observeCashIn() {
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
                        title = "Success",
                        message = "৳${tx.amount} sent to ${tx.receiverPhone}"
                    ) {
                        findNavController().navigateUp()
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCashIn.isEnabled = true

                    requireContext().showToast(
                        result.message ?: "User not found"
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}