package com.brandon.campingmate.presentation.postwrite

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PostWriteViewModel : ViewModel() {

    private val _event = MutableSharedFlow<PostWriteEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val event: SharedFlow<PostWriteEvent> = _event.asSharedFlow()


    fun handleEvent(event: PostWriteEvent) {
        when (event) {
            PostWriteEvent.UploadPost -> {
                _event.tryEmit(PostWriteEvent.UploadPost)
            }
        }
    }
}