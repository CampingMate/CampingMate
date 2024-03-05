package com.brandon.campingmate.presentation.postwrite

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivityPostWriteBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class PostWriteActivity : AppCompatActivity() {

    private val binding: ActivityPostWriteBinding by lazy { ActivityPostWriteBinding.inflate(layoutInflater) }

    private val viewModel: PostWriteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initListener()
        initViewModel()
        setupOnBackPressedHandling()
    }

    private fun initViewModel() = with(viewModel) {
        lifecycleScope.launch {
            event.flowWithLifecycle(lifecycle).collectLatest { event ->

                onEvent(event)
            }
        }
    }

    private fun onEvent(event: PostWriteEvent) {
        when (event) {
            PostWriteEvent.UploadPost -> {
                Timber.d("포스팅 업로드 이벤트 발생!!")
                // TODO send post
            }
        }
    }


    private fun initView() = with(binding) {
        // 툴바 활성화
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 기본 타이틀 숨기기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)// 뒤로가기 버튼 활성화
    }

    private fun initListener() = with(binding) {
        btnPostUpload.setOnClickListener {
            viewModel.handleEvent(PostWriteEvent.UploadPost)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupOnBackPressedHandling() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 여기에서 애니메이션 적용 후 활동 종료
                ActivityCompat.finishAfterTransition(this@PostWriteActivity)
                overridePendingTransition(R.anim.anim_none, R.anim.slide_out)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


}