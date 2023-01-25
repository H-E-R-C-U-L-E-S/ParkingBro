package com.bro.parking.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bro.parking.R
import com.bro.parking.adapters.MainRecyclerAdapter
import com.bro.parking.custom_components.DialogWithGif
import com.bro.parking.databinding.FragmentSaveListBinding
import com.bro.parking.helpers.SavedItems
import com.bro.parking.models.MainRecyclerItem
import com.google.android.material.snackbar.Snackbar

class SaveListFragment : Fragment() {
    private lateinit var binding: FragmentSaveListBinding
    private lateinit var mData: SavedItems
    private lateinit var savedItems: ArrayList<MainRecyclerItem>
    private lateinit var hintDialog: DialogWithGif

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaveListBinding.inflate(inflater, container, false)

        mData = SavedItems(requireContext())
        savedItems = mData.getSavedItems()

        val adapter1 = MainRecyclerAdapter(requireContext(), savedItems, false)

        val sharedPref = requireContext().getSharedPreferences("USER_SAVES", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()


        hintDialog = DialogWithGif(
            R.drawable.trash, getString(R.string.item_delete_hint), getString(R.string.ok)
        ) {
            // Ask to add car
            hintDialog.dismiss()
            editor.putBoolean("dialogShown", true)
            editor.apply()
        }
        hintDialog.show(parentFragmentManager, "")




        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.adapter = adapter1

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem: MainRecyclerItem = savedItems[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition

                savedItems.removeAt(viewHolder.adapterPosition)
                adapter1.notifyItemRemoved(viewHolder.adapterPosition)

                val snack = Snackbar.make(
                    binding.recyclerview, "Saved item deleted", Snackbar.LENGTH_LONG
                )
                snack.setAction(getString(R.string.undo)) {
                    savedItems.add(position, deletedItem)
                    adapter1.notifyItemInserted(position)
                }

                val view = snack.view
                val text =
                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                text.gravity = Gravity.CENTER_VERTICAL
                text.compoundDrawablePadding = 42
                text.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.baseline_error_outline_24, 0, 0, 0
                )
                snack.addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        mData.saveItems(savedItems)
                    }
                }).show()


            }

        }).attachToRecyclerView(binding.recyclerview)

        return binding.root
    }


}