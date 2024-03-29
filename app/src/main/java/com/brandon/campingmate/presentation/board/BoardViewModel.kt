package com.brandon.campingmate.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.domain.usecase.SearchPostUseCase
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
    private val searchPostUseCase: SearchPostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState.init())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val _event =
        MutableSharedFlow<BoardEvent>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val event: SharedFlow<BoardEvent> = _event.asSharedFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user


    init {
        Timber.tag("BOARD").d("ViewModel is being created")
        _uiState.update { it.copy(isInitialLoading = true) }
        getPosts()
        checkLoginStatus()
    }

    fun searchPost(keyword: String?) {
        if (keyword.isNullOrBlank()) return
        _uiState.update { it.copy(isSearchLoading = true) }
        viewModelScope.launch {
            searchPostUseCase(keyword).fold(onSuccess = { newPostListItems ->
                Timber.tag("SEARCH").d("성공: $newPostListItems")
                if (newPostListItems.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            posts = newPostListItems,
                            isNothingToShow = true,
                            isSearchLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(posts = newPostListItems, isSearchLoading = false) }
                }
            }, onFailure = { e -> Timber.d("검색 중 에러 발생: $e") })
        }
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            getUserUserCase().fold(onSuccess = { user -> _user.value = user },
                onFailure = { e -> Timber.d("로그인 중 에러 발생: $e") })
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

            is BoardEvent.MakeToast -> _event.tryEmit(BoardEvent.MakeToast(event.message))


            BoardEvent.RefreshPostsRequested -> {
                refreshPosts()
            }

            BoardEvent.LoadMorePostsRequested -> {
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
        _uiState.update { it.copy(isLoadingMore = true) }
        getPosts()
    }

    private fun refreshPosts() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        getPosts(shouldFetchFromFirst = true)
        handleEvent(BoardEvent.MakeToast("새로고침"))
    }

    private fun getPosts(
        pageSize: Int = 10, shouldFetchFromFirst: Boolean = false, shouldScrollToTop: Boolean = false
    ) {
        viewModelScope.launch {
            getPostUseCase(
                pageSize = pageSize, shouldFetchFromFirst = shouldFetchFromFirst
            ).fold(onSuccess = { newPosts ->
                if (_uiState.value.isLoadingMore && newPosts.isEmpty()) handleEvent(BoardEvent.MakeToast("새로운 게시글이 더 이상 없어요."))
                _uiState.update { currentState ->
                    currentState.copy(
                        posts = if (shouldFetchFromFirst) newPosts else currentState.posts + newPosts,
                        isRefreshing = false,
                        isLoadingMore = false,
                        isInitialLoading = false,
                        isNothingToShow = false,
                        isSearchLoading = false,
                        shouldScrollToTop = shouldScrollToTop,
                    )
                }
            }, onFailure = {})
        }
    }

    fun clearScrollToTopFlag() {
        _uiState.update { it.copy(shouldScrollToTop = false) }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.tag("BOARD").d("ViewModel is being cleared")
    }

}

class BoardViewModelFactory(
    private val getPostsUseCase: GetPostsUseCase,
    private val getUserUserCase: GetUserUserCase,
    private val searchPostUseCase: SearchPostUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating BoardViewModel instance")
        if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return BoardViewModel(
                getPostsUseCase, getUserUserCase, searchPostUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
