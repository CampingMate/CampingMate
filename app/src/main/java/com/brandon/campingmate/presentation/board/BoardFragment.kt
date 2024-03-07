package com.brandon.campingmate.presentation.board

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.data.model.request.PostDTO
import com.brandon.campingmate.data.repository.PostRepositoryImpl
import com.brandon.campingmate.data.source.network.impl.PostRemoteDataSourceImpl
import com.brandon.campingmate.databinding.FragmentBoardBinding
import com.brandon.campingmate.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.network.firestore.FireStoreService.fireStoreDB
import com.brandon.campingmate.presentation.board.adapter.PostListAdapter
import com.brandon.campingmate.presentation.board.adapter.PostListItem
import com.brandon.campingmate.presentation.postdetail.PostDetailActivity
import com.brandon.campingmate.presentation.postwrite.PostWriteActivity
import com.brandon.campingmate.utils.UiState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BoardViewModel by viewModels {
        BoardViewModelFactory(
            GetPostsUseCase(PostRepositoryImpl(PostRemoteDataSourceImpl(fireStoreDB))),
        )
    }
    private val postListAdapter: PostListAdapter by lazy {
        PostListAdapter(onClickItem = { postEntity ->
            viewModel.handleEvent(BoardEvent.ViewPostDetail(postEntity))
        })
    }

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var postWriteResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("BoardFragment onCreate")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Timber.d("BoardFragment onCreateView")
        _binding = FragmentBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("BoardFragment onViewCreated")

//        upLoadFakePosts(35)
        initResultLauncher()
        initView()
        initListener()
        initViewModel()
    }

    private fun initResultLauncher() {
        postWriteResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.handleEvent(BoardEvent.NothingToFetch)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("BoardFragment onStart")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("BoardFragment onResume")
        viewModel.handleEvent(BoardEvent.RequestPostList(BoardViewModel.PostLoadTrigger.REFRESH))
    }

    override fun onPause() {
        super.onPause()
        Timber.d("BoardFragment onPause")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("BoardFragment onStop")
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("BoardFragment onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("BoardFragment onDestroy")
        _binding = null
    }

    private fun initListener() = with(binding) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Timber.d("Search submitted: $query")
                // Implement search logic here
                searchView.clearFocus() // SearchView로부터 포커스 제거
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText.isNullOrEmpty().let {
                    btnWrite.isVisible = it
                    val params = searchView.layoutParams as ConstraintLayout.LayoutParams
                    if (it) {
                        params.marginEnd = 0 // 마진 제거
                    } else {
                        val marginInDp = 16 // 마진 값 DP 단위로 설정
                        val marginInPx = (marginInDp * resources.displayMetrics.density).toInt() // DP를 픽셀로 변환
                        params.marginEnd = marginInPx // 마진 생성
                    }
                    searchView.layoutParams = params // 변경된 마진을 적용
                }
                return false
            }
        })

        rvPostList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 스크롤 내릴 때 양수, 올릴 때 음수
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                    Timber.d("dy: $dy, scrollVertical: ${recyclerView.canScrollVertically(1)}, lastVisibleItemPosition+1: ${lastVisibleItemPosition + 1},  totalItemCount: $totalItemCount} ")

//                    if (!recyclerView.canScrollVertically(1) && layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    if (!recyclerView.canScrollVertically(1) && lastVisibleItemPosition + 1 >= totalItemCount) {
                        // 무한 스크롤 이벤트 발생
                        viewModel.handleEvent(BoardEvent.RequestPostList(BoardViewModel.PostLoadTrigger.SCROLL))
                    }
                }
            }
        })

        btnWrite.setOnClickListener {
            viewModel.handleEvent(BoardEvent.NavigateToPostCreation)
        }

        rvPostList.addOnItemTouchListener(object : RecyclerView.OnScrollListener(),
            RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                hideKeyboard()
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        })


    }

    private fun initView() = with(binding) {
        // View initialization logic
        rvPostList.adapter = postListAdapter

        linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvPostList.layoutManager = linearLayoutManager
    }

    private fun initViewModel() = with(viewModel) {
        // collect : 새로운 데이터가 발행 되면 끝날 때 까지 기다림
        // collectLatest : 새로운 데이터가 발행되면 이전 처리르 취소하고 새로운 데이터 처리
        // 데이터의 일관성을 유지해줌
        // Flow 는 Lifecycle-Aware Components 가 아니다
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
    }

    private fun onEvent(event: BoardEvent) {
        when (event) {
            is BoardEvent.RequestPostList -> {}

            BoardEvent.NothingToFetchMore -> {
                Snackbar.make(binding.root, "문서의 끝에 도달했습니다", Snackbar.LENGTH_SHORT).show()
            }

            is BoardEvent.ViewPostDetail -> {
                Intent(requireContext(), PostDetailActivity::class.java).apply {
                    putExtra(PostDetailActivity.EXTRA_POST_ENTITY, event.postEntity)
                }.also {
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        requireContext(), R.anim.slide_in, R.anim.anim_none
                    ).toBundle()
                    startActivity(it, options)
                }
            }

            BoardEvent.NoPostsAvailable -> {
                // 로디 활성화로 변경
                Snackbar.make(binding.root, "가져올 문서가 없습니다", Snackbar.LENGTH_SHORT).show()
            }

            BoardEvent.NavigateToPostCreation -> {
                Intent(requireContext(), PostWriteActivity::class.java).also {
                    // 애니메이션 적용 + 결과 돌려받기
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        requireContext(), R.anim.slide_in, R.anim.anim_none
                    )
                    postWriteResultLauncher.launch(it, options)
                }
            }

            BoardEvent.NothingToFetch -> {

            }

            // TODO: ViewModel 에서 끝나는 이벤트와 그렇지 않은 이벤트를 구분할 수는 없을까?
            BoardEvent.ScrollPerformed -> {}

        }
    }

    private fun onBind(state: BoardUiState) = with(binding) {
        Timber.tag("All").d("현재 상태 isNeedRefresh :${state.isNeedScroll}")
        when (val postsState = state.posts) {
            is UiState.Success -> {
                // isPostsLoading 상태가 viewModel 에서 변경됨에 따라 로딩 아이템 추가/삭제
                // TODO 빈화면, 에러 화면 애니메이션 끄기
                val newPosts = if (state.isLoadingNext) {
                    postsState.data + listOf(PostListItem.Loading)
                } else {
                    postsState.data
                }
                postListAdapter.submitList(newPosts) {
                    if (state.isNeedScroll) {
                        scrollToTop()
                        viewModel.handleEvent(BoardEvent.ScrollPerformed)
                    } else {

                    }
                }
            }

            is UiState.Error -> {
                // TODO show 에러 애니메이션, 빈화면 애니메이션 끄기
            }

            is UiState.Empty -> {}
        }


    }

    companion object {
        @JvmStatic
        fun newInstance() = BoardFragment()
    }

    private fun hideKeyboard() {
        binding.searchView.clearFocus()
    }

    private fun scrollToTop() {
        binding.rvPostList.post {
            val layoutManager = linearLayoutManager
            layoutManager.scrollToPositionWithOffset(0, 0)
        }
    }

    // Todo: Remove
    private fun upLoadFakePosts(pageSize: Int) {
        val fakePosts = generateFakePosts(pageSize)
        Timber.d("Generated FakeData: $fakePosts")
        uploadPostsToFirestore(fakePosts)
    }
    // Todo: Remove
    private fun uploadPostsToFirestore(posts: List<PostDTO>) {
        val db = fireStoreDB.collection("posts")
        posts.forEach { post ->
            val postId = post.postId ?: db.document().id // id가 null이면 새 문서 ID 생성
            Timber.d("Pre-generated Id: ${db.document().id}")
            // Set id
            val newPostWithId = post.copy(postId = postId)
            db.document(postId).set(newPostWithId).addOnSuccessListener {
                Timber.d("Post successfully uploaded: $postId")
            }.addOnFailureListener { e ->
                Timber.e(e, "Error uploading post: $postId")
            }
        }
    }
    // Todo: Remove
    private fun generateFakePosts(dataSize: Int): List<PostDTO> {
        val fakePosts = mutableListOf<PostDTO>()
        for (i in 1..dataSize) {
            Thread.sleep(100)
            fakePosts.add(
                PostDTO(
                    postId = null,
                    authorName = "Author$i",
                    authorId = "AuthorId$i",
                    authorProfileImageUrl = "https://example.com/profile$i.jpg",
                    title = "Title $i",
                    content = "This is the content for post $i. Here we can have some more text just to make it look like a real post content.",
                    imageUrlList = listOf(
                        "https://campingagains3.s3.ap-northeast-2.amazonaws.com/medium_2021_10_17_11_38_57_f4f550931f.png",
                        "https://img1.daumcdn.net/thumb/R1280x0.fjpg/?fname=http://t1.daumcdn.net/brunch/service/user/wlQ/image/t9TZ03FH0sDqrDV8qQPj6VTfplg.jpeg"
                    ),
                    // 대신 실제 Firebase Timestamp 인스턴스 사용 필요
                    timestamp = Timestamp.now()
                )
            )
        }
        return fakePosts
    }


}
