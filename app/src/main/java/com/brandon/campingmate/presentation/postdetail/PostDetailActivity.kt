package com.brandon.campingmate.presentation.postdetail

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.brandon.campingmate.R
import com.brandon.campingmate.data.repository.PostRepositoryImpl
import com.brandon.campingmate.data.source.network.impl.PostRemoteDataSourceImpl
import com.brandon.campingmate.databinding.ActivityPostDetailBinding
import com.brandon.campingmate.domain.usecase.GetPostByIdUseCase
import com.brandon.campingmate.network.firestore.FireStoreService
import com.brandon.campingmate.presentation.postdetail.adapter.ImageListAdapter
import com.brandon.campingmate.utils.toFormattedString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }

    private val binding: ActivityPostDetailBinding by lazy { ActivityPostDetailBinding.inflate(layoutInflater) }

    private val imageListAdapter: ImageListAdapter by lazy {
        ImageListAdapter(emptyList())
    }

    private val viewModel: PostDetailViewModel by viewModels {
        PostDetailViewModelFactory(
            GetPostByIdUseCase(PostRepositoryImpl(PostRemoteDataSourceImpl(FireStoreService.fireStoreDB))),
        )
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val postId = intent.getStringExtra(EXTRA_POST_ID)
        viewModel.loadData(postId)

        initView()
        initViewModel()

        setupOnBackPressedHandling()
    }

    private fun initViewModel() = with(viewModel) {
        lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(lifecycle).collectLatest { state ->
                updateUI(state)
            }
        }

        lifecycleScope.launch {
            viewModel.event.flowWithLifecycle(lifecycle).collectLatest { event ->
                handleEvent(event)
            }
        }
    }

    private fun handleEvent(event: PostDetailEvent) {
        TODO("Not yet implemented")
    }

    private fun updateUI(state: PostDetailUiState) {
        state.post?.let {
            with(binding) {
                tvUsername.text = it.authorName
                tvTitle.text = it.title
                tvCreatedAt.text = it.timestamp.toFormattedString()
                tvContent.text = it.content
                ivUserProfile.load(it.authorProfileImageUrl)
                imageListAdapter.setImageUrls(it.imageUrlList)
                imageListAdapter.notifyDataSetChanged()
                // TODO 댓글 목록
            }
        }
    }

    private fun initView() = with(binding) {
        // 툴바 활성화
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 기본 타이틀 숨기기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)// 뒤로가기 버튼 활성화

        rvPostImages.layoutManager =
            LinearLayoutManager(this@PostDetailActivity, LinearLayoutManager.HORIZONTAL, false)
        rvPostImages.adapter = imageListAdapter
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
                // 여기에서 애니메이션 적용 후 활동 종료
                ActivityCompat.finishAfterTransition(this@PostDetailActivity)
                overridePendingTransition(R.anim.anim_none, R.anim.slide_out)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

}