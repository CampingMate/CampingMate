package com.brandon.campingmate

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.brandon.campingmate.databinding.ActivityPostDetailBinding
import com.brandon.campingmate.song.domain.model.PostEntity
import timber.log.Timber

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ENTITY = "extra_post_entity"
    }

    private val binding: ActivityPostDetailBinding by lazy { ActivityPostDetailBinding.inflate(layoutInflater) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val postEntity = intent.getParcelableExtra(EXTRA_POST_ENTITY, PostEntity::class.java)
        Timber.d("수신한 post 객체: $postEntity")

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.anim_none, R.anim.slide_out);
    }

}