package com.bro.parking.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bro.parking.fragments.CarNumberFragment
import com.bro.parking.fragments.PhoneNumberFragment

class ViewPagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> CarNumberFragment()
            1 -> PhoneNumberFragment()
            else -> CarNumberFragment()
        }
    }
}