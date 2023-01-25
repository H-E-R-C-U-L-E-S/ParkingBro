package com.bro.parking.custom_components

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.bro.parking.databinding.PermissionDialogLayoutBinding
import com.bumptech.glide.Glide

class DialogWithGif(
    private val drawable: Int,
    private val text: String,
    private val buttonText : String,
    private val clickListener: OnClickListener
) : DialogFragment() {

    private lateinit var binding: PermissionDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PermissionDialogLayoutBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Glide.with(requireContext()).load(drawable).into(binding.imageView)
        binding.permissionAskTv.text = text
        binding.continueButton.text = buttonText

        binding.continueButton.setOnClickListener(clickListener)
        return binding.root
    }

}
