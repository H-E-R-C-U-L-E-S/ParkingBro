package com.bro.parking.fragments


import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bro.parking.adapters.ViewPagerAdapter
import com.bro.parking.databinding.FragmentSearchBinding
import com.google.android.material.tabs.TabLayoutMediator


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var tabLayoutHidden = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Car numbers"
                1 -> tab.text = "Phone numbers"
            }

        }.attach()

        binding.mainContainer.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.mainContainer.getWindowVisibleDisplayFrame(r)
            val heightDiff: Int =
                Resources.getSystem().displayMetrics.heightPixels - (r.bottom - r.top)
            if (heightDiff > 100) {
                //enter your code here
                binding.tabLayout.visibility = View.GONE
                tabLayoutHidden = true
            } else {
                if (tabLayoutHidden){
                    binding.tabLayout.visibility = View.VISIBLE
                    tabLayoutHidden = false
                }
            }
        }


        // Inflate the layout for this fragment
        return binding.root
    }
}