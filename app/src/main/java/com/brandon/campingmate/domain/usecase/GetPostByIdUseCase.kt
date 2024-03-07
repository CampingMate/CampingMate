package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.utils.Resource

class GetPostByIdUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        postId: String
    ): Resource<PostEntity> {
        return postRepository.getPostById(postId)
    }
}