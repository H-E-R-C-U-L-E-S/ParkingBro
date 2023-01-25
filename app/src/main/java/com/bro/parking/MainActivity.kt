package com.bro.parking


import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bro.parking.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // Binding
    private lateinit var binding: ActivityMainBinding

    // NavState
    private var navHidden= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)


        binding.mainContainer.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.mainContainer.getWindowVisibleDisplayFrame(r)
            val heightDiff: Int = Resources.getSystem().displayMetrics.heightPixels - (r.bottom - r.top)
            if (heightDiff > 100) {
                // Opened
                binding.bottomNavigationView.visibility = View.GONE
                navHidden = true
            } else {
                if (navHidden){
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    navHidden = false
                }
            }
        }

    }
}