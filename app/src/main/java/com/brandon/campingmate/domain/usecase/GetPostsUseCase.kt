package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.presentation.board.adapter.PostListItem
import com.brandon.campingmate.utils.mappers.toPostListItem

class GetPostsUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        pageSize: Int,
        shouldFetchFromFirst: Boolean,
    ): Result<List<PostListItem.PostItem>> {
        return postRepository.getPosts(pageSize, shouldFetchFromFirst)
            .mapCatching { modelList -> modelList.toPostListItem() }
    }
}