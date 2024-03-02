package com.brandon.campingmate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PostDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_none, R.anim.slide_out);
    }
}