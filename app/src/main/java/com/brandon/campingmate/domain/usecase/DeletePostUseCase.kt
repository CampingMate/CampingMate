package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.repository.PostRepository

class DeletePostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        post: Post?
    ): Result<String> {
        if (post == null) throw IllegalArgumentException("이미 삭제된 게시물입니다.")
        return postRepository.deletePostById(post)
    }
}