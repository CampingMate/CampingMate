package com.brandon.campingmate.presentation.postdetail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
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
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListAdapter
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem
import com.brandon.campingmate.presentation.postdetail.adapter.PostDetailImageListAdapter
import com.brandon.campingmate.utils.setDebouncedOnClickListener
import com.brandon.campingmate.utils.toFormattedString
import com.brandon.campingmate.utils.toPx
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

    private val commentListAdapter: PostCommentListAdapter by lazy {
        PostCommentListAdapter()
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
        btnSend.setDebouncedOnClickListener {
            val content = etCommentInput.text.toString()
            viewModel.handleEvent(PostDetailEvent.UploadComment(content))
        }

        btnRecentCommnet.setOnClickListener {
            bottomSheetBehavior?.let {
                it.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        btnClose.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Timber.d("BottomSheet State Changed: $newState")
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

        sheetRefresh.setOnRefreshListener {
            viewModel.handleEvent(PostDetailEvent.SwipeRefresh)
        }

        rvComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (!recyclerView.canScrollVertically(1) && lastVisibleItemPosition + 1 >= totalItemCount) {
                        viewModel.handleEvent(PostDetailEvent.InfiniteScroll)
                    }
                }
            }
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
        state.comments.let { comments ->
            val firstComment = comments.firstOrNull()
            firstComment?.let { comment ->
                if (comment is PostCommentListItem.PostCommentItem) {
                    binding.ivCommentUserProfile.load(comment.authorImageUrl)
                    binding.tvComment.text = comment.content
                }
            }

            binding.tvNoComment.isVisible = comments.isEmpty()

            if (state.isInfiniteLoadingComments) {
                commentListAdapter.submitList(comments + listOf(PostCommentListItem.Loading))
            } else {
                commentListAdapter.submitList(comments.filterIsInstance<PostCommentListItem.PostCommentItem>())
            }
        }

        if (!state.isSwipeLoadingComments) {
            binding.sheetRefresh.isRefreshing = false
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

        rvComments.layoutManager =
            LinearLayoutManager(this@PostDetailActivity, LinearLayoutManager.VERTICAL, false)
        rvComments.adapter = commentListAdapter


        bottomSheetBehavior = BottomSheetBehavior.from(binding.sheetContainer)

        bottomSheetBehavior?.let {
            it.state = BottomSheetBehavior.STATE_HIDDEN
            it.peekHeight = 650.toPx(this@PostDetailActivity)
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
                if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
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