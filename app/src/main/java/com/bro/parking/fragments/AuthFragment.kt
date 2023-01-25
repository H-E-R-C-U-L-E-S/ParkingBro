package com.bro.parking.fragments

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bro.parking.R
import com.bro.parking.databinding.FragmentAuthBinding
import com.bro.parking.extensions.hideKeyboard

import com.bro.parking.extensions.shake
import com.bro.parking.extensions.showKeyboard

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.bro.parking.helpers.PhoneCode2CountryCode
import java.util.*
import java.util.concurrent.TimeUnit


class AuthFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentAuthBinding

    // Firebase
    private lateinit var auth: FirebaseAuth

    // Set default country code
    private var countryCode = "GE"

    // ... //
    private var isLoading = false
    private var phoneNumber = ""
    private var verificationId = ""
    private lateinit var forceToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Binding
        binding = FragmentAuthBinding.inflate(inflater, container, false)

        // Firebase
        auth = Firebase.auth

        // Phone number editText
        binding.phoneNumberEt.addTextChangedListener(PhoneNumberFormattingTextWatcher("GE"))
        binding.phoneNumberEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                if (isLoading){
                    binding.phoneNumberEt.hideKeyboard(requireContext())
                }
                else{
                    binding.fabNext.performClick()
                }
                true
            } else false
        }
        binding.phoneNumberEt.showKeyboard(requireContext())

        // Set country name
        binding.countryNameTv.setText(Locale("", countryCode).displayCountry)

        // Country code editText
        binding.countryCodeEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

                // Always show +
                if (!s?.startsWith('+')!!) {
                    s.insert(0, "+")
                }

                // Get country code
                countryCode = PhoneCode2CountryCode().countryFor(s.toString())

                // Move to phone et
                if (s.length == 4 && countryCode.isNotEmpty()) {
                    binding.phoneNumberEt.requestFocus()
                }

                // Set country name
                binding.countryNameTv.setText(Locale("", countryCode).displayCountry)

            }
        })

        // Fab
        binding.fabNext.setOnClickListener {
            val currentPhoneNumber = "${binding.countryCodeEt.text} ${binding.phoneNumberEt.text}"
            // Check fields
            if (countryCode.isEmpty() && binding.phoneNumberEt.text.isEmpty()) {
                binding.fullPhoneNumberLayout.shake()
                binding.countryCodeEt.requestFocus()
                return@setOnClickListener
            } else if (binding.phoneNumberEt.text.isEmpty()) {
                binding.phoneNumberEt.shake()
                binding.phoneNumberEt.requestFocus()
                return@setOnClickListener
            } else if (countryCode.isEmpty()) {
                binding.countryCodeEt.shake()
                binding.countryCodeEt.requestFocus()
                return@setOnClickListener
            }

            binding.fullPhoneNumberLayout.hideKeyboard(requireContext())

            // Set auth language
            auth.setLanguageCode(countryCode.lowercase())

            // Show dialog
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(currentPhoneNumber)
            builder.setMessage(getString(R.string.is_this_correct_number_question))
            builder.setNeutralButton(getString(R.string.edit)) { dialog, _ ->
                dialog.dismiss()
                binding.phoneNumberEt.showKeyboard(requireContext())
            }
            builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (currentPhoneNumber.trim().length > 8) {
                    if (currentPhoneNumber == phoneNumber) {
                        val action =
                            AuthFragmentDirections.actionAuthFragmentToVerificationFragment(
                                phoneNumber, verificationId, forceToken
                            )
                        findNavController().navigate(action)
                    } else {
                        loading(true)
                        sendVerificationCode(currentPhoneNumber)
                    }
                } else {
                    builder.setTitle(getString(R.string.sorry))
                    builder.setMessage(getString(R.string.invalid_phone_number_hint))
                    builder.setNeutralButton(null, null)
                    builder.setPositiveButton(getString(R.string.ok)) { mDialog, _ -> mDialog.dismiss() }
                    builder.setOnDismissListener {
                        binding.phoneNumberEt.showKeyboard(requireContext())
                    }
                    builder.show()
                }
            }
            builder.setOnCancelListener {
                binding.phoneNumberEt.showKeyboard(requireContext())
            }
            val dialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isAllCaps = false
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).isAllCaps = false
        }
        return binding.root
    }

    private fun sendVerificationCode(mPhoneNumber: String) {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                // ... //
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                loading(false)

                val builder = MaterialAlertDialogBuilder(requireContext())
                builder.setCancelable(false)

                if (exception is FirebaseAuthInvalidCredentialsException && exception.errorCode == "ERROR_INVALID_PHONE_NUMBER") {
                    builder.setTitle(getString(R.string.sorry))
                    builder.setMessage(getString(R.string.invalid_phone_number_hint))
                    builder.setPositiveButton(getString(R.string.ok)) { mDialog, _ -> mDialog.dismiss() }
                    builder.setOnDismissListener {
                        binding.phoneNumberEt.showKeyboard(requireContext())
                    }
                    builder.show()
                } else {
                    builder.setTitle(getString(R.string.sorry))
                    builder.setMessage(exception.localizedMessage)
                    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                    builder.show()
                }
            }

            override fun onCodeSent(
                verificationID: String, token: PhoneAuthProvider.ForceResendingToken
            ) {
                loading(false)

                // Set variables and go to verification fragment
                verificationId = verificationID
                forceToken = token
                phoneNumber = mPhoneNumber

                val action = AuthFragmentDirections.actionAuthFragmentToVerificationFragment(
                    phoneNumber, verificationID, token
                )

                findNavController().navigate(action)
            }

        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mPhoneNumber.replace(" ", ""))       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun loading(loading: Boolean) {
        isLoading = loading
        if (isLoading) {
            binding.fabNext.isLoading(true)
            binding.countryCodeEt.isEnabled = false
            binding.phoneNumberEt.isEnabled = false
        } else {
            binding.fabNext.isLoading(false)
            binding.countryCodeEt.isEnabled = true
            binding.phoneNumberEt.isEnabled = true
        }
    }

}