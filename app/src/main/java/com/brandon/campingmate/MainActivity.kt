package com.brandon.campingmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.brandon.campingmate.databinding.ActivityMainBinding
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        setupBottomNavigation()

    }

    private fun setupBottomNavigation() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                // 여기에서 NavController를 사용하여 화면 전환을 수행합니다.
                // 예를 들어, 각 탭의 ID 또는 태그에 따라 다른 화면으로 이동할 수 있습니다.
                when (newTab.id) {
                    R.id.tab_home -> navController.navigate(R.id.homeFragment)
                    R.id.tab_board -> navController.navigate(R.id.boardFragment)
                    R.id.tab_chat -> navController.navigate(R.id.chatFragment)
                    R.id.tab_map -> navController.navigate(R.id.mapFragment)
                    R.id.tab_profile -> navController.navigate(R.id.profileFragment)
                }
            }
        })
    }

}