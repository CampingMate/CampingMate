package com.brandon.campingmate.presentation.postdetail

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivityPostDetailBinding
import com.brandon.campingmate.domain.model.PostEntity
import timber.log.Timber

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ENTITY = "extra_post_entity"
    }

    private val binding: ActivityPostDetailBinding by lazy { ActivityPostDetailBinding.inflate(layoutInflater) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val postEntity = intent.getParcelableExtra(EXTRA_POST_ENTITY, PostEntity::class.java)
        Timber.d("수신한 post 객체: $postEntity")

        setupOnBackPressedHandling()
    }

    private fun setupOnBackPressedHandling() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 여기에서 애니메이션 적용 후 활동 종료
                ActivityCompat.finishAfterTransition(this@PostDetailActivity)
                overridePendingTransition(R.anim.anim_none, R.anim.slide_out)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

}