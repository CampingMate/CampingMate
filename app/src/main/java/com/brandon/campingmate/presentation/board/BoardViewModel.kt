package com.brandon.campingmate.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.presentation.mapper.toPostListItem
import com.brandon.campingmate.utils.UiState
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
) : ViewModel() {

    enum class PostLoadTrigger {
        SCROLL, REFRESH
    }

    private val _uiState = MutableStateFlow(BoardUiState.init())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    // 플로우 버퍼 공간 10개, 이후 가장 최근 발행된 이벤트 버리고 새로운 이벤트 추가
    private val _event = MutableSharedFlow<BoardEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<BoardEvent> = _event.asSharedFlow()


    init {
        Timber.d("Initializing BoardViewModel")
        loadPosts(PostLoadTrigger.REFRESH, pageSize = 20)   // 초기 20개 이후 10개씩 추가로 가져옴
    }

    fun handleEvent(event: BoardEvent) {
        when (event) {
            is BoardEvent.RequestPostList -> {
                /**
                 * event 버퍼를 10개 유지중이므로 event 가 쌓이면 loading 상태가 업데이트 되고난 후
                 * event 가 소진되며 리스트를 계속 불러올 수 있으니 로딩 중일 때는 event 를 발생시키지 않음
                 * tryEmit 는 MutableSharedFlow 의 정책(버퍼 사이즈, 초과시 처리 방침 등을 고려한다)
                 */
                if (uiState.value.isLoadingNext && uiState.value.isRefreshing) return
                loadPosts(trigger = event.trigger)
                when (val trigger = event.trigger) {
                    PostLoadTrigger.SCROLL -> {
                        Timber.tag("Kimchi").d("데이터 요청 이벤트 발생!!(스크롤)")
                    }

                    PostLoadTrigger.REFRESH -> {
                        Timber.tag("Kimchi").d("데이터 요청 이벤트 발생!!(새로고침)")
                    }
                }
            }

            BoardEvent.NothingToFetchMore -> {
                Timber.d("문서의 끝 이벤트 발생!!")
                _event.tryEmit(BoardEvent.NothingToFetchMore)
            }

            is BoardEvent.ViewPostDetail -> {
                Timber.d("문서 열기 이벤트 발생!!")
                _event.tryEmit(BoardEvent.ViewPostDetail(event.postEntity))
            }

            BoardEvent.NoPostsAvailable -> {
                Timber.d("불러올 문서 없음 이벤트 발생!!")
                _event.tryEmit(BoardEvent.NoPostsAvailable)
            }

            BoardEvent.NavigateToPostCreation -> {
                Timber.d("Post 쓰기 이동 이벤트 발생!!")
                _event.tryEmit(BoardEvent.NavigateToPostCreation)
            }

            BoardEvent.NothingToFetch -> {
                Timber.d("Post 리스트 새로고침 이벤트 발생!!")
                loadPosts(PostLoadTrigger.REFRESH)
            }

            BoardEvent.ScrollPerformed -> {
                _uiState.update { it.copy(isNeedScroll = false) }
            }
        }
    }

    private fun loadPosts(trigger: PostLoadTrigger, pageSize: Int = 10) {
        viewModelScope.launch {
            Timber.d(" loadPosts - $trigger 이벤트 발생!!")

            // Event 수준에서 먼저 예외처리 되어있음
            when (trigger) {
                PostLoadTrigger.SCROLL -> if (_uiState.value.isLoadingNext) return@launch
                PostLoadTrigger.REFRESH -> if (_uiState.value.isRefreshing) return@launch
            }

            Timber.d("Emitting loading state")
            updateLoadingState(true, trigger)

            runCatching {
                Timber.d("Attempting to load posts")
                when (trigger) {
                    PostLoadTrigger.SCROLL -> {
                        getPostUseCase(pageSize, _uiState.value.lastVisibleDoc)
                    }

                    PostLoadTrigger.REFRESH -> {
                        _uiState.update { it.copy(posts = UiState.Empty) }
                        getPostUseCase(pageSize, null)
                    }
                }
            }.onSuccess { result ->
                // Success 처리
                result.data?.let { data ->
                    Timber.d("Posts loaded successfully")
                    Timber.tag("Song").d("post: ${data.posts.map { it.title }}")

                    when (_uiState.value.posts) {
                        UiState.Empty -> {
                            if (data.posts.isEmpty()) handleEvent(BoardEvent.NoPostsAvailable)
                        }

                        is UiState.Success -> {
                            when (trigger) {
                                PostLoadTrigger.SCROLL -> {
                                    if (data.posts.isEmpty()) handleEvent(
                                        BoardEvent.NothingToFetchMore
                                    )
                                }

                                PostLoadTrigger.REFRESH -> {
                                    if (data.posts.isEmpty()) handleEvent(
                                        BoardEvent.NothingToFetch
                                    )
                                }
                            }

                        }

                        else -> {}
                    }

                    val newPosts = if (_uiState.value.posts is UiState.Success) {
                        // 역전 옵션 설정
                        (_uiState.value.posts as UiState.Success).data + data.posts.toPostListItem()
                    } else {
                        data.posts.toPostListItem()
                    }
                    val lastVisibleDoc = data.lastVisibleDoc
                    when (trigger) {
                        PostLoadTrigger.SCROLL -> {
                            _uiState.update {
                                it.copy(
                                    posts = UiState.Success(newPosts),
                                    lastVisibleDoc = lastVisibleDoc,
                                )
                            }
                        }

                        PostLoadTrigger.REFRESH -> {
                            _uiState.update {
                                it.copy(
                                    posts = UiState.Success(newPosts),
                                    lastVisibleDoc = lastVisibleDoc,
                                    isNeedScroll = true,
                                )
                            }
                        }
                    }

                } ?: run {
                    Timber.e("Failed to load posts, data is null")
                    _uiState.update { it.copy(posts = UiState.Error("Data is null"), isLoadingNext = false) }
                }
            }.onFailure { exception ->
                Timber.e("Error loading posts: ${exception.message}")
                _uiState.update {
                    it.copy(
                        posts = UiState.Error(
                            exception.message ?: "An unknown error occurred"
                        ), isLoadingNext = false, isRefreshing = false
                    )
                }
            }.also {
                updateLoadingState(false, trigger)
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean, trigger: PostLoadTrigger) {
        _uiState.update { currentState ->
            when (trigger) {
                PostLoadTrigger.SCROLL -> currentState.copy(isLoadingNext = isLoading)
                PostLoadTrigger.REFRESH -> currentState.copy(isRefreshing = isLoading)
            }
        }
    }
}

class BoardViewModelFactory(
    private val getPostsUseCase: GetPostsUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating BoardViewModel instance")
        if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return BoardViewModel(getPostsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
