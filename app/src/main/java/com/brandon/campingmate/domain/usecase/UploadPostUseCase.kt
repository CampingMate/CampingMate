package com.brandon.campingmate.domain.usecase

import android.net.Uri
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.repository.PostRepository
import com.google.firebase.Timestamp

class UploadPostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        user: User?,
        imageUris: List<Uri>
    ): Result<String> {
        user ?: throw Exception("Can't find user")

        val post = Post(
            postId = null,
            authorId = "tempUserID",
            authorName = user.nickName,
            authorProfileImageUrl = user.profileImage,
            title = title,
            content = content,
            imageUrls = imageUris.map { it.toString() },
            timestamp = Timestamp.now()
        )

        return postRepository.uploadPostWithImages(post, imageUris)
    }
}