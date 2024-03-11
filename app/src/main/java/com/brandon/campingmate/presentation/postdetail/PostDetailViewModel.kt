package com.brandon.campingmate.presentation.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.usecase.GetPostByIdUseCase
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
) : ViewModel() {


    private val _event = MutableSharedFlow<PostDetailEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<PostDetailEvent> = _event.asSharedFlow()


    private val _uiState = MutableStateFlow(PostDetailUiState.init())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()


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
    private val getPostByIdUseCase: GetPostByIdUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Timber.d("Creating PostDetailViewModel instance")
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return PostDetailViewModel(getPostByIdUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
