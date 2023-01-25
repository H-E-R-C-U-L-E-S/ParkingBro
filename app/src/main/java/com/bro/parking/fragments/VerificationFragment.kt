package com.bro.parking.fragments

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bro.parking.MainActivity
import com.bro.parking.R
import com.bro.parking.databinding.FragmentVerificationBinding
import com.bro.parking.extensions.shake
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class VerificationFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentVerificationBinding

    // Firebase
    private lateinit var auth: FirebaseAuth

    // Args
    private val args: VerificationFragmentArgs by navArgs()

    //...//
    private lateinit var mTimer: CountDownTimer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // Binding
        binding = FragmentVerificationBinding.inflate(inflater, container, false)

        // Firebase
        auth = Firebase.auth

        // set number and start timer
        binding.phoneNumberTv.text = args.phoneNumber

        // Start countdown
        startResendCodeTimer()

        //...//
        var b = false

        binding.verificationNumEt1.doOnTextChanged { _, _, _, count ->
            if (count == 1) {
                binding.verificationNumEt2.requestFocus()
                b = true
            }
        }

        binding.verificationNumEt2.doOnTextChanged { _, _, _, count ->
            if (count == 1) {
                binding.verificationNumEt3.requestFocus()
                b = true
            }
        }
        binding.verificationNumEt2.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (binding.verificationNumEt2.text.isEmpty() && b) {
                    binding.verificationNumEt1.text.clear()
                    binding.verificationNumEt1.requestFocus()
                }
                b = true
            }
            false
        }

        binding.verificationNumEt3.doOnTextChanged { _, _, _, count ->
            if (count == 1) {
                binding.verificationNumEt4.requestFocus()
                b = true
            }
        }
        binding.verificationNumEt3.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                if (binding.verificationNumEt3.text.isEmpty() && b) {
                    binding.verificationNumEt2.text.clear()
                    binding.verificationNumEt2.requestFocus()
                }
                b = true
            }
            false
        }

        binding.verificationNumEt4.doOnTextChanged { _, _, _, count ->
            if (count == 1) {
                binding.verificationNumEt5.requestFocus()
                b = true
            }
        }
        binding.verificationNumEt4.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                if (binding.verificationNumEt4.text.isEmpty() && b) {
                    binding.verificationNumEt3.text.clear()
                    binding.verificationNumEt3.requestFocus()
                }
                b = true
            }
            false
        }

        binding.verificationNumEt5.doOnTextChanged { _, _, _, count ->
            if (count == 1) {
                binding.verificationNumEt6.requestFocus()
                b = true
            }
        }
        binding.verificationNumEt5.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                if (binding.verificationNumEt5.text.isEmpty() && b) {
                    binding.verificationNumEt4.text.clear()
                    binding.verificationNumEt4.requestFocus()
                }
                b = true
            }
            false
        }

        binding.verificationNumEt6.doOnTextChanged { _, _, _, count ->
            if (count == 1) {
                b = false
                val code =
                    binding.verificationNumEt1.text.toString() + binding.verificationNumEt2.text.toString() + binding.verificationNumEt3.text.toString() + binding.verificationNumEt4.text.toString() + binding.verificationNumEt5.text.toString() + binding.verificationNumEt6.text.toString()
                val authCredential = PhoneAuthProvider.getCredential(args.verificationId, code)
                signInWithPhoneNumber(authCredential)
            }
        }
        binding.verificationNumEt6.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                if (binding.verificationNumEt6.text.isEmpty() && b) {
                    binding.verificationNumEt5.text.clear()
                    binding.verificationNumEt5.requestFocus()
                }
                b = true
            }
            false
        }

        binding.resendCodeButton.setOnClickListener {
            binding.resendCodeButton.isClickable = false
            binding.resendCodeButton.text = getString(R.string.sending_code)
            resendVerificationCode()
        }
        return binding.root
    }

    private fun resendVerificationCode() {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                // ... //
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                binding.resendCodeButton.isClickable = true
                val builder = MaterialAlertDialogBuilder(requireContext())
                builder.setTitle(getString(R.string.sorry))
                builder.setMessage(exception.localizedMessage?.toString())
                builder.setPositiveButton(getString(R.string.ok)) { mDialog, _ -> mDialog.dismiss() }
                builder.show()
            }

            override fun onCodeSent(verificationID: String, p1: PhoneAuthProvider.ForceResendingToken) {
                binding.resendCodeButton.text = getString(R.string.code_sent)
                binding.resendCodeButton.postDelayed({startResendCodeTimer()},3000)
            }
        }


        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(args.phoneNumber.replace(" ", ""))       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(args.forceResendingToken).build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun signInWithPhoneNumber(authCredential: PhoneAuthCredential) {
        auth.signInWithCredential(authCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                binding.otpLayout.shake()
                binding.verificationNumEt1.text.clear()
                binding.verificationNumEt2.text.clear()
                binding.verificationNumEt3.text.clear()
                binding.verificationNumEt4.text.clear()
                binding.verificationNumEt5.text.clear()
                binding.verificationNumEt6.text.clear()
                binding.verificationNumEt1.requestFocus()
            } else {
                val builder = MaterialAlertDialogBuilder(requireContext())
                builder.setTitle(getString(R.string.sorry))
                builder.setMessage(task.exception?.localizedMessage?.toString())
                builder.setPositiveButton(getString(R.string.ok)) { mDialog, _ -> mDialog.dismiss() }
                builder.show()
            }
        }
    }

    private fun startResendCodeTimer() {
        binding.resendCodeButton.isClickable = true
        binding.resendCodeButton.isEnabled = false

        mTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val seconds = (millisUntilFinished / 1000).toInt()
                val minutes = seconds / 60
                val txt = "${getString(R.string.you_can_request_a_new_code_in)} $minutes:$seconds"

                binding.resendCodeButton.text = txt

            }

            override fun onFinish() {
                binding.resendCodeButton.isEnabled = true
                binding.resendCodeButton.text = getString(R.string.tap_to_get_a_new_code)
            }
        }
        mTimer.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        mTimer.cancel()
    }

}