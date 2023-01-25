package com.bro.parking.fragments

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bro.parking.adapters.MainRecyclerAdapter
import com.bro.parking.databinding.FragmentProfileBinding
import com.bro.parking.models.MainRecyclerItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var adapter: MainRecyclerAdapter
    private var profileItems = ArrayList<MainRecyclerItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = MainRecyclerAdapter(requireContext(), profileItems)
        binding.recyclerview.adapter = adapter

        auth = Firebase.auth
        databaseReference = Firebase.database.reference

        val sharedPref = requireContext().getSharedPreferences("USER_PROFILE", Context.MODE_PRIVATE)

        if (sharedPref.getBoolean("profileIsLoaded", false)) {
            val fname = sharedPref.getString("Fname","")
            val phone = sharedPref.getString("Phone","")
            val email = sharedPref.getString("Email","")
            val car = sharedPref.getString("Car","")

            binding.userNameTv.text = fname
            if (!phone.isNullOrEmpty()){
                profileItems.add(MainRecyclerItem(phone,"Phone number"))
                adapter.notifyItemInserted(profileItems.size)
            }
            if (!email.isNullOrEmpty()){
                profileItems.add(MainRecyclerItem(email,"Email address"))
                adapter.notifyItemInserted(profileItems.size)
            }
            if (!car.isNullOrEmpty()){
                profileItems.add(MainRecyclerItem(car,"Car license plate",))
                adapter.notifyItemInserted(profileItems.size)
            }

        } else {
            getUserData()
        }

        // Hide view on keyboard open
        binding.mainContainer.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.mainContainer.getWindowVisibleDisplayFrame(r)

            val heightDiff: Int =
                Resources.getSystem().displayMetrics.heightPixels - (r.bottom - r.top)

            if (heightDiff > 100) {
                // Keyboard opened
                binding.wrapContainer.visibility = View.GONE
            } else {
                binding.wrapContainer.visibility = View.VISIBLE
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun getUserData() {
        val user = auth.currentUser
        val phoneNumber = user?.phoneNumber.toString()

        val sharedPref = requireContext().getSharedPreferences("USER_PROFILE", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        if (user != null) {
            profileItems.add(MainRecyclerItem(phoneNumber, "Phone number", false))
            adapter.notifyItemInserted(0)
            editor.putString("Phone", phoneNumber)
            editor.apply()
        }

        databaseReference.child("Phones").child(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("fname")) {
                        binding.userNameTv.text = snapshot.child("fname").value.toString()
                        editor.putString("Fname", snapshot.child("fname").value.toString())
                        editor.apply()
                    }
                    if (snapshot.hasChild("email")) {
                        editor.putString("Email", snapshot.child("email").value.toString())
                        editor.apply()
                        profileItems.add(
                            MainRecyclerItem(
                                snapshot.child("email").value.toString(), "Email address"
                            )
                        )
                        adapter.notifyItemInserted(profileItems.size)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // ... //
                }

            })

        databaseReference.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var numberFound = false
                    snapshot.children.forEach {
                        if (!numberFound && it.child("phone").value.toString() == phoneNumber) {
                            numberFound = true
                            profileItems.add(
                                MainRecyclerItem(
                                    it.child("car").value.toString(), "Car license plate"
                                )
                            )
                            adapter.notifyItemInserted(profileItems.size)
                            editor.putString("Car", it.child("car").value.toString())
                            editor.apply()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        editor.putBoolean("profileIsLoaded",true)
        editor.apply()

    }

}