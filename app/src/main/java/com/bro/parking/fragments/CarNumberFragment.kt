package com.bro.parking.fragments

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bro.parking.R
import com.bro.parking.custom_components.BottomSheetWithEditText
import com.bro.parking.custom_components.DialogWithGif
import com.bro.parking.custom_components.NumberFoundBottomSheetFragment
import com.bro.parking.databinding.FragmentCarNumberBinding
import com.bro.parking.extensions.hideKeyboard
import com.bro.parking.extensions.setIcon
import com.bro.parking.extensions.shake
import com.bro.parking.text_watchers.CarNumberTextWatcher
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


class CarNumberFragment : Fragment() {

    // Binding
    private lateinit var binding: FragmentCarNumberBinding

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    // Image and loading state
    private var imgHidden = false
    private var isLoading = false

    // Dialog
    private lateinit var askAddCarDialog: DialogWithGif

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Binding
        binding = FragmentCarNumberBinding.inflate(inflater, container, false)

        // Firebase
        auth = Firebase.auth
        databaseReference = Firebase.database.reference


        // Load gif
        Glide.with(requireContext()).load(R.drawable.license_plate).into(binding.gifIv)

        // Car plate editText
        binding.carLicensePlateEt.addTextChangedListener(CarNumberTextWatcher())
        binding.carLicensePlateEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (isLoading) {
                    binding.carLicensePlateEt.hideKeyboard(requireContext())
                } else {
                    binding.fabSearch.performClick()
                }
                true
            } else false
        }

        // Search fab
        binding.fabSearch.setOnClickListener { _ ->
            // ... //
            val carNumber = binding.carLicensePlateEt.text.toString().uppercase()

            // Check info
            if (carNumber.count { it.isLetter() } == 4 && carNumber.count { it == '-' } == 2 && carNumber.count { it.isDigit() } == 3) {
                binding.fabSearch.isLoading(true)
                isLoading = true
                binding.carLicensePlateEt.hideKeyboard(requireContext())
                binding.carLicensePlateEt.clearFocus()
                search(carNumber)
            } else {
                binding.textInputLayout.shake()
            }

        }

        // Hide view on keyboard open
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

    private fun search(carNumber: String) {
        // Get user uid
        val userUid = auth.currentUser?.uid.toString()

        // Check if user car
        databaseReference.child("Users").child(userUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount > 1) {
                        searchWithCarNumber(carNumber)
                    } else {
                        isLoading = false
                        binding.fabSearch.isLoading(false)

                        askAddCarDialog =
                            DialogWithGif(R.drawable.circle_add, getString(R.string.add_car_hint),getString(R.string.Continue)) {
                                // Ask to add car
                                askAddCarDialog.dismiss()
                                showBottomSheetWithEditText()
                            }
                        askAddCarDialog.show(parentFragmentManager, "")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.fabSearch.isLoading(false)
                    isLoading = false
                    val snack = Snackbar.make(
                        binding.fabSearch, getString(R.string.database_error), Snackbar.LENGTH_LONG
                    )
                    snack.setAction(getString(R.string.try_again)) {
                        binding.fabSearch.performClick()
                        snack.dismiss()
                    }
                    val view = snack.view
                    val text =
                        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    text.gravity = Gravity.CENTER_VERTICAL
                    text.compoundDrawablePadding = 42
                    text.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.baseline_error_outline_24, 0, 0, 0
                    )
                    snack.show()
                }
            })
    }

    private fun showBottomSheetWithEditText() {
        val d = BottomSheetWithEditText(binding.fabSearch)
        d.show(parentFragmentManager, "")
    }

    private fun searchWithCarNumber(carNumber: String) {
        // Search for phone number
        databaseReference.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var numberFound = false
                    var phoneNumber = ""

                    snapshot.children.forEach {
                        if (!numberFound && it.hasChild("car")) {
                            if (it.child("car").value.toString() == carNumber) {
                                numberFound = true
                                phoneNumber = it.child("phone").value.toString()
                            }
                        }
                    }

                    if (!numberFound) {
                        binding.fabSearch.isLoading(false)
                        isLoading = false

                        val snackBar = Snackbar.make(
                            binding.fabSearch,
                            getString(R.string.no_data_found_hint),
                            Snackbar.LENGTH_LONG
                        )
                        snackBar.setIcon(R.drawable.outline_person_off_24)
                        snackBar.show()

                    } else {
                        searchForPhoneNumberOwner(phoneNumber, carNumber)
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

    private fun searchForPhoneNumberOwner(phone: String, carNumber: String) {

        databaseReference.child("Phones")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var fullName = getString(R.string.name_unknown)

                    if (snapshot.hasChild(phone)) {
                        fullName = snapshot.child(phone).child("fname").value.toString()
                    }

                    isLoading = false
                    binding.fabSearch.isLoading(false)

                    val dialog = NumberFoundBottomSheetFragment(
                        binding.fabSearch, fullName, carNumber, phone, true
                    )
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

}