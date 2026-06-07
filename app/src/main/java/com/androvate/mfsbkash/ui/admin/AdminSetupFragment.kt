package com.androvate.mfsbkash.ui.admin

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.androvate.mfsbkash.data.model.Resource
import com.androvate.mfsbkash.ui.auth.AuthViewModel
import com.androvate.mfsbkash.utils.showToast
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AdminSetupFragment : Fragment() {
    private val viewModel: AuthViewModel by viewModels()

    // Built programmatically to avoid needing a new layout file
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 80, 48, 48)
        }

        val dp = resources.displayMetrics.density

        fun addField(hint: String, isPassword: Boolean = false): Pair<TextInputLayout, TextInputEditText> {
            val til = TextInputLayout(ctx, null, R.attr.textInputOutlinedStyle).apply {
                this.hint = hint
                if (isPassword) endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (16 * dp).toInt() }
            }
            val et = TextInputEditText(til.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (isPassword) inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                else inputType = InputType.TYPE_CLASS_TEXT
            }
            til.addView(et)
            root.addView(til)
            return til to et
        }

        val (_, etName) = addField("Admin Name")
        val (_, etPhone) = addField("Phone (11 digits)")
        val (_, etPassword) = addField("Password (min 6 chars)", isPassword = true)
        val (_, etPin) = addField("PIN (5 digits)")

        etName.setText("Super Admin")
        etPhone.setText("01000000000")
        etPassword.setText("Admin@123")
        etPin.setText("00000")

        val progress = CircularProgressIndicator(ctx).apply {
            isIndeterminate = true
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = (12 * dp).toInt()
            }
        }
        root.addView(progress)

        val btn = MaterialButton(ctx).apply {
            text = "Create Admin Account"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (52 * dp).toInt()
            )
        }
        root.addView(btn)

        btn.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val pin = etPin.text.toString().trim()

            if (name.isEmpty() || phone.length < 11 || password.length < 6 || pin.length != 5) {
                ctx.showToast("Please fill all fields correctly")
                return@setOnClickListener
            }
            viewModel.seedAdmin(phone, password, pin, name)
        }

        viewModel.adminSeedResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    progress.visibility = View.VISIBLE
                    btn.isEnabled = false
                }
                is Resource.Success -> {
                    progress.visibility = View.GONE
                    btn.isEnabled = true
                    ctx.showToast("Admin created! You can now login.")
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    progress.visibility = View.GONE
                    btn.isEnabled = true
                    ctx.showToast(result.message ?: "Failed")
                }
            }
        }

        return root
    }
}