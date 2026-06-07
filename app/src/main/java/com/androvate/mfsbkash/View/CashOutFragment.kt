package com.androvate.mfsbkash.View




import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.SessionManager
import com.androvate.mfsbkash.Viewmodel.TransactionViewModel
import com.androvate.mfsbkash.databinding.FragmentCashoutBinding
import com.androvate.mfsbkash.formatCurrency
import com.androvate.mfsbkash.model.Resource
import com.androvate.mfsbkash.showSuccessDialog
import com.androvate.mfsbkash.showToast


class CashOutFragment : Fragment() {
    private var _binding: FragmentCashoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCashoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = SessionManager.getUser(requireContext())
        binding.tvBalance.text = "Balance: ${user?.balance?.formatCurrency()}"

        setupFeeCalculator()
        setupObservers()

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnCashOut.setOnClickListener {
            val agentPhone = binding.etAgentPhone.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            if (agentPhone.isEmpty() || agentPhone.length < 11) {
                binding.tilAgentPhone.error = "Enter valid agent phone"; return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                binding.tilAmount.error = "Enter amount"; return@setOnClickListener
            }
            if (pin.isEmpty() || pin.length != 5) {
                binding.tilPin.error = "Enter 5-digit PIN"; return@setOnClickListener
            }
            binding.tilAgentPhone.error = null
            binding.tilAmount.error = null
            binding.tilPin.error = null

            val amount = amountStr.toDoubleOrNull() ?: return@setOnClickListener
            if (user != null) {
                viewModel.cashOut(user, agentPhone, amount, pin)
            }
        }
    }

    private fun setupFeeCalculator() {
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amount = s.toString().toDoubleOrNull() ?: 0.0
                val fee = amount * 0.018
                binding.tvFee.text = "Fee: ${fee.formatCurrency()} (1.8%)"
                binding.tvTotal.text = "Total deduction: ${(amount + fee).formatCurrency()}"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupObservers() {
        viewModel.transactionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCashOut.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCashOut.isEnabled = true
                    val tx = result.data!!
                    requireContext().showSuccessDialog(
                        title = "Cash Out Successful!",
                        message = "৳${tx.amount} cashed out\nFee: ${tx.fee.formatCurrency()}\nTxn ID: ${tx.transactionId}",
                        onDismiss = { findNavController().navigateUp() }
                    )
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCashOut.isEnabled = true
                    requireContext().showToast(result.message ?: "Transaction failed")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}