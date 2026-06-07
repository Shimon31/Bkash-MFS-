package com.androvate.mfsbkash.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.databinding.FragmentAdminTransactionsBinding
import com.androvate.mfsbkash.ui.common.TransactionAdapter
import com.androvate.mfsbkash.ui.common.TransactionViewModel

class AdminTransactionsFragment : Fragment() {
    private var _binding: FragmentAdminTransactionsBinding? = null
    private val binding get() = _binding!!
    private val txViewModel: TransactionViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        adapter = TransactionAdapter(emptyList())
        binding.rvTransactions.adapter = adapter

        txViewModel.allTransactions.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvTransactions.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = result.data ?: emptyList()
                    if (list.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvTransactions.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvTransactions.visibility = View.VISIBLE
                        adapter.updateList(list)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = result.message ?: "Error loading transactions"
                }
            }
        }

        txViewModel.fetchAllTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}