package com.bro.parking.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.bro.parking.R
import com.bro.parking.custom_components.NumberFoundBottomSheetFragment
import com.bro.parking.custom_components.PermissionsDialogFragment
import com.bro.parking.databinding.FragmentPhoneNumberBinding
import com.bro.parking.extensions.hideKeyboard
import com.bro.parking.extensions.setIcon
import com.bro.parking.extensions.shake
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class PhoneNumberFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentPhoneNumberBinding

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    // Image and loading state
    private var isLoading = false
    private var imgHidden = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Binding
        binding = FragmentPhoneNumberBinding.inflate(inflater, container, false)

        // Firebase
        auth = Firebase.auth
        databaseReference = Firebase.database.reference

        // Load gif
        Glide.with(requireContext()).load(R.drawable.people).into(binding.gifIv)

        // License plate editText
        binding.phoneNumberEt.addTextChangedListener(PhoneNumberFormattingTextWatcher("GE"))
        binding.phoneNumberEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (isLoading) {
                    binding.phoneNumberEt.hideKeyboard(requireContext())
                } else {
                    binding.fabSearch.performClick()
                }
                true
            } else false
        }

        binding.fabSearch.setOnClickListener {
            // ... //
            var phoneNumber = binding.phoneNumberEt.text.toString().replace(" ","")

            if (!phoneNumber.startsWith('+')){
                phoneNumber = "+995$phoneNumber"
            }

            // Validate field
            if (phoneNumber.length > 8 && phoneNumber.drop(1).isDigitsOnly() ){
                // ... //
                binding.phoneNumberEt.hideKeyboard(requireContext())
                binding.phoneNumberEt.clearFocus()

                // Check if user uploaded contacts
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    binding.fabSearch.isLoading(true)
                    isLoading = true

                    // SharedPrefs
                    val sharedPref =
                        requireContext().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                    if (!sharedPref.getBoolean("ContactsSynced", false)) {
                        uploadContacts()
                    }

                    // Search for number
                    searchWithNumber(phoneNumber)

                } else {
                    // Show permission dialog
                    val permissionDialog = PermissionsDialogFragment(
                        R.drawable.avatar,
                        getString(R.string.contacts_permission_hint),
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.READ_CONTACTS
                        ),
                        101
                    )
                    permissionDialog.show(parentFragmentManager, "")
                }
            }
            else{
                binding.textInputLayout.shake()
            }


        }

        // Hide view on keyboard open 3004
        binding.mainContainer.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.mainContainer.getWindowVisibleDisplayFrame(r)

            val heightDiff: Int =
                Resources.getSystem().displayMetrics.heightPixels - (r.bottom - r.top)

            if (heightDiff > 100) {
                // Keyboard opened
                binding.gifIv.visibility = View.GONE
                imgHidden = true
            } else {
                if (imgHidden) {
                    binding.gifIv.visibility = View.VISIBLE
                    imgHidden = false
                }
            }
        }

        return binding.root
    }

    private fun searchWithNumber(phoneNumber: String) {
        // Search
        databaseReference.child("Phones")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(phoneNumber)) {
                        val fullName = snapshot.child(phoneNumber).child("fname").value.toString()
                        findCarNumber(phoneNumber, fullName)
                    } else {
                        binding.fabSearch.isLoading(false)
                        isLoading = false
                        val snackBar = Snackbar.make(
                            binding.fabSearch,
                            getString(R.string.no_data_found_hint),
                            Snackbar.LENGTH_LONG
                        )
                        snackBar.setIcon(R.drawable.outline_person_off_24)
                        snackBar.show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.fabSearch.isLoading(false)
                    isLoading = false
                    val snackBar = Snackbar.make(
                        binding.fabSearch,
                        getString(R.string.database_error),
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setIcon(R.drawable.baseline_error_outline_24)
                    snackBar.setAction(getString(R.string.try_again)) {
                        binding.fabSearch.performClick()
                    }
                    snackBar.show()
                }
            })
    }

    private fun findCarNumber(phoneNumber: String, fullName: String) {
        databaseReference.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var numberFound = false
                    var carNumber = "Unknown"

                    snapshot.children.forEach {
                        if (!numberFound && it.child("phone").value.toString() == phoneNumber) {
                            numberFound = true
                            carNumber = it.child("car").value.toString()
                        }
                    }

                    isLoading = false
                    binding.fabSearch.isLoading(false)

                    val dialog = NumberFoundBottomSheetFragment(binding.fabSearch,fullName, carNumber, phoneNumber,false)
                    dialog.show(parentFragmentManager, "")

                }

                override fun onCancelled(error: DatabaseError) {
                    binding.fabSearch.isLoading(false)
                    isLoading = false
                    val snackBar = Snackbar.make(
                        binding.fabSearch,
                        getString(R.string.database_error),
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setIcon(R.drawable.baseline_error_outline_24)
                    snackBar.setAction(getString(R.string.try_again)) {
                        binding.fabSearch.performClick()
                    }
                    snackBar.show()
                }
            })
    }

    @SuppressLint("Range")
    private fun uploadContacts() {
        val mResolver = requireActivity().contentResolver

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val cursor = mResolver.query(uri, null, null, null, null)

        if (cursor?.count!! > 0) {
            while (cursor.moveToNext()) {
                val contactName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val contactPhone =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                databaseReference.child("Phones")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.hasChild(contactPhone)) {
                                val ref = databaseReference.child("Phones").child(contactPhone)
                                ref.child("fname").setValue(contactName)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // ... //
                        }
                    })

            }
        }
        cursor.close()

        // SharedPrefs
        val sharedPref = requireContext().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)

        val editor = sharedPref.edit()
        editor.putBoolean("ContactsSynced", true)
        editor.apply()
    }

}