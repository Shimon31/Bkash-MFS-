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
import com.androvate.mfsbkash.databinding.FragmentSendMoneyBinding
import com.androvate.mfsbkash.formatCurrency
import com.androvate.mfsbkash.model.Resource
import com.androvate.mfsbkash.showSuccessDialog
import com.androvate.mfsbkash.showToast


class SendMoneyFragment : Fragment() {
    private var _binding: FragmentSendMoneyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSendMoneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = SessionManager.getUser(requireContext())
        binding.tvBalance.text = "Balance: ${user?.balance?.formatCurrency()}"

        setupFeeCalculator()
        setupObservers()

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSend.setOnClickListener {
            val receiverPhone = binding.etReceiverPhone.text.toString().trim()
            val amountStr = binding.etAmount.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            if (receiverPhone.isEmpty() || receiverPhone.length < 11) {
                binding.tilReceiverPhone.error = "Enter valid phone number"; return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                binding.tilAmount.error = "Enter amount"; return@setOnClickListener
            }
            if (pin.isEmpty() || pin.length != 5) {
                binding.tilPin.error = "Enter 5-digit PIN"; return@setOnClickListener
            }

            binding.tilReceiverPhone.error = null
            binding.tilAmount.error = null
            binding.tilPin.error = null

            val amount = amountStr.toDoubleOrNull() ?: return@setOnClickListener
            if (user != null) {
                viewModel.sendMoney(user, receiverPhone, amount, pin)
            }
        }
    }

    private fun setupFeeCalculator() {
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amount = s.toString().toDoubleOrNull() ?: 0.0
                val fee = if (amount >= 1000) amount * 0.0185 else if (amount > 0) 5.0 else 0.0
                binding.tvFee.text = "Fee: ${fee.formatCurrency()}"
                binding.tvTotal.text = "Total: ${(amount + fee).formatCurrency()}"
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
                    binding.btnSend.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSend.isEnabled = true
                    val tx = result.data!!
                    requireContext().showSuccessDialog(
                        title = "Money Sent!",
                        message = "৳${tx.amount} sent to ${tx.receiverPhone}\nTxn ID: ${tx.transactionId}",
                        onDismiss = { findNavController().navigateUp() }
                    )
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSend.isEnabled = true
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