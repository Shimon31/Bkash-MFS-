package com.androvate.mfsbkash.View.admin


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.Adapter.TransactionAdapter
import com.androvate.mfsbkash.Viewmodel.TransactionViewModel
import com.androvate.mfsbkash.databinding.FragmentTransactionHistoryBinding


class AdminTransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!
    private val txViewModel: TransactionViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "All Transactions"
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        adapter = TransactionAdapter(emptyList())
        binding.rvTransactions.adapter = adapter

        txViewModel.allTransactions.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = result.data ?: emptyList()
                    binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvTransactions.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    adapter.updateList(list)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = result.message
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