package com.androvate.mfsbkash.View.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.Adapter.UserManageAdapter
import com.androvate.mfsbkash.Viewmodel.AdminViewModel
import com.androvate.mfsbkash.databinding.FragmentManageUsersBinding
import com.androvate.mfsbkash.model.Resource
import com.androvate.mfsbkash.showToast

class ManageUsersFragment : Fragment() {
    private var _binding: FragmentManageUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: UserManageAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "Manage Users"
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        adapter = UserManageAdapter { user, isActive ->
            viewModel.toggleUserStatus(user.uid, isActive)
        }
        binding.rvUsers.adapter = adapter

        viewModel.users.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = result.data ?: emptyList()
                    binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    adapter.updateList(list)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    requireContext().showToast(result.message ?: "Error")
                }
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                requireContext().showToast("Status updated")
                viewModel.fetchAllUsers()
            } else if (result is Resource.Error) {
                requireContext().showToast(result.message ?: "Failed")
            }
        }

        viewModel.fetchAllUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}