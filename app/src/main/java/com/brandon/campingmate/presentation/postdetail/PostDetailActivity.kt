package com.brandon.campingmate.presentation.postdetail

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
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
import com.brandon.campingmate.databinding.BottomSheetPostdetailCommnetSideMenuBinding
import com.brandon.campingmate.domain.usecase.DeletePostCommentUseCase
import com.brandon.campingmate.domain.usecase.DeletePostUseCase
import com.brandon.campingmate.domain.usecase.GetPostByIdUseCase
import com.brandon.campingmate.domain.usecase.GetPostCommentsUseCase
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.domain.usecase.UploadPostCommentUseCase
import com.brandon.campingmate.network.firestore.FirebaseService
import com.brandon.campingmate.network.firestore.FirebaseService.fireStoreDB
import com.brandon.campingmate.presentation.common.SnackbarUtil
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListAdapter
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem
import com.brandon.campingmate.presentation.postdetail.adapter.PostImageListAdapter
import com.brandon.campingmate.utils.setDebouncedOnClickListener
import com.brandon.campingmate.utils.toFormattedString
import com.brandon.campingmate.utils.toPx
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }

    private val binding: ActivityPostDetailBinding by lazy { ActivityPostDetailBinding.inflate(layoutInflater) }

    private val imageListAdapter: PostImageListAdapter by lazy {
        PostImageListAdapter(onClick = { uri ->
            showImagePreviewDialog(uri)
        })
    }

    private val commentListAdapter: PostCommentListAdapter by lazy {
        PostCommentListAdapter(onClick = { commentItem ->
            viewModel.handleEvent(PostDetailEvent.ShowBottomSheetCommentMenu(commentItem))
        })
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
            ), DeletePostCommentUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB),
                    FireBaseStorageDataSourceImpl(FirebaseService.firebaseStorage)
                )
            ),
            DeletePostUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB),
                    FireBaseStorageDataSourceImpl(FirebaseService.firebaseStorage)
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
        Timber.tag("USER").d("postId: $postId")
        viewModel.getPostById(postId)

        initView()
        initListener()
        initViewModel()
        setupOnBackPressedHandling()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLoginStatus()
    }

    private fun initListener() = with(binding) {

        btnUploadComment.setDebouncedOnClickListener {
            val content = etCommentInput.text.toString()
            if (content.isBlank()) return@setDebouncedOnClickListener
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
                        binding.clCommentBarContainer.isVisible = false
                    }

                    else -> binding.clCommentBarContainer.isVisible = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                binding.clCommentBarContainer.isVisible = (slideOffset < -0.8).not()
                when (slideOffset) {
                    in 0f..1f -> binding.nsContainer.alpha = 0.5f
                    in -1f..0f -> binding.nsContainer.alpha = 1 - 0.5f * (slideOffset + 1)
                    else -> binding.nsContainer.alpha = 0.5f
                }
            }
        })

        val rootView = binding.root
        var previousHeightDiff = 0
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

        etCommentInput.addTextChangedListener {
            viewModel.checkValidComment(it.toString())
        }

        btnDeletePost.setDebouncedOnClickListener {
            showDeleteConfirmationDialog { viewModel.handleEvent(PostDetailEvent.DeletePost) }
        }
    }

    private fun initViewModel() = with(viewModel) {
        lifecycleScope.launch {
            viewModel.user.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).collectLatest { user ->
                setupCommentMenu(user != null)
            }
        }

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

        lifecycleScope.launch {
            viewModel.buttonUiState.flowWithLifecycle(lifecycle).collectLatest {
                binding.btnUploadComment.isEnabled = it
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

            is PostDetailEvent.MakeToast -> showToast(event.message)
            is PostDetailEvent.ShowBottomSheetMenu -> showBottomSheetCommentMenu(
                event.isOwner,
                event.postCommentId
            )

            PostDetailEvent.OwnershipVerified -> handleDeletePostButton()
            PostDetailEvent.DeletePost -> finishActivityWithToast("게시물이 삭제되었습니다", true)
            PostDetailEvent.Post404 -> finishActivityWithToast("이미 삭제된 게시물입니다.", false)

            else -> {}
        }
    }

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
                imageListAdapter.submitList(imageUrls)
            }
        }
        state.comments.let { comments ->
            binding.tvTitleCommentCount.text = comments.size.toString()
            val firstComment = comments.lastOrNull()
            firstComment?.let { comment ->
                binding.ivCommentUserProfile.load(comment.authorImageUrl)
                binding.tvComment.text = comment.content
            }

            binding.tvNoComment.isVisible = comments.isEmpty()

            if (state.isInfiniteLoadingComments) {
                commentListAdapter.submitList(comments + listOf(PostCommentListItem.Loading))
            } else {
                commentListAdapter.submitList(comments)
            }
        }

        if (!state.isSwipeLoadingComments) {
            binding.sheetRefresh.isRefreshing = false
        }

    }

    private fun initView() = with(binding) {
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

        val filterArray = arrayOf<InputFilter>(InputFilter.LengthFilter(150))
        etCommentInput.filters = filterArray
        etCommentInput.movementMethod = ScrollingMovementMethod()

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
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showBottomSheetCommentMenu(
        isOwner: Boolean,
        postCommentId: String?,
    ) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetPostdetailCommnetSideMenuBinding.inflate(layoutInflater)

        if (isOwner) {
            bottomSheetBinding.btnMenuOwner.isVisible = true
            bottomSheetBinding.btnMenuNotOwner.isVisible = false
        } else {
            bottomSheetBinding.btnMenuOwner.isVisible = false
            bottomSheetBinding.btnMenuNotOwner.isVisible = true
        }

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.btnMenuOwner.setOnClickListener {
            Timber.tag("DELETE").d("삭제 이벤트 발생")
            viewModel.handleEvent(PostDetailEvent.DeletePostComment(postCommentId))
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun setupCommentMenu(isUserLoggedIn: Boolean) {
        with(binding) {
            if (isUserLoggedIn) {
                etCommentInput.setOnClickListener(null)
                etCommentInput.isFocusable = true
                etCommentInput.isFocusableInTouchMode = true
                etCommentInput.hint = "텍스트를 입력하세요"
            } else {
                etCommentInput.setOnClickListener {
                    Timber.tag("SHOW").d("로그인 필요 알림")
                    SnackbarUtil.showSnackBar(binding.root)
                }
                etCommentInput.isFocusable = false
                etCommentInput.isFocusableInTouchMode = false
                etCommentInput.hint = "로그인 후 사용해주세요"
            }
        }
    }

    private fun showImagePreviewDialog(imageUrl: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.iv_image_preview)

        Glide.with(this).load(imageUrl).into(imageView)

        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(dialogView)
            dialogView.setOnClickListener { dismiss() } // 다이얼로그 바깥을 누르면 닫히도록 설정
        }
        dialog.show()
    }

    private fun handleDeletePostButton() {
        binding.btnDeletePost.isVisible = true
    }

    private fun finishActivityWithToast(message: String, isFinishing: Boolean) {
        viewModel.handleEvent(PostDetailEvent.MakeToast(message))
        if (isFinishing) {
            ActivityCompat.finishAfterTransition(this@PostDetailActivity)
            overridePendingTransition(R.anim.anim_none, R.anim.slide_out)
        }
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_post, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.btn_delete_yes)?.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_delete_no)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}