package com.brandon.campingmate.presentation.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.usecase.GetPostByIdUseCase
import com.brandon.campingmate.domain.usecase.UploadPostCommentUseCase
import com.brandon.campingmate.utils.Resource
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

class PostDetailViewModel(
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val uploadPostCommentUseCase: UploadPostCommentUseCase,
) : ViewModel() {


    private val _event = MutableSharedFlow<PostDetailEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<PostDetailEvent> = _event.asSharedFlow()


    private val _uiState = MutableStateFlow(PostDetailUiState.init())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun handleEvent(event: PostDetailEvent) {
        when (event) {
            is PostDetailEvent.UploadComment -> {
                Timber.d("게시물 업로드 이벤트 발생")
                uploadComment(event.comment)
            }
        }
    }

    private fun uploadComment(comment: String) {
        viewModelScope.launch {
            uploadPostCommentUseCase(
                postId = "OhjH7RyaFCL5NEAVdIa7",
                comment = comment
            ).fold(
                onSuccess = { commentId ->
                    // TODO 성공 이벤트 발생. 댓글 화면에 추가하기, input 창 비우기
                    Timber.d("댓글 작성 성공")
                },
                onFailure = {
                    // TODO 실패 이벤트 발생, input 창 두고, Toast 띄우기
                    Timber.d("댓글 작성 실패")
                }
            )
        }
    }


    fun loadData(postId: String?) {
        if (postId == null) return
        viewModelScope.launch {
            when (val result = getPostByIdUseCase(postId)) {
                Resource.Empty -> {}

                is Resource.Error -> {}

                is Resource.Success -> _uiState.update { it.copy(post = result.data) }
            }
        }
    }
}

class PostDetailViewModelFactory(
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val uploadPostCommentUseCase: UploadPostCommentUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating PostDetailViewModel instance")
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return PostDetailViewModel(
                getPostByIdUseCase,
                uploadPostCommentUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
