package com.brandon.campingmate.presentation.postwrite

import android.net.Uri

sealed class PostWriteEvent {

    data class PostUploadSuccess(
        val postId: String
    ) : PostWriteEvent()

    data class UploadPost(
        val title: String,
        val content: String,
    ) : PostWriteEvent()

    data class ImageSelected(
        val imageUris: List<Uri>
    ) : PostWriteEvent()

    data class ClickImageDelete(
        val imageUri: Uri
    ) : PostWriteEvent()

    data class OpenPhotoPicker(
        val uris: List<Uri>? = null
    ) : PostWriteEvent()

}