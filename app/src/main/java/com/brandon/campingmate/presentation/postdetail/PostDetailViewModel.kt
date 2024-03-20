package com.brandon.campingmate.presentation.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.usecase.GetPostByIdUseCase
import com.brandon.campingmate.domain.usecase.GetPostCommentsUseCase
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.domain.usecase.UploadPostCommentUseCase
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem
import com.brandon.campingmate.utils.Resource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class PostDetailViewModel(
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val uploadPostCommentUseCase: UploadPostCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val getUserUserCase: GetUserUserCase,
) : ViewModel() {


    private val _event = MutableSharedFlow<PostDetailEvent>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<PostDetailEvent> = _event.asSharedFlow()


    private val _uiState = MutableStateFlow(PostDetailUiState.init())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _buttonUiState = MutableStateFlow(false)
    val buttonUiState: Flow<Boolean> = _buttonUiState

    init {
        checkLoginStatus()
    }

    fun handleEvent(event: PostDetailEvent) {
        when (event) {
            is PostDetailEvent.UploadComment -> {
                Timber.d("댓글 업로드 이벤트 발생")
                uploadComment(event.comment)
            }

            PostDetailEvent.UploadCommentSuccess -> _event.tryEmit(PostDetailEvent.UploadCommentSuccess)
            PostDetailEvent.SwipeRefresh -> refreshComments()
            PostDetailEvent.InfiniteScroll -> infiniteScroll()
        }
    }

    private fun refreshComments() {
        if (_uiState.value.isSwipeLoadingComments) return
        _uiState.value = _uiState.value.copy(isSwipeLoadingComments = true)
        getComments()
    }

    private fun infiniteScroll() {
        if (_uiState.value.isInfiniteLoadingComments) return
        _uiState.value = _uiState.value.copy(isInfiniteLoadingComments = true)
        getComments()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            getUserUserCase().fold(
                onSuccess = { user -> _user.value = user },
                onFailure = { e -> Timber.d("로그인 중 에러 발생, 예외: $e") }
            )
        }
    }


    private fun getComments(pageSize: Int = 10) {
        viewModelScope.launch {
            getPostCommentsUseCase(
                postId = _uiState.value.post?.postId,
                pageSize = pageSize,
            ).fold(
                onSuccess = { newComments ->

                    _uiState.update { currentState ->
                        val uniqueOldComments = currentState.comments.filterNot { oldComment ->
                            newComments.any { newItem -> newItem.commentId == oldComment.commentId }
                        }
                        currentState.copy(
                            comments = uniqueOldComments + newComments,
                            isSwipeLoadingComments = false,
                            isInfiniteLoadingComments = false
                        )
                    }
                },
                onFailure = { e ->
                    when (e) {
                        is IllegalArgumentException -> Timber.e("현재 Post를 불러오지 못했습니다. $e")
                        else -> Timber.e("알 수 없는 에러")
                    }
                }
            )
        }
    }

    private fun addCommentToTop(postComment: PostCommentListItem.PostCommentItem) {
        _uiState.update { currentState -> currentState.copy(comments = listOf(postComment) + currentState.comments) }
    }

    private fun uploadComment(comment: String) {
        viewModelScope.launch {
            uploadPostCommentUseCase(
                postId = _uiState.value.post?.postId,
                user = _user.value,
                comment = comment
            ).fold(
                onSuccess = { item ->
                    handleEvent(PostDetailEvent.UploadCommentSuccess)
                    addCommentToTop(item)
                },
                onFailure = {
                    Timber.d("댓글 작성 실패")
                }
            )
        }
    }


    fun getPost(postId: String?) {
        if (postId == null) return
        viewModelScope.launch {
            when (val result = getPostByIdUseCase(postId)) {
                Resource.Empty -> {}
                // TODO 에러 시 알림
                is Resource.Error -> {}

                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(post = result.data)
                    getComments()
                }
            }
        }
    }

    fun checkValidComment(text: String) {
        _buttonUiState.update { isUploadButtonEnable(text) }
    }

    private fun isUploadButtonEnable(text: String): Boolean {
        return text.isNotBlank()
    }
}

class PostDetailViewModelFactory(
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val uploadPostCommentUseCase: UploadPostCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val getUserUserCase: GetUserUserCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating PostDetailViewModel instance")
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return PostDetailViewModel(
                getPostByIdUseCase,
                uploadPostCommentUseCase,
                getPostCommentsUseCase,
                getUserUserCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
