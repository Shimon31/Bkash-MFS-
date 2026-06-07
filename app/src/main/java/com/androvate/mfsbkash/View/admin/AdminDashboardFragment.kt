package com.androvate.mfsbkash.View.admin

import com.androvate.mfsbkash.Viewmodel.AdminViewModel
import com.androvate.mfsbkash.Viewmodel.AuthViewModel
import com.androvate.mfsbkash.Viewmodel.TransactionViewModel
import com.androvate.mfsbkash.databinding.FragmentAdminDashboardBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.SessionManager
import com.androvate.mfsbkash.formatCurrency

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    private val txViewModel: TransactionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = SessionManager.getUser(requireContext())
        binding.tvAdminName.text = "Admin: ${user?.name ?: "Admin"}"

        setupObservers()
        setupClickListeners()
        loadData()
    }

    private fun setupClickListeners() {
        binding.cardManageUsers.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_manageUsersFragment)
        }
        binding.cardManageAgents.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_manageAgentsFragment)
        }
        binding.cardTransactions.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminTransactionsFragment)
        }
        binding.cardDeposit.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_adminDepositFragment)
        }
        binding.cardRegisterAgent.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_registerAgentFragment)
        }
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            SessionManager.clear(requireContext())
            findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
        }
        binding.ivRefresh.setOnClickListener { loadData() }
    }

    private fun setupObservers() {
        adminViewModel.stats.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val stats = result.data!!
                binding.tvTotalUsers.text = stats["totalUsers"].toString()
                binding.tvTotalAgents.text = stats["totalAgents"].toString()
                binding.tvTotalBalance.text = (stats["totalBalance"] as Double).formatCurrency()
            }
        }
        txViewModel.allTransactions.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                binding.tvTotalTransactions.text = result.data?.size.toString()
            }
        }
    }

    private fun loadData() {
        adminViewModel.fetchStats()
        txViewModel.fetchAllTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}