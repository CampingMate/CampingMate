package com.brandon.campingmate.presentation.main

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivityMainBinding
import com.brandon.campingmate.presentation.splash.SplashViewModel
import com.brandon.campingmate.utils.isInternetAvailable
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val sharedViewModel: SplashViewModel by viewModels()
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

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

        checkInternetConnection()

        Timber.plant(Timber.DebugTree())
        setupNetworkCallback()
        initView()
    }

    private fun checkInternetConnection() = with(binding) {
        if (!isInternetAvailable(this@MainActivity)) {
            Timber.d("인터넷 연결이 필요합니다.")
            clNoNetwork.isVisible = true
        } else {
            clNoNetwork.isVisible = false
        }
    }

    private fun setupNetworkCallback() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.tag("NETWORK").d("인터넷 연결중")
                runOnUiThread {
                    binding.clNoNetwork.isVisible = false
                }
            }

            override fun onLost(network: Network) {
                Timber.tag("NETWORK").d("인터넷 실패")
                runOnUiThread {
                    binding.clNoNetwork.isVisible = true
                }
            }
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::connectivityManager.isInitialized) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private fun setSplash() {
        val splashScreen = installSplashScreen()

        sharedViewModel.isLoading.observe(this) { isLoading ->
            Handler(Looper.getMainLooper()).postDelayed({
                splashScreen.setKeepOnScreenCondition { isLoading }
            }, 600)
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

