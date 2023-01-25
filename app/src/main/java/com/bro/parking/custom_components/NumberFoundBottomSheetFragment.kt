package com.bro.parking.custom_components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.bro.parking.R
import com.bro.parking.databinding.NumberFoundBottomSheetBinding
import com.bro.parking.helpers.SavedItems
import com.bro.parking.models.MainRecyclerItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class NumberFoundBottomSheetFragment(
    private val viewForSnackBar : View,
    private val fullName: String = "",
    private val carNumber: String = "",
    private val phoneNumber: String = "",
    private val isFromCarNumber: Boolean
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = NumberFoundBottomSheetBinding.inflate(inflater, container, false)
        binding.fullNameEt.text = fullName
        binding.phoneNumberEt.text = phoneNumber
        binding.carNumberEt.text = carNumber
        binding.fabClose.setOnClickListener {
            this.dismissNow()
        }
        binding.callButton.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:$phoneNumber")
                startActivity(callIntent)
            } else {
                this.dismiss()
                val permissionDialog = PermissionsDialogFragment(
                    R.drawable.phone_call,
                    getString(R.string.call_permission_hint),
                    requireActivity(),
                    arrayOf(Manifest.permission.CALL_PHONE),
                    101
                )
                permissionDialog.show(parentFragmentManager, "")
            }
        }
        binding.copyButton.setOnClickListener {
            this.dismiss()
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText(null, phoneNumber)
            clipboard.setPrimaryClip(clip)
            val snack = Snackbar.make(
                viewForSnackBar, "Phone number copied", Snackbar.LENGTH_LONG
            )
            val view = snack.view
            val text =
                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            text.gravity = Gravity.CENTER_VERTICAL
            text.compoundDrawablePadding = 42
            text.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_copy_all_24, 0, 0, 0
            )
            snack.show()
        }

        binding.saveButton.setOnClickListener {
            this.dismiss()
            val s = SavedItems(requireContext())
            val k = s.getSavedItems()
            val searchType = if (isFromCarNumber){
                "Car license plate"
            } else{
                "Phone number"
            }

            val newItem = MainRecyclerItem(carNumber,searchType)
            k.add(newItem)
            s.saveItems(k)

            val snack = Snackbar.make(
                viewForSnackBar, getString(R.string.added_to_saves), Snackbar.LENGTH_LONG
            )
            val view = snack.view
            val text =
                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            text.gravity = Gravity.CENTER_VERTICAL
            text.compoundDrawablePadding = 42
            text.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_done_all_24, 0, 0, 0
            )
            snack.show()

        }
        binding.shareButton.setOnClickListener {
            this.dismiss()
            val shareTxt =
                "Full name: $fullName\nPhone number: $phoneNumber\nCar number: $carNumber"
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareTxt)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Person info")
            startActivity(Intent.createChooser(shareIntent, "Share info via"))

        }
        return binding.root
    }

}