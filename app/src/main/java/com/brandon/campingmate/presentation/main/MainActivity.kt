package com.brandon.campingmate.presentation.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivityMainBinding
import com.brandon.campingmate.presentation.splash.SplashViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val sharedViewModel: SplashViewModel by viewModels()


    private var backPressedTime: Long = 0


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish()
        } else {
            Toast.makeText(this, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setSplash()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Timber.plant(Timber.DebugTree())
        initView()

    }

    private fun setSplash() {
        val splashScreen = installSplashScreen()


        sharedViewModel.isLoading.observe(this) { isLoading ->
            splashScreen.setKeepOnScreenCondition { isLoading }
        }

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

