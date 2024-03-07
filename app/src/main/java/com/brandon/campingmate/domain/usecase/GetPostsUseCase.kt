package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.PostsEntity
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

class GetPostsUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        pageSize: Int = 10,
        lastVisibleDoc: DocumentSnapshot? = null
    ): Resource<PostsEntity> {
        return postRepository.getPosts(pageSize, lastVisibleDoc)
    }

}