package com.brandon.campingmate.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class BoardViewModel(
    private val getPostUseCase: GetPostsUseCase,
    private val getUserUserCase: GetUserUserCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState.init())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val _event =
        MutableSharedFlow<BoardEvent>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val event: SharedFlow<BoardEvent> = _event.asSharedFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user


    init {
        Timber.d("Initializing BoardViewModel")
        _uiState.update { it.copy(isInitialLoading = true) }
        getPosts()
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            getUserUserCase().fold(
                onSuccess = { user -> _user.value = user },
                onFailure = { e -> Timber.d("로그인 중 에러 발생, 예외: $e") }
            )
        }
    }

    fun handleEvent(event: BoardEvent) {
        when (event) {
            is BoardEvent.OpenContent -> {
                _event.tryEmit(BoardEvent.OpenContent(event.post))
            }

            is BoardEvent.NavigateToPostWrite -> {
                _event.tryEmit(BoardEvent.NavigateToPostWrite)
            }

            BoardEvent.RefreshRequested -> {
                refreshPosts()
            }

            BoardEvent.LoadMoreRequested -> {
                loadMorePosts()
            }

            BoardEvent.RefreshPostsAndScrollToTopRequested -> {
                refreshPostsAndScrollToTop()
            }

            else -> {}
        }
    }

    private fun refreshPostsAndScrollToTop() {
        getPosts(shouldFetchFromFirst = true, shouldScrollToTop = true)
    }

    private fun loadMorePosts() {
        if (_uiState.value.isLoadingMore) return
        Timber.tag("LOAD").d("loadMorePosts")
        _uiState.update { it.copy(isLoadingMore = true) }
        getPosts()
    }

    private fun refreshPosts() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        getPosts(shouldFetchFromFirst = true)
    }

    private fun getPosts(
        pageSize: Int = 10,
        shouldFetchFromFirst: Boolean = false,
        shouldScrollToTop: Boolean = false
    ) {
        viewModelScope.launch {
            getPostUseCase(
                pageSize = pageSize,
                shouldFetchFromFirst = shouldFetchFromFirst
            ).fold(
                onSuccess = { newPosts ->
                    if (_uiState.value.isLoadingMore && newPosts.isEmpty()) handleEvent(BoardEvent.MakeToast("새로운 게시글이 더 이상 없어요."))
                    if (_uiState.value.isRefreshing) handleEvent(BoardEvent.MakeToast("새로고침"))
                    _uiState.update { currentState ->
                        Timber.tag("LOAD").d("newPosts: $newPosts")
                        currentState.copy(
                            posts = if (shouldFetchFromFirst) newPosts else currentState.posts + newPosts,
                            isRefreshing = false,
                            isLoadingMore = false,
                            isInitialLoading = false,
                            shouldScrollToTop = shouldScrollToTop,
                        )
                    }
                },
                onFailure = {}
            )
        }
    }

    fun clearScrollToTopFlag() {
        _uiState.update { it.copy(shouldScrollToTop = false) }
    }

}

class BoardViewModelFactory(
    private val getPostsUseCase: GetPostsUseCase,
    private val getUserUserCase: GetUserUserCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating BoardViewModel instance")
        if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return BoardViewModel(getPostsUseCase, getUserUserCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
