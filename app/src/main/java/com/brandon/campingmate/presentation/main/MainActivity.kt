package com.brandon.campingmate.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.brandon.campingmate.databinding.ActivityMainBinding
import com.kakao.sdk.common.util.Utility
import nl.joery.animatedbottombar.AnimatedBottomBar
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        var keyHash = Utility.getKeyHash(this)
//        Log.d("keyHash", "$keyHash")

        Timber.plant(Timber.DebugTree())

        binding.viewPager.adapter = ViewPager2Adapter(supportFragmentManager, lifecycle)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 1
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

