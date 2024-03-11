package com.brandon.campingmate.presentation.postwrite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.brandon.campingmate.R
import com.brandon.campingmate.data.repository.PostRepositoryImpl
import com.brandon.campingmate.data.source.network.impl.PostRemoteDataSourceImpl
import com.brandon.campingmate.databinding.ActivityPostWriteBinding
import com.brandon.campingmate.domain.usecase.UploadPostUseCase
import com.brandon.campingmate.network.firestore.FireStoreService.fireStoreDB
import com.brandon.campingmate.presentation.postdetail.PostDetailActivity
import com.brandon.campingmate.presentation.postdetail.PostDetailActivity.Companion.EXTRA_POST_ID
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class PostWriteActivity : AppCompatActivity() {

    private val binding: ActivityPostWriteBinding by lazy { ActivityPostWriteBinding.inflate(layoutInflater) }

    private val viewModel: PostWriteViewModel by viewModels {
        PostWriteViewModelFactory(
            UploadPostUseCase(PostRepositoryImpl(PostRemoteDataSourceImpl(fireStoreDB)))
        )
    }

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

        // TODO 텍스트 입력 여부에 따른 버튼 활성/비활성화
    }

    private fun onEvent(event: PostWriteEvent) {
        when (event) {
            is PostWriteEvent.PostUploadSuccess -> {
                // 화면 깜박임 방지
                hideKeyboard()
                // Navigate to PostDetail
                val intent = Intent(this, PostDetailActivity::class.java).apply {
                    putExtra(EXTRA_POST_ID, event.postId)
                }
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_up,
                    R.anim.anim_none
                ).toBundle()
                setResult(Activity.RESULT_OK)
                startActivity(intent, options)
//                startActivityForResult(intent, POST_WRITE_REQUEST_CODE, options)
                ActivityCompat.finishAfterTransition(this)
            }

            else -> {}
        }
    }


    private fun initView() = with(binding) {
        // 툴바 활성화
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 기본 타이틀 숨기기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)// 뒤로가기 버튼 활성화
    }

    private fun initListener() = with(binding) {
        // TODO 어쩌다 터치 두번되는 문제 있음
        btnPostUpload.setOnClickListener {
            val title = binding.tvTitle.text.toString()
            val content = binding.tvContent.text.toString()
            viewModel.handleEvent(
                PostWriteEvent.UploadPost(
                    title = title,
                    content = content,
                )
            )
        }
        btnAddIMage.setOnClickListener {
            // 권한 얻기 + 갤러리에서 이미지 가져오기
            checkPermissionAndPickImage()
        }
    }

    private fun checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery()
            } else {
                Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupOnBackPressedHandling() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Timber.d("PostWriteActivity 종료")
                // 여기에서 애니메이션 적용 후 활동 종료
                ActivityCompat.finishAfterTransition(this@PostWriteActivity)
                overridePendingTransition(R.anim.anim_none, R.anim.slide_out)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = 100
        private const val PICK_IMAGE_REQUEST = 101
    }
}