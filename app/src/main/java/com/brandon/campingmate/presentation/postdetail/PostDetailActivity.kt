package com.brandon.campingmate.presentation.postdetail

import LinearVerticalItemDecoration
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.brandon.campingmate.R
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.data.local.preferences.PreferencesDataSourceImpl
import com.brandon.campingmate.data.remote.firebasestorage.FireBaseStorageDataSourceImpl
import com.brandon.campingmate.data.remote.firestore.FirestoreDataSourceImpl
import com.brandon.campingmate.data.repository.PostRepositoryImpl
import com.brandon.campingmate.data.repository.UserRepositoryImpl
import com.brandon.campingmate.databinding.ActivityPostDetailBinding
import com.brandon.campingmate.domain.usecase.GetPostByIdUseCase
import com.brandon.campingmate.domain.usecase.GetPostCommentsUseCase
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.domain.usecase.UploadPostCommentUseCase
import com.brandon.campingmate.network.firestore.FirebaseService
import com.brandon.campingmate.network.firestore.FirebaseService.fireStoreDB
import com.brandon.campingmate.presentation.postdetail.adapter.PostDetailCommentListAdapter
import com.brandon.campingmate.presentation.postdetail.adapter.PostDetailImageListAdapter
import com.brandon.campingmate.utils.toFormattedString
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
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

    private val commentListAdapter: PostDetailCommentListAdapter by lazy {
        PostDetailCommentListAdapter()
    }

    private val viewModel: PostDetailViewModel by viewModels {
        PostDetailViewModelFactory(
            GetPostByIdUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB),
                    FireBaseStorageDataSourceImpl(FirebaseService.firebaseStorage)
                )
            ), UploadPostCommentUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB),
                    FireBaseStorageDataSourceImpl(FirebaseService.firebaseStorage)
                )
            ), GetPostCommentsUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB),
                    FireBaseStorageDataSourceImpl(FirebaseService.firebaseStorage)
                )
            ), GetUserUserCase(
                UserRepositoryImpl(
                    PreferencesDataSourceImpl(
                        EncryptedPrefs.sharedPreferences
                    ), FirestoreDataSourceImpl(
                        fireStoreDB
                    )
                )
            )
        )
    }

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val postId = intent.getStringExtra(EXTRA_POST_ID)
        viewModel.getPost(postId)

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

        btnRecentCommnet.setOnClickListener {
            bottomSheetBehavior?.let {
                it.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        bottomSheetLayout.btnClose.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        hideKeyboard()
                        binding.commentBarContainer.isVisible = false
                    }

                    else -> binding.commentBarContainer.isVisible = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드하는 동안 배경 투명도 조절
                binding.commentBarContainer.isVisible = (slideOffset < -0.8).not()
                when (slideOffset) {
                    in 0f..1f -> binding.nsContainer.alpha = 0.5f
                    in -1f..0f -> binding.nsContainer.alpha = 1 - 0.5f * (slideOffset + 1)
                    else -> binding.nsContainer.alpha = 0.5f
                }
            }
        })

        val rootView = binding.root
        var previousHeightDiff: Int = 0
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val heightDiff = rootView.rootView.height - (rect.bottom - rect.top)
            val isKeyboardActive = heightDiff > 500
            if (heightDiff != previousHeightDiff) {
                Timber.tag("VIEW").d("isKeyboardActive: $isKeyboardActive")
                when (isKeyboardActive) {
                    true -> {
                        binding.overlayView.isVisible = true
                    }

                    false -> {
                        binding.overlayView.isVisible = false
                        binding.etCommentInput.clearFocus()
                    }
                }
                previousHeightDiff = heightDiff
            }
        }

        bottomSheetLayout.rvComments.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                rv.parent.requestDisallowInterceptTouchEvent(true)
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })


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
        when (event) {
            PostDetailEvent.UploadCommentSuccess -> {
                binding.etCommentInput.text.clear()
                showSnackbar("댓글이 업로드되었습니다.")
                hideKeyboard()
            }

            else -> {}
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onBind(state: PostDetailUiState) {
        state.post?.let { post ->
            with(binding) {
                tvUsername.text = post.authorName
                tvTitle.text = post.title
                tvCreatedAt.text = post.timestamp.toFormattedString()
                tvContent.text = post.content
                ivUserProfile.load(post.authorProfileImageUrl)
            }
            post.imageUrls?.let { imageUrls ->
                binding.rvPostImage.isVisible = imageUrls.isEmpty().not()
                imageListAdapter.setImageUrls(imageUrls)
                imageListAdapter.notifyDataSetChanged()
            }
        }
        state.comments.let {
            val firstComment = it.firstOrNull()
            firstComment?.let { comment ->
                // TODO 댓글 작성자 이미지 불러오기
//                binding.ivCommentUserProfile.load()
                binding.tvComment.text = comment.content
            }
            Timber.tag("COMMENT").d("Count: ${it.size}")
            commentListAdapter.submitList(it)
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

        bottomSheetLayout.rvComments.layoutManager =
            LinearLayoutManager(this@PostDetailActivity, LinearLayoutManager.VERTICAL, false)
        bottomSheetLayout.rvComments.adapter = commentListAdapter

        bottomSheetLayout.rvComments.addItemDecoration(LinearVerticalItemDecoration(200))

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout.root)
        bottomSheetBehavior?.let {
            it.state = BottomSheetBehavior.STATE_HIDDEN
//            it.expandedOffset = 200
            it.isFitToContents = true
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
                // 여기에서 애니메이션 적용 후 활동 종료
                if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    // 바텀시트가 확장되거나 축소된 상태일 때, 바텀시트를 숨깁니다.
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    ActivityCompat.finishAfterTransition(this@PostDetailActivity)
                    overridePendingTransition(R.anim.anim_none, R.anim.slide_out)
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            it.clearFocus()
        } ?: inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun showSnackbar(message: String) {
        val rootView = binding.root
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

}