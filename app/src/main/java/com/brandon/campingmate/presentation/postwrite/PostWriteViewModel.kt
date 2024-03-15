package com.brandon.campingmate.presentation.postwrite

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.usecase.UploadPostUseCase
import com.google.firebase.Timestamp
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

class PostWriteViewModel(
    private val uploadPostUseCase: UploadPostUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostWriteImageUiState.init())
    val uiState: StateFlow<PostWriteImageUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<PostWriteEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<PostWriteEvent> = _event.asSharedFlow()


    fun handleEvent(event: PostWriteEvent) {
        when (event) {
            is PostWriteEvent.UploadPost -> {
                // 업로드 요청에 대해 event 를 발생시키지 않고 요청을 viewModel 에서 처리해 완료한다
                // _event.tryEmit(event)
                Timber.d("게시물 업로드 이벤트 발생!")
                uploadPost(event.title, event.content)
            }

            is PostWriteEvent.PostUploadSuccess -> {
                Timber.d("게시물이 성공적으로 업로드 되었습니다!")
                _event.tryEmit(PostWriteEvent.PostUploadSuccess(event.postId))
            }

            is PostWriteEvent.ImageSelected -> {
                Timber.d("이미지 리스트 가져오기 이벤트 발생!")
                updateSelectedImages(event.imageUris)
            }

            is PostWriteEvent.ClickImageDelete -> {
                Timber.d("이미지 삭제 이벤트 발생!")
                removeSelectedImage(event.imageUri)
            }

            is PostWriteEvent.OpenPhotoPicker -> {
                Timber.d("이미지 선택키 실행 이벤트 발생!")
                _event.tryEmit(PostWriteEvent.OpenPhotoPicker(_uiState.value.imageUris))
            }
        }
    }

    private fun removeSelectedImage(imageUri: Uri) {
        _uiState.update { currentState ->
            val updatedList = currentState.imageUris.filterNot { it == imageUri }
            currentState.copy(imageUris = updatedList)
        }
    }

    private fun updateSelectedImages(imageUris: List<Uri>) {
        _uiState.update { it.copy(imageUris = imageUris) }
    }

    private fun uploadPost(title: String, content: String) {
        viewModelScope.launch {
            // TODO 임시 유저 정보 이후 다른 곳에서 가져옴
            val authorName = "wiz" // 유저이름
            val authorId = "Kakao3375284946" // 유저 id (문서 key)
            val authorProfileImageUrl =
                "https://t1.kakaocdn.net/account_images/default_profile.jpeg.twg.thumb.R640x640"

            val postEntity = PostEntity(
                postId = null,  // Datasource 계층에서 주입
                authorName = authorName,
                authorId = authorId,
                authorProfileImageUrl = authorProfileImageUrl,
                title = title,
                content = content,
                imageUrls = null,
                timestamp = Timestamp.now()
            )
            // TODO 업로드 중 로딩 애니메이션 적용

            uploadPostUseCase(postEntity = postEntity, imageUris = uiState.value.imageUris).fold(
                onSuccess = { postId ->
                    handleEvent(PostWriteEvent.PostUploadSuccess(postId))
                },
                onFailure = { e -> Timber.tag("POST UPLOAD").d("게시물 업로드 실패: ${e.message}") }
            )
        }
    }
}

class PostWriteViewModelFactory(
    private val uploadPostUseCase: UploadPostUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating PostWriteViewModel instance")
        if (modelClass.isAssignableFrom(PostWriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return PostWriteViewModel(
                uploadPostUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}