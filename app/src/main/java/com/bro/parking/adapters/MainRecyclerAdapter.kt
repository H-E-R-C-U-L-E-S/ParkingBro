package com.bro.parking.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bro.parking.R
import com.bro.parking.databinding.ProfileRecyclerItemBinding
import com.bro.parking.extensions.showKeyboard
import com.bro.parking.models.MainRecyclerItem

class MainRecyclerAdapter(private val context : Context, private val lst: ArrayList<MainRecyclerItem>, private val isProfile : Boolean = true) : RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder>() {

    // Binding
    private lateinit var binding :ProfileRecyclerItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ProfileRecyclerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return lst.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = lst[position]
        holder.mainTextEt.setText(currentItem.mainInfo)
        holder.secondaryTextTv.text = currentItem.infoType

        if (isProfile){
            holder.endButton.visibility = View.VISIBLE
        }

        when (currentItem.infoType) {
            "Phone number" -> {
                holder.mainIcon.setImageDrawable(getDrawable(context,R.drawable.baseline_call_24))
                holder.endButton.isEnabled = false
                holder.endButton.setImageDrawable(getDrawable(context,R.drawable.baseline_verified_24))
            }
            "Email address" -> {
                holder.mainTextEt.hint = "someone@example.com"
                holder.mainIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_email_24))
                holder.endButton.setImageDrawable(getDrawable(context,R.drawable.baseline_edit_24))
            }
            "Car license plate" -> {
                holder.mainTextEt.hint = context.getString(R.string.car_search_hint)
                holder.mainIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_directions_car_24))
                holder.endButton.setImageDrawable(getDrawable(context,R.drawable.baseline_edit_24))
            }
        }

        holder.endButton.setOnClickListener{
            holder.mainTextEt.isEnabled = true
            holder.mainTextEt.setSelection(holder.mainTextEt.length())
            holder.mainTextEt.requestFocus()
            holder.mainTextEt.showKeyboard(context)
            holder.endButton.setImageDrawable(getDrawable(context,R.drawable.baseline_save_as_24))
        }


    }

    class ViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        val mainTextEt : EditText = itemView.findViewById(R.id.main_text_et)
        val secondaryTextTv : TextView = itemView.findViewById(R.id.secondary_text_tv)
        val mainIcon : ImageView = itemView.findViewById(R.id.main_iv)
        val endButton : ImageView = itemView.findViewById(R.id.end_button)
    }

}