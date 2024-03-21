package com.brandon.campingmate.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.presentation.board.BoardViewModel.RefreshTrigger.SCROLL
import com.brandon.campingmate.presentation.board.BoardViewModel.RefreshTrigger.SWIPE
import com.brandon.campingmate.presentation.board.BoardViewModel.RefreshTrigger.UPLOAD
import com.brandon.campingmate.utils.Resource
import com.brandon.campingmate.utils.mappers.toPostListItem
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

    enum class RefreshTrigger {
        SCROLL, SWIPE, UPLOAD
    }

    private val _uiState = MutableStateFlow(BoardUiState.init())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private val _event =
        MutableSharedFlow<BoardEvent>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val event: SharedFlow<BoardEvent> = _event.asSharedFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user


    init {
        Timber.d("Initializing BoardViewModel")
        loadPosts(SWIPE)
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
            is BoardEvent.LoadPosts -> {
                Timber.d("데이터 불러오기 이벤트")
                loadPosts(event.trigger)
            }

            is BoardEvent.OpenContent -> {
                Timber.d("문서 열기 이벤트")
                _event.tryEmit(BoardEvent.OpenContent(event.post))
            }

            is BoardEvent.MakeToast -> {
                Timber.d("토스트 만들기 이벤트")
                _event.tryEmit(BoardEvent.MakeToast(event.message))
            }

            is BoardEvent.NavigateToPostWrite -> {
                Timber.d("게시물 작성 이벤트")
                _event.tryEmit(BoardEvent.NavigateToPostWrite)
            }

            else -> {}
        }
    }

    private fun loadPosts(trigger: RefreshTrigger, pageSize: Int = 10) {
        if (uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (trigger == SWIPE) handleEvent(BoardEvent.MakeToast("새로고침"))

            val result = when (trigger) {
                SCROLL -> getPostUseCase(pageSize, _uiState.value.lastVisibleDoc)
                else -> getPostUseCase(pageSize, null) // SWIPE와 UPLOAD는 처음부터 목록을 로드
            }


            _uiState.update { currentState ->
                when (result) {
                    is Resource.Empty -> {
                        if (trigger == SCROLL) {
                            handleEvent(BoardEvent.MakeToast("더이상 볼 문서가 없습니다."))
                        }
                        currentState.copy(isLoading = false)
                    }
                    // TODO 에러 처리 로직 추가
                    is Resource.Error -> currentState.copy(isLoading = false)

                    is Resource.Success -> {
                        val newPosts = if (trigger == SCROLL) {
                            currentState.posts + result.data.posts.toPostListItem()
                        } else {
                            result.data.posts.toPostListItem()
                        }
                        currentState.copy(
                            posts = newPosts,
                            lastVisibleDoc = result.data.lastVisibleDoc,
                            isLoading = false,
                            shouldScrollToTop = trigger == UPLOAD
                        )
                    }
                }
            }
        }
    }

    fun resetScrollToTopFlag() {
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
