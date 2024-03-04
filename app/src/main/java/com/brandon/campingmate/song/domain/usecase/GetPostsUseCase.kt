package com.brandon.campingmate.song.domain.usecase

import com.brandon.campingmate.song.domain.model.PostsEntity
import com.brandon.campingmate.song.domain.repository.PostRepository
import com.brandon.campingmate.song.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

class GetPostsUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        pageSize: Int = 10,
        lastVisibleDoc: DocumentSnapshot? = null
    ): Resource<PostsEntity> {
        return runCatching {
            // 성공, 실패 반환 가능
            postRepository.getPosts(pageSize, lastVisibleDoc)
        }.getOrElse { exception ->
            Resource.Error("Failed to load posts: ${exception.localizedMessage}", null)
        }
    }

}