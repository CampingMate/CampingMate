package com.brandon.campingmate.presentation.postwrite

import ImagePicker
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.method.ScrollingMovementMethod
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.R
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs.sharedPreferences
import com.brandon.campingmate.data.local.preferences.PreferencesDataSourceImpl
import com.brandon.campingmate.data.remote.firebasestorage.FireBaseStorageDataSourceImpl
import com.brandon.campingmate.data.remote.firestore.FirestoreDataSourceImpl
import com.brandon.campingmate.data.repository.PostRepositoryImpl
import com.brandon.campingmate.data.repository.UserRepositoryImpl
import com.brandon.campingmate.databinding.ActivityPostWriteBinding
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.domain.usecase.UploadPostUseCase
import com.brandon.campingmate.network.firestore.FirebaseService.fireStoreDB
import com.brandon.campingmate.network.firestore.FirebaseService.firebaseStorage
import com.brandon.campingmate.presentation.postdetail.PostDetailActivity
import com.brandon.campingmate.presentation.postdetail.PostDetailActivity.Companion.EXTRA_POST_ID
import com.brandon.campingmate.presentation.postwrite.adapter.PostWriteImageAdapter
import com.brandon.campingmate.utils.setDebouncedOnClickListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class PostWriteActivity : AppCompatActivity() {

    private val binding: ActivityPostWriteBinding by lazy { ActivityPostWriteBinding.inflate(layoutInflater) }

    private val viewModel: PostWriteViewModel by viewModels {
        PostWriteViewModelFactory(
            UploadPostUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(
                        fireStoreDB
                    ),
                    FireBaseStorageDataSourceImpl(
                        firebaseStorage
                    )
                )
            ),
            GetUserUserCase(
                UserRepositoryImpl(
                    PreferencesDataSourceImpl(
                        sharedPreferences
                    ),
                    FirestoreDataSourceImpl(
                        fireStoreDB
                    )
                )
            )

        )
    }

    private val imageListAdapter: PostWriteImageAdapter by lazy {
        PostWriteImageAdapter(onImageDeleteClicked = { uri ->
            viewModel.handleEvent(PostWriteEvent.ClickImageDelete(uri))
        })
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val editTexts
        get() = listOf(
            binding.etTitle, binding.etContent
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Timber.tag("LOGIN").d("onCreate")


        initActivityResultContracts()
        initView()
        initListener()
        initViewModel()
        setupOnBackPressedHandling()
    }

    private fun initActivityResultContracts() {
        // 권한 요청 결과를 처리하는 ActivityResultLauncher 초기화
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    showImagePickerBottomSheet()
                } else {
                    Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initViewModel() = with(viewModel) {

        lifecycleScope.launch {
            user.flowWithLifecycle(lifecycle).collect { user ->
                if (user != null) {
                    // 사용자가 로그인 한 경우
                    Timber.tag("LOGIN").d("회원입니다: $user")
                } else {
                    // 사용자가 로그인 하지 않은 경우
                    Timber.tag("LOGIN").d("비회원입니다")
                }
            }
        }

        lifecycleScope.launch {
            uiState.flowWithLifecycle(lifecycle).collectLatest { state ->
                onBind(state)
            }
        }

        lifecycleScope.launch {
            event.flowWithLifecycle(lifecycle).collectLatest { event ->
                onEvent(event)
            }
        }

        lifecycleScope.launch {
            buttonUiState.flowWithLifecycle(lifecycle).collectLatest { state ->
                Timber.tag("BUTTON").d("활성화여부: $state")
                binding.btnPostUpload.isEnabled = state
            }
        }

    }

    private fun onBind(state: PostWriteImageUiState) = with(state) {
        imageListAdapter.submitList(imageUris)
        binding.lottieUploading.isVisible = isUploading
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
                    this, R.anim.slide_up, R.anim.anim_none
                ).toBundle()
                setResult(Activity.RESULT_OK)
                startActivity(intent, options)
                ActivityCompat.finishAfterTransition(this)
            }

            is PostWriteEvent.OpenPhotoPicker -> {
                checkPermissionAndPickImage(event.uris)
            }

            else -> {}
        }
    }


    private fun initView() = with(binding) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 기본 타이틀 숨기기
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화

        rvPostImage.layoutManager =
            LinearLayoutManager(this@PostWriteActivity, LinearLayoutManager.HORIZONTAL, false)
        rvPostImage.adapter = imageListAdapter

        rvPostImage.itemAnimator = null

        etTitle.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(40))
        etTitle.movementMethod = ScrollingMovementMethod()

        etContent.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(200))
        etContent.movementMethod = ScrollingMovementMethod()
    }

    private fun initListener() = with(binding) {
        btnPostUpload.setDebouncedOnClickListener {
            hideKeyboard()
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()
            viewModel.handleEvent(
                PostWriteEvent.UploadPost(
                    title = title,
                    content = content,
                )
            )
        }
        btnAddImage.setOnClickListener {
            // 권한 얻기 + 갤러리에서 이미지 가져오기
            viewModel.handleEvent(PostWriteEvent.OpenPhotoPicker())
        }

        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            var previousHeightDiff: Int = 0
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val heightDiff = rootView.rootView.height - (rect.bottom - rect.top)
                val isKeyboardActive = heightDiff > 500
                if (heightDiff != previousHeightDiff) {
                    Timber.tag("VIEW").d("isKeyboardActive: $isKeyboardActive")
                    when (isKeyboardActive) {
                        true -> {
                            binding.rvImageContainer.animate().translationY(rvImageContainer.height.toFloat())
                                .setDuration(100)
                                .withEndAction {
                                    rvImageContainer.isVisible = false
                                }
                        }

                        false -> {
                            rvImageContainer.isVisible = true
                            rvImageContainer.animate().translationY(0f)
                                .setDuration(100)
                        }
                    }
                    previousHeightDiff = heightDiff
                }
            }
        })

        etTitle.addTextChangedListener {
            viewModel.onTitleChanged(it.toString())
        }

        etContent.addTextChangedListener {
            viewModel.onBodyChanged(it.toString())
        }
    }

    private fun showImagePickerBottomSheet(uris: List<Uri> = emptyList()) {
        val bottomSheet = ImagePicker(maxSelection = 5,
            preselectedImages = uris,
            gridCount = 3,
            gridSpacing = 8,
            includeEdge = false,
            cornerRadius = 16f,
            bottomSheetUsageDescription = null,
            onSelectionComplete = { selectedImages ->
                viewModel.handleEvent(PostWriteEvent.ImageSelected(selectedImages))
            })
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    private fun checkPermissionAndPickImage(uris: List<Uri>?) {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 있을 경우, 이미지 선택기 실행
                uris?.let { showImagePickerBottomSheet(it) }
            }

            else -> {
                // 권한 요청
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
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
            it.clearFocus()
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = 100
    }
}
