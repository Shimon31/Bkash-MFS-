package com.androvate.mfsbkash.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.androvate.mfsbkash.R
import com.androvate.mfsbkash.databinding.ItemTransactionBinding
import com.androvate.mfsbkash.formatCurrency
import com.androvate.mfsbkash.model.Transaction
import com.androvate.mfsbkash.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = transactions[position]
        val ctx = holder.itemView.context

        holder.binding.apply {
            tvTxType.text = tx.type.toString()
            tvTxId.text = tx.transactionId

            tvDate.text = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(Date(tx.timestamp))

            tvStatus.text = tx.status

            val isCredit = tx.type == TransactionType.RECEIVE_MONEY ||
                    tx.type == TransactionType.CASH_IN ||
                    tx.type == TransactionType.DEPOSIT

            val amountText = if (isCredit) {
                "+${tx.amount.formatCurrency()}"
            } else {
                "-${tx.amount.formatCurrency()}"
            }

            tvAmount.text = amountText
            tvAmount.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (isCredit) R.color.colorSuccess else R.color.colorError
                )
            )

            val iconRes = when (tx.type) {
                TransactionType.SEND_MONEY -> R.drawable.baseline_send_24
                TransactionType.RECEIVE_MONEY -> R.drawable.baseline_call_received_24
                TransactionType.CASH_IN -> R.drawable.ic_cash_in
                TransactionType.CASH_OUT -> R.drawable.cash_out
                TransactionType.DEPOSIT -> R.drawable.deposit
                else -> {}
            }

            ivTxIcon.setImageResource(iconRes as Int)

            tvCounterparty.text = if (isCredit) {
                "From: ${tx.senderPhone}"
            } else {
                "To: ${tx.receiverPhone}"
            }
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateList(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}