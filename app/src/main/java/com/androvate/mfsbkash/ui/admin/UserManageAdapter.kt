package com.androvate.mfsbkash.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.data.model.User
import com.androvate.mfsbkash.databinding.ItemUserManageBinding
import com.androvate.mfsbkash.utils.formatCurrency

class UserManageAdapter(
    private val onToggleStatus: (User, Boolean) -> Unit,
    private val onAdjustBalance: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<UserManageAdapter.ViewHolder>() {

    private var users = listOf<User>()

    inner class ViewHolder(val binding: ItemUserManageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserManageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val ctx = holder.itemView.context

        holder.binding.apply {
            tvName.text = user.name
            tvPhone.text = user.phone
            tvRole.text = user.role.replaceFirstChar { it.uppercase() }
            tvBalance.text = user.balance.formatCurrency()
            tvStatus.text = if (user.isActive) "Active" else "Inactive"
            tvStatus.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (user.isActive) R.color.colorSuccess else R.color.colorError
                )
            )

            // Prevent listener firing during bind
            switchActive.setOnCheckedChangeListener(null)
            switchActive.isChecked = user.isActive
            switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggleStatus(user, isChecked)
            }

            btnAdjustBalance.setOnClickListener {
                onAdjustBalance(user)
            }

            btnDelete.setOnClickListener {
                onDelete(user)
            }
        }
    }

    override fun getItemCount() = users.size

    fun updateList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }
}