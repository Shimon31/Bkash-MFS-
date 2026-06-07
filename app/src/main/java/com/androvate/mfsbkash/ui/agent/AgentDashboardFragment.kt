package com.androvate.mfsbkash.ui.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.ui.common.TransactionAdapter
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.utils.SessionManager
import com.androvate.mfsbkash.ui.common.TransactionViewModel
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.databinding.FragmentAgentDashboardBinding
import com.androvate.mfsbkash.utils.formatCurrency
import com.androvate.mfsbkash.ui.auth.AuthViewModel

class AgentDashboardFragment : Fragment() {
    private var _binding: FragmentAgentDashboardBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private val txViewModel: TransactionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAgentDashboardBinding.inflate(inflater, container, false)
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
        binding.tvAgentName.text = "Agent: ${user?.name ?: ""}"
        binding.tvAgentPhone.text = user?.phone ?: ""
        binding.tvBalance.text = user?.balance?.formatCurrency() ?: "৳0.00"
    }

    private fun setupClickListeners() {
        binding.cardCashIn.setOnClickListener {
            findNavController().navigate(R.id.action_agentDashboardFragment_to_cashInFragment)
        }
        binding.cardHistory.setOnClickListener {
            findNavController().navigate(R.id.action_agentDashboardFragment_to_transactionHistoryFragment)
        }
        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.action_agentDashboardFragment_to_profileFragment)
        }
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            SessionManager.clear(requireContext())
            findNavController().navigate(R.id.action_agentDashboardFragment_to_loginFragment)
        }
        binding.ivRefresh.setOnClickListener { loadData() }
    }

    private fun setupObservers() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val user = result.data!!
                SessionManager.saveUser(requireContext(), user)
                binding.tvBalance.text = user.balance.formatCurrency()
            }
        }
        txViewModel.history.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val list = result.data ?: emptyList()
                val adapter = TransactionAdapter(list.take(3))
                binding.rvRecentTransactions.adapter = adapter
                binding.tvNoTransactions.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
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