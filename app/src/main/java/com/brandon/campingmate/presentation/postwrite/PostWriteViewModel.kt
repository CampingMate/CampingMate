package com.brandon.campingmate.presentation.postwrite

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.usecase.GetUserUserCase
import com.brandon.campingmate.domain.usecase.UploadPostUseCase
import com.brandon.campingmate.presentation.login.LoginActivity.Constants.AES_KEY
import com.brandon.campingmate.presentation.login.LoginActivity.Constants.decrypt
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

class PostWriteViewModel(
    private val uploadPostUseCase: UploadPostUseCase,
    private val getUserUserCase: GetUserUserCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostWriteImageUiState.init())
    val uiState: StateFlow<PostWriteImageUiState> = _uiState.asStateFlow()

    private val _buttonUiState = MutableStateFlow(false)
    val buttonUiState: Flow<Boolean> = _buttonUiState

    private val _event = MutableSharedFlow<PostWriteEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<PostWriteEvent> = _event.asSharedFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            getUserUserCase().fold(
                onSuccess = { user -> _user.value = user },
                onFailure = { e -> Timber.d("로그인 중 에러 발생, 예외: $e") }
            )
        }
    }


    fun handleEvent(event: PostWriteEvent) {
        when (event) {
            is PostWriteEvent.UploadPost -> {
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
            _uiState.update { it.copy(isUploading = true) }
            uploadPostUseCase(
                title = title,
                content = content,
                user = _user.value,
                imageUris = uiState.value.imageUris
            ).fold(
                onSuccess = { postId ->
                    handleEvent(PostWriteEvent.PostUploadSuccess(postId))
                    _uiState.update { it.copy(isUploading = false) }
                },
                onFailure = { e ->
                    Timber.tag("POST UPLOAD").d("게시물 업로드 실패: ${e.message}")
                    _uiState.update { it.copy(isUploading = false) }
                })
        }
    }

    fun onTitleChanged(text: String) {
        _uiState.update { it.copy(title = text) }
        updateSubmitButtonState()
    }


    fun onBodyChanged(text: String) {
        _uiState.update { it.copy(content = text) }
        updateSubmitButtonState()
    }

    private fun updateSubmitButtonState() {
        _buttonUiState.update {
            _uiState.value.title.isNotBlank() && _uiState.value.content.isNotBlank()
        }
    }
}

class PostWriteViewModelFactory(
    private val uploadPostUseCase: UploadPostUseCase,
    private val getUserUserCase: GetUserUserCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating PostWriteViewModel instance")
        if (modelClass.isAssignableFrom(PostWriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return PostWriteViewModel(
                uploadPostUseCase,
                getUserUserCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}