package com.brandon.campingmate.presentation.postwrite

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.usecase.UploadPostImagesUseCase
import com.brandon.campingmate.domain.usecase.UploadPostUseCase
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class PostWriteViewModel(
    private val uploadPostUseCase: UploadPostUseCase,
    private val uploadPostImagesUseCase: UploadPostImagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostWriteUiState.init())
    val uiState: StateFlow<PostWriteUiState> = _uiState.asStateFlow()

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

            is PostWriteEvent.UploadPostImages -> {
                Timber.d("게시물 이미지 업로드 이벤트 발생!")
                uploadPostImages(event.imageUris)
            }
        }
    }

    private fun uploadPostImages(imageUris: List<Uri>) {
        viewModelScope.launch {
            uploadPostImagesUseCase(imageUris = imageUris, onSuccess = {
                Timber.d("이미지 업로드 성공 목록: ${it}")
            }, onFailure = {
                Timber.d("이미지 업로드 실패")
            })

        }
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
                imageUrlList = listOf(
                    "https://gocamping.or.kr/upload/camp/3578/thumb/thumb_720_8754vKyTsiEUC3WTugx38cpD.jpg",
                    "https://skinnonews.com/wp-content/uploads/2016/08/%EC%BA%A0%ED%95%91%EC%9E%A5-%EC%86%8D-%EC%84%9D%EC%9C%A0_%EB%B3%B8%EB%AC%B8.png",
                    "https://www.sjfmc.or.kr/images/kor/sub04/sub04_10_01.jpg",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSbT2goJ5i9e0nm_fbYMz82A4Da00Fw7XjHvA&usqp=CAU",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRzI86Lb9SiPMK_MmTZ93s0hnaipYMBLDzBPw&usqp=CAU"
                ).shuffled(),
                timestamp = Timestamp.now()
            )

            runCatching {
                Timber.d("Attempting to upload post")
                uploadPostUseCase(
                    postEntity = postEntity,
                    imageUris = emptyList(),
                    onSuccess = { postId ->
                        Timber.d("Post successfully uploaded: $postId")
                        handleEvent(PostWriteEvent.PostUploadSuccess(postId))
                    }, onFailure = { exception ->
                        Timber.e(exception, "Error uploading post")
                    })
            }.onFailure { exception ->
                Timber.e("Error loading posts: ${exception.message}")
            }

        }
    }
}

class PostWriteViewModelFactory(
    private val uploadPostUseCase: UploadPostUseCase,
    private val uploadPostImagesUseCase: UploadPostImagesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating PostWriteViewModel instance")
        if (modelClass.isAssignableFrom(PostWriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return PostWriteViewModel(
                uploadPostUseCase, uploadPostImagesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}