package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.utils.Resource

class GetPostByIdUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        postId: String
    ): Resource<Post> {
        return postRepository.getPostById(postId)
    }
}