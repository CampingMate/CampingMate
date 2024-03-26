package com.brandon.campingmate.presentation.board

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.data.local.preferences.PreferencesDataSourceImpl
import com.brandon.campingmate.data.remote.api.OpenSearchDataSourceImpl
import com.brandon.campingmate.data.remote.firebasestorage.FireBaseStorageDataSourceImpl
import com.brandon.campingmate.data.remote.firestore.FirestoreDataSourceImpl
import com.brandon.campingmate.data.repository.PostRepositoryImpl
import com.brandon.campingmate.data.repository.SearchPostRepositoryImpl
import com.brandon.campingmate.data.repository.UserRepositoryImpl
import com.brandon.campingmate.databinding.FragmentBoardBinding
import com.brandon.campingmate.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.domain.usecase.SearchPostUseCase
import com.brandon.campingmate.network.firestore.FirebaseService.fireStoreDB
import com.brandon.campingmate.network.firestore.FirebaseService.firebaseStorage
import com.brandon.campingmate.network.retrofit.NetWorkClient.openSearchService
import com.brandon.campingmate.presentation.board.adapter.PostListAdapter
import com.brandon.campingmate.presentation.board.adapter.PostListItem
import com.brandon.campingmate.presentation.common.SnackbarUtil
import com.brandon.campingmate.presentation.postdetail.PostDetailActivity
import com.brandon.campingmate.presentation.postwrite.PostWriteActivity
import com.brandon.campingmate.utils.clearText
import com.brandon.campingmate.utils.setDebouncedOnClickListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BoardViewModel by viewModels {
        BoardViewModelFactory(
            GetPostsUseCase(
                PostRepositoryImpl(
                    FirestoreDataSourceImpl(fireStoreDB), FireBaseStorageDataSourceImpl(firebaseStorage)
                )
            ),
            GetUserUserCase(
                UserRepositoryImpl(
                    PreferencesDataSourceImpl(
                        EncryptedPrefs.sharedPreferences
                    ), FirestoreDataSourceImpl(
                        fireStoreDB
                    )
                )
            ),
            SearchPostUseCase(
                SearchPostRepositoryImpl(
                    OpenSearchDataSourceImpl(openSearchService)
                )
            )
        )
    }
    private val postListAdapter: PostListAdapter by lazy {
        PostListAdapter(onClickItem = { postEntity ->
            viewModel.handleEvent(BoardEvent.OpenContent(postEntity))
        })
    }

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var postWriteResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initResultLauncher()
        initView()
        initListener()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLoginStatus()
        binding.shimmerView.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerView.stopShimmer()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initListener() = with(binding) {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchPost(query)
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        rvPostList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    if (!recyclerView.canScrollVertically(1) && lastVisibleItemPosition + 1 >= totalItemCount) {
                        viewModel.handleEvent(BoardEvent.LoadMorePostsRequested)
                    }
                }
            }
        })

        fab.setOnClickListener {
            viewModel.handleEvent(BoardEvent.NavigateToPostWrite)
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

        swipeRefresh.setOnRefreshListener {
            viewModel.handleEvent(BoardEvent.RefreshPostsRequested)
            binding.swipeRefresh.isRefreshing = false
            searchView.clearText()
        }

        btnLottieCamp.setDebouncedOnClickListener {
            viewModel.handleEvent(BoardEvent.RefreshPostsRequested)
            searchView.clearText()
        }
    }

    private fun initView() = with(binding) {
        rvPostList.adapter = postListAdapter
        rvPostList.layoutManager = LinearLayoutManager(requireContext()).also {
            linearLayoutManager = it
        }
        rvPostList.setHasFixedSize(true)
    }

    private fun initViewModel() = with(viewModel) {
        lifecycleScope.launch {
            user.flowWithLifecycle(lifecycle).collectLatest { user ->
                updateUiForLogin(user != null)
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
    }

    private fun onEvent(event: BoardEvent) {
        when (event) {
            is BoardEvent.OpenContent -> {
                Intent(requireContext(), PostDetailActivity::class.java).apply {
                    putExtra(PostDetailActivity.EXTRA_POST_ID, event.post.postId)
                }.also {
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        requireContext(), R.anim.slide_in, R.anim.anim_none
                    ).toBundle()
                    startActivity(it, options)
                }
            }

            BoardEvent.NavigateToPostWrite -> {
                Intent(requireContext(), PostWriteActivity::class.java).also {
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        requireContext(), R.anim.slide_in, R.anim.anim_none
                    )
                    postWriteResultLauncher.launch(it, options)
                }
            }

            is BoardEvent.MakeToast -> {
                showToast(event.message)
            }

            else -> {}
        }
    }

    private fun onBind(state: BoardUiState) = with(state) {
        binding.lottieNothingToShow.isVisible = isNothingToShow
        binding.lottieSearching.isVisible = isSearchLoading
        postListAdapter.submitList(posts + if (isLoadingMore) listOf(PostListItem.Loading) else emptyList()) {
            if (shouldScrollToTop) {
                binding.rvPostList.smoothScrollToPosition(0)
                viewModel.clearScrollToTopFlag()
            }
        }

        binding.shimmerView.isVisible = isInitialLoading
    }

    private fun initResultLauncher() {
        postWriteResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.handleEvent(BoardEvent.RefreshPostsAndScrollToTopRequested)
                }
            }
    }

    private fun hideKeyboard() {
        binding.searchView.clearFocus()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUiForLogin(isLogin: Boolean) = with(binding) {
        if (isLogin) {
            fab.setOnClickListener {
                viewModel.handleEvent(BoardEvent.NavigateToPostWrite)
            }
        } else {
            fab.setOnClickListener {
                SnackbarUtil.showSnackBar(binding.root)
            }
        }
    }

}
