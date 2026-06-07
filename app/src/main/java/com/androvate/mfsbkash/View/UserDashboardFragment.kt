package com.androvate.mfsbkash.View


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.Adapter.TransactionAdapter
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.SessionManager
import com.androvate.mfsbkash.Viewmodel.AuthViewModel
import com.androvate.mfsbkash.Viewmodel.TransactionViewModel
import com.androvate.mfsbkash.databinding.FragmentUserDashboardBinding
import com.androvate.mfsbkash.formatCurrency
import com.androvate.mfsbkash.model.Resource

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDashboardFragment : Fragment() {
    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private val txViewModel: TransactionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        setupClickListeners()
        loadData()
    }

    private fun setupUI() {
        val user = SessionManager.getUser(requireContext())
        binding.tvUserName.text = "Hello, ${user?.name ?: "User"}!"
        binding.tvUserPhone.text = user?.phone ?: ""
        binding.tvBalance.text = user?.balance?.formatCurrency() ?: "৳0.00"
        binding.tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun setupClickListeners() {
        binding.cardSendMoney.setOnClickListener {
            findNavController().navigate(R.id.action_userDashboardFragment_to_sendMoneyFragment)
        }
        binding.cardCashOut.setOnClickListener {
            findNavController().navigate(R.id.action_userDashboardFragment_to_cashoutFragment)
        }
        binding.cardHistory.setOnClickListener {
            findNavController().navigate(R.id.action_userDashboardFragment_to_transactionHistoryFragment)
        }
        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.action_userDashboardFragment_to_profileFragment)
        }
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            SessionManager.clear(requireContext())
            findNavController().navigate(R.id.action_userDashboardFragment_to_loginFragment)
        }
        binding.ivRefresh.setOnClickListener { loadData() }
    }

    private fun setupObservers() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val user = result.data!!
                SessionManager.saveUser(requireContext(), user)
                binding.tvBalance.text = user.balance.formatCurrency()
                binding.tvUserName.text = "Hello, ${user.name}!"
            }
        }

        txViewModel.history.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val list = result.data ?: emptyList()
                val recent = list.take(3)
                if (recent.isEmpty()) {
                    binding.tvNoTransactions.visibility = View.VISIBLE
                    binding.rvRecentTransactions.visibility = View.GONE
                } else {
                    binding.tvNoTransactions.visibility = View.GONE
                    binding.rvRecentTransactions.visibility = View.VISIBLE
                    val adapter = TransactionAdapter(recent)
                    binding.rvRecentTransactions.adapter = adapter
                }
            }
        }
    }

    private fun loadData() {
        authViewModel.fetchCurrentUser()
        val user = SessionManager.getUser(requireContext())
        user?.uid?.let { txViewModel.fetchHistory(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}