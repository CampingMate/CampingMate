package com.brandon.campingmate.presentation.main

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.brandon.campingmate.R
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.brandon.campingmate.databinding.ActivityMainBinding
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.presentation.splash.SplashViewModel
import com.kakao.sdk.common.util.Utility
import nl.joery.animatedbottombar.AnimatedBottomBar
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var splashScreen: SplashScreen

    private val viewModel by lazy {
        ViewModelProvider(this)[SplashViewModel::class.java]
    }

//    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Main", "#csh onCreate")

//        installSplashScreen().apply {
//            Log.d("Main", "#csh installSplashScreen")
//            setKeepOnScreenCondition{viewModel.isLoading.value}
//        }
        splashScreen = installSplashScreen()
        startSplash()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        var keyHash = Utility.getKeyHash(this)
//        Log.d("keyHash", "$keyHash")

        Timber.plant(Timber.DebugTree())

//        homeCity = intent.getParcelableArrayListExtra("homeCity") ?: arrayListOf()
//        homeTheme = intent.getParcelableArrayListExtra("homeTheme")?: arrayListOf()
//        Log.d("Main","#csh homeCity: $homeCity")
//        Log.d("Main","#csh homeTheme: $homeTheme")

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

    private fun startSplash() {
//            viewModel.isGet.asLiveData().observe(this) { isDataLoaded ->
//                val isCityLoaded = isDataLoaded["city"]?:false
//                val isThemeLoaded = isDataLoaded["theme"]?:false
//                if (isCityLoaded&&isThemeLoaded) {
//                    splashScreen.setKeepOnScreenCondition { viewModel.isLoading.value }
//            }
//        }
//        viewModel.loadData()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 5f, 1f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 5f, 1f)

//            ObjectAnimator.ofPropertyValuesHolder(splashScreenView.iconView, scaleX, scaleY).run {
            ObjectAnimator.ofPropertyValuesHolder(splashScreenView.iconView).run {
                interpolator = AnticipateInterpolator()
                duration = 1000L
                doOnEnd {
                    splashScreenView.remove()
                }
                Log.d("Main", "#csh duration start")
                start()
            }
        }
    }

//    private val isGetDataObserver = Observer<Int> { loadedCount ->
//        if (loadedCount == 2 && !isDataLoaded) { // 데이터를 가져오고, 로딩이 완료되지 않았을 때만 실행
//            handleDataLoaded()
//        }
//    }

//    private fun handleDataLoaded(){
//        isDataLoaded = true // 데이터가 로딩되었음을 표시
//        homeCity = viewModel.allCityData
//        homeTheme = viewModel.allThemeData
////        startActivity(Intent(this@MainActivity, MainActivity::class.java))
////        finish() // 현재 액티비티 종료
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        // Activity가 종료될 때 LiveData의 observe를 제거하여 메모리 누수를 방지
//        viewModel.isGet.removeObserver(isGetDataObserver)
//    }

}

