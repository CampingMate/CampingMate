package com.brandon.campingmate.presentation.postdetail

import android.annotation.SuppressLint
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
import com.brandon.campingmate.data.remote.firebasestorage.FireBaseStorageDataSourceImpl
import com.brandon.campingmate.data.remote.firestore.FirestoreDataSourceImpl
import com.brandon.campingmate.data.repository.PostRepositoryImpl
import com.brandon.campingmate.databinding.ActivityPostDetailBinding
import com.brandon.campingmate.domain.usecase.GetPostByIdUseCase
import com.brandon.campingmate.domain.usecase.UploadPostCommentUseCase
import com.brandon.campingmate.network.firestore.FirebaseService
import com.brandon.campingmate.network.firestore.FirebaseService.fireStoreDB
import com.brandon.campingmate.presentation.postdetail.adapter.PostDetailImageListAdapter
import com.brandon.campingmate.utils.toFormattedString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }

    private val binding: ActivityPostDetailBinding by lazy { ActivityPostDetailBinding.inflate(layoutInflater) }

    private val imageListAdapter: PostDetailImageListAdapter by lazy {
        PostDetailImageListAdapter(emptyList())
    }

    private val viewModel: PostDetailViewModel by viewModels {
        PostDetailViewModelFactory(
            GetPostByIdUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB),
                    FireBaseStorageDataSourceImpl(FirebaseService.firebaseStorage)
                )
            ),
            UploadPostCommentUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB),
                    FireBaseStorageDataSourceImpl(FirebaseService.firebaseStorage)
                )
            ),
        )
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val postId = intent.getStringExtra(EXTRA_POST_ID)
        viewModel.loadData(postId)

        initView()
        initListener()
        initViewModel()
        setupOnBackPressedHandling()

    }

    private fun initListener() = with(binding) {
        btnSend.setOnClickListener {
            val content = etCommentInput.text.toString()
            viewModel.handleEvent(PostDetailEvent.UploadComment(content))
        }
    }

    private fun initViewModel() = with(viewModel) {
        lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(lifecycle).collectLatest { state ->
                onBind(state)
            }
        }

        lifecycleScope.launch {
            viewModel.event.flowWithLifecycle(lifecycle).collectLatest { event ->
                onEvent(event)
            }
        }
    }

    private fun onEvent(event: PostDetailEvent) {
        TODO("Not yet implemented")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onBind(state: PostDetailUiState) {
        state.post?.let {
            Timber.tag("USER").d("State: ${state.post}")
            with(binding) {
                tvUsername.text = it.authorName
                tvTitle.text = it.title
                tvCreatedAt.text = it.timestamp.toFormattedString()
                tvContent.text = it.content
                ivUserProfile.load(it.authorProfileImageUrl)
                imageListAdapter.setImageUrls(it.imageUrls)
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

        rvPostImage.layoutManager =
            LinearLayoutManager(this@PostDetailActivity, LinearLayoutManager.HORIZONTAL, false)
        rvPostImage.adapter = imageListAdapter
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