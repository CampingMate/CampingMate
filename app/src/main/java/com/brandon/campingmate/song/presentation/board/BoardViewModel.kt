package com.brandon.campingmate.song.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.song.domain.usecase.GetPostsUseCase
import com.brandon.campingmate.song.presentation.mapper.toPostListItem
import com.brandon.campingmate.song.utils.Resource
import com.brandon.campingmate.song.utils.UiState
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class BoardViewModel(
    private val getPostUseCase: GetPostsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState.init())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    init {
        Timber.d("Initializing BoardViewModel")
        loadPosts()
    }

    fun loadPosts(pageSize: Int = 10, lastVisibleDoc: DocumentSnapshot? = null) {
        Timber.d("Loading posts with pageSize: $pageSize, lastVisibleDoc: ${lastVisibleDoc?.id}")

        viewModelScope.launch {
            // Emit loading state
            Timber.d("Emitting loading state")
            _uiState.value = _uiState.value.copy(posts = UiState.Loading)

            // Attempt to load posts
            Timber.d("Attempting to load posts")
            val result = getPostUseCase()

            // Update UI state based on result
            _uiState.value = when (result) {
                is Resource.Success -> {
                    result.data?.let { data ->
                        if (data.posts.isEmpty()) {
                            Timber.d("No posts loaded, data is empty")
                            _uiState.value.copy(posts = UiState.Empty)
                        } else {
                            Timber.d("Posts loaded successfully")
//                            Timber.v("data: $data")
                            val postListItem = data.posts.toPostListItem()
                            val lastVisibleDoc = data.lastVisibleDoc
                            _uiState.value.copy(
                                posts = UiState.Success(postListItem), lastVisibleDoc = lastVisibleDoc
                            )
                        }
                    } ?: run {
                        Timber.e("Failed to load posts, data is null")
                        _uiState.value.copy(posts = UiState.Error("Data is null"))
                    }
                }

                is Resource.Error -> {
                    Timber.e("Error loading posts: ${result.message}")
                    _uiState.value.copy(posts = UiState.Error(result.message ?: "An unknown error occurred"))
                }
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
