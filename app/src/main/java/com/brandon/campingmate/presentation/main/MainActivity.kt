package com.brandon.campingmate.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivityMain2Binding
import com.brandon.campingmate.domain.model.HomeEntity
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMain2Binding.inflate(layoutInflater) }
    var homeCity = ArrayList<HomeEntity>()
    var homeTheme = ArrayList<HomeEntity>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        var keyHash = Utility.getKeyHash(this)
//        Log.d("keyHash", "$keyHash")

        Timber.plant(Timber.DebugTree())

        homeCity = intent.getParcelableArrayListExtra("homeCity") ?: arrayListOf()
        homeTheme = intent.getParcelableArrayListExtra("homeTheme") ?: arrayListOf()
        Log.d("Main", "#csh homeCity: $homeCity")
        Log.d("Main", "#csh homeTheme: $homeTheme")

        initView()

    }

    private fun initView() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val navController = navHostFragment?.navController
        if (navController != null) {
            binding.bottomNavigation.setupWithNavController(navController)
        }
    }
}

