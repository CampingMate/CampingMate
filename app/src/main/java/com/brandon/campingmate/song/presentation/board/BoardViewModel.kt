package com.brandon.campingmate.song.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.song.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.song.presentation.mapper.toPostListItem
import com.brandon.campingmate.song.utils.UiState
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

    private val _uiState = MutableStateFlow(BoardUiState.init())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    // 플로우 버퍼 공간 10개, 이후 가장 최근 발행된 이벤트 버리고 새로운 이벤트 추가
    private val _event = MutableSharedFlow<BoardEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<BoardEvent> = _event.asSharedFlow()


    init {
        Timber.d("Initializing BoardViewModel")
        loadPosts(20)   // 초기 20개 이후 10개씩 추가로 가져옴
    }

    fun loadPosts(pageSize: Int = 10) {
        viewModelScope.launch {
            // Event 수준에서 먼저 예외처리 되어있음
            if (_uiState.value.isPostsLoading) {
                return@launch
            }

            Timber.d("Emitting loading state")
            _uiState.update { it.copy(isPostsLoading = true) }

            runCatching {
                Timber.d("Attempting to load posts")
                Timber.tag("Kimchi").d("마지막 문서: ${_uiState.value.lastVisibleDoc?.id}")
                getPostUseCase(pageSize, _uiState.value.lastVisibleDoc)
            }.onSuccess { result ->
                // Success 처리
                result.data?.let { data ->
                    Timber.d("Posts loaded successfully")

                    when (_uiState.value.posts) {
                        UiState.Empty -> {
                            if (data.posts.isEmpty()) handleEvent(
                                BoardEvent.PostListEmpty
                            )
                        }

                        is UiState.Success -> {
                            if (data.posts.isEmpty()) handleEvent(
                                BoardEvent.ScrollEndEvent
                            )
                        }

                        else -> {}
                    }

                    val newPosts = if (_uiState.value.posts is UiState.Success) {
                        (_uiState.value.posts as UiState.Success).data + data.posts.toPostListItem()
                    } else {
                        data.posts.toPostListItem()
                    }
                    val lastVisibleDoc = data.lastVisibleDoc
                    _uiState.update {
                        it.copy(
                            posts = UiState.Success(newPosts),
                            lastVisibleDoc = lastVisibleDoc,
                            isPostsLoading = false
                        )
                    }
                } ?: run {
                    Timber.e("Failed to load posts, data is null")
                    _uiState.update { it.copy(posts = UiState.Error("Data is null"), isPostsLoading = false) }
                }
            }.onFailure { exception ->
                // Failure 처리
                Timber.e("Error loading posts: ${exception.message}")
                _uiState.update {
                    it.copy(
                        posts = UiState.Error(
                            exception.message ?: "An unknown error occurred"
                        ), isPostsLoading = false
                    )
                }
            }
        }
    }

    fun handleEvent(event: BoardEvent) {
        when (event) {
            BoardEvent.LoadMoreItems -> {
                /**
                 * event 버퍼를 10개 유지중이므로 event 가 쌓이면 loading 상태가 업데이트 되고난 후
                 * event 가 소진되며 리스트를 계속 불러올 수 있으니 로딩 중일 때는 event 를 발생시키지 않음
                 * tryEmit 는 MutableSharedFlow 의 정책(버퍼 사이즈, 초과시 처리 방침 등을 고려한다)
                 */
                if (uiState.value.isPostsLoading) return
                Timber.tag("Kimchi").d("데이터 요청 이벤트 발생!!")
                _event.tryEmit(BoardEvent.LoadMoreItems)
            }

            BoardEvent.ScrollEndEvent -> {
                Timber.d("문서의 끝 이벤트 발생!!")
                _event.tryEmit(BoardEvent.ScrollEndEvent)
            }

            is BoardEvent.OpenContent -> {
                Timber.d("문서 열기 이벤트 발생!!")
                _event.tryEmit(BoardEvent.OpenContent(event.postEntity))
            }

            BoardEvent.PostListEmpty -> {
                Timber.d("불러올 문서 없음!!")
                _event.tryEmit(BoardEvent.PostListEmpty)
            }

            BoardEvent.MoveToPostWrite -> {
                Timber.d("Post 쓰기 이동!!")
                _event.tryEmit(BoardEvent.MoveToPostWrite)
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
