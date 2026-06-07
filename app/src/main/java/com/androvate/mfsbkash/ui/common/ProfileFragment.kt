package com.androvate.mfsbkash.ui.common


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.utils.SessionManager
import com.androvate.mfsbkash.databinding.FragmentProfileBinding
import com.androvate.mfsbkash.utils.formatCurrency

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = SessionManager.getUser(requireContext())
        binding.apply {
            tvName.text = user?.name ?: "-"
            tvPhone.text = user?.phone ?: "-"
            tvRole.text = user?.role?.replaceFirstChar { it.uppercase() } ?: "-"
            tvBalance.text = user?.balance?.formatCurrency() ?: "৳0.00"
            tvStatus.text = if (user?.isActive == true) "Active" else "Inactive"
            tvMemberId.text = user?.uid?.take(10)?.uppercase() ?: "-"
            tvJoinDate.text = user?.createdAt?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
            } ?: "-"
            tvInitial.text = user?.name?.firstOrNull()?.toString() ?: "U"
        }

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}