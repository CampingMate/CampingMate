package com.brandon.campingmate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.brandon.campingmate.databinding.ActivityCampDetailBinding

class CampDetailActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCampDetailBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}