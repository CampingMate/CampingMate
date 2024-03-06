package com.brandon.campingmate.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.brandon.campingmate.databinding.ActivityMainBinding
import nl.joery.animatedbottombar.AnimatedBottomBar
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Timber.plant(Timber.DebugTree())

        binding.viewPager.adapter = ViewPager2Adapter(supportFragmentManager, lifecycle)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 4
        binding.bottomNavigation.setupWithViewPager2(binding.viewPager)

        binding.bottomNavigation.setOnTabSelectListener(object: AnimatedBottomBar.OnTabSelectListener{
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                binding.viewPager.setCurrentItem(newIndex, false)
            }
        })
    }
}

