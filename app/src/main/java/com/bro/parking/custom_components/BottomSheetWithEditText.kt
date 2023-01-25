package com.bro.parking.custom_components

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.bro.parking.R
import com.bro.parking.databinding.AddCarBottomSheetLayoutBinding
import com.bro.parking.extensions.setIcon
import com.bro.parking.extensions.shake
import com.bro.parking.extensions.showKeyboard
import com.bro.parking.text_watchers.CarNumberTextWatcher
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class BottomSheetWithEditText(private val viewForSnackbar: View) : BottomSheetDialogFragment() {

    // Binding
    private lateinit var binding: AddCarBottomSheetLayoutBinding

    private var isLoading = false

    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
        auth = Firebase.auth
        databaseReference = Firebase.database.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Binding
        binding = AddCarBottomSheetLayoutBinding.inflate(inflater, container, false)

        // Edit text
        binding.carLicensePlateEt.addTextChangedListener(CarNumberTextWatcher())
        binding.carLicensePlateEt.doAfterTextChanged {
            if (it?.length == 9 && !isLoading){
                uploadNumberToFirebase(it.toString().uppercase())
            }
        }
        binding.carLicensePlateEt.requestFocus()
        binding.carLicensePlateEt.showKeyboard(requireContext())
        binding.carLicensePlateEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val carPlateN = binding.carLicensePlateEt.toString().uppercase()
                if (!isLoading){
                    if (carPlateN.count { it.isLetter() } == 4 && carPlateN.count { it.isDigit() } == 3 && carPlateN.count { it == '-' } == 2) {
                        uploadNumberToFirebase(carPlateN)
                    } else {
                        binding.textInputLayout.shake()
                    }
                }
                true
            } else false
        }

        return binding.root
    }

    private fun uploadNumberToFirebase(carPlateNumber: String) {
        startLoading(true)

        databaseReference.child("Users").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var isAdded = false

                snapshot.children.forEach {
                    if (!isAdded && it.hasChild("car")) {
                        if (it.child("car").value.toString() == carPlateNumber) {
                            isAdded = true
                        }
                    }
                }

                if (isAdded) {
                    startLoading(false)
                    this@BottomSheetWithEditText.dismiss()
                    val snackBar = Snackbar.make(
                        viewForSnackbar,
                        getString(R.string.car_is_already_added_hint),
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setIcon(R.drawable.baseline_error_outline_24)
                    snackBar.show()
                } else {
                    // else register number
                    val user = auth.currentUser
                    if (user != null){
                        val hashMap = HashMap<String,String>()
                        if (user.phoneNumber != null){
                            hashMap["phone"] = user.phoneNumber!!
                        }
                        hashMap["car"] = carPlateNumber
                        databaseReference.child("Users").child(user.uid).setValue(hashMap).addOnCompleteListener {
                            startLoading(false)
                            this@BottomSheetWithEditText.dismiss()
                            if (it.isSuccessful){
                                val snackBar = Snackbar.make(
                                    viewForSnackbar,
                                    getString(R.string.car_added_successfully),
                                    Snackbar.LENGTH_LONG
                                )
                                snackBar.setIcon(R.drawable.baseline_done_all_24)
                                snackBar.show()
                                val sharedPref = requireContext().getSharedPreferences("USER_PROFILE", Context.MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putBoolean("profileIsLoaded",false)
                                editor.apply()
                            }
                            else{
                                val snackBar = Snackbar.make(
                                    viewForSnackbar,
                                    getString(R.string.database_error),
                                    Snackbar.LENGTH_LONG
                                )
                                snackBar.setIcon(R.drawable.baseline_error_outline_24)
                                snackBar.show()
                            }
                        }

                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                startLoading(false)
                val snackBar = Snackbar.make(
                    viewForSnackbar,
                    getString(R.string.database_error),
                    Snackbar.LENGTH_LONG
                )
                snackBar.setIcon(R.drawable.baseline_error_outline_24)
                snackBar.show()
            }
        })
    }

    private fun startLoading(b: Boolean) {
        isLoading = b
        if (b) {
            binding.loadingIndicator.visibility = View.VISIBLE
        } else {
            binding.loadingIndicator.visibility = View.INVISIBLE
        }

    }

}