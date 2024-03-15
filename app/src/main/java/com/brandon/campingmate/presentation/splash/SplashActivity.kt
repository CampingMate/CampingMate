package com.brandon.campingmate.presentation.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.brandon.campingmate.R
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.main.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class SplashActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this)[SplashViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("Splash Activity","#csh onCreate")
        var isGet:Int = 0
        viewModel.loadData()
        Log.d("Splash Activity","#csh getData start")

        viewModel.isGet.observe(this){
            if(it == 2){
                val intent = Intent(this, MainActivity::class.java)
                intent.putParcelableArrayListExtra("homeCity", ArrayList(viewModel.allCityData))
                intent.putParcelableArrayListExtra("homeTheme", ArrayList(viewModel.allThemeData))
                startActivity(intent)
                finish()
            }
        }
//        viewModel.allCityData.observe(this){
//
//            isGet+=1
//            if(isGet == 2){
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }
//        viewModel.allThemeData.observe(this){
//            isGet+=1
//            if(isGet == 2){
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }

    }
}