package com.androvate.mfsbkash.utils


import android.content.Context
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showSuccessDialog(title: String, message: String, onDismiss: () -> Unit = {}) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Done") { dialog, _ ->
            dialog.dismiss()
            onDismiss()
        }
        .setCancelable(false)
        .show()
}

fun Double.formatCurrency(): String {
    return "৳${String.format("%.2f", this)}"
}