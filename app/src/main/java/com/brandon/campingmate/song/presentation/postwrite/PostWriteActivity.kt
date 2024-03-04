package com.brandon.campingmate.song.presentation.postwrite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.brandon.campingmate.databinding.ActivityPostWriteBinding

class PostWriteActivity : AppCompatActivity() {

    private val binding: ActivityPostWriteBinding by lazy { ActivityPostWriteBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



    }


}