package com.androvate.mfsbkash


import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

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