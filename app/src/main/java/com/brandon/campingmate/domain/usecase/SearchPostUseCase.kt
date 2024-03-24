package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.repository.SearchPostRepository
import com.brandon.campingmate.presentation.board.adapter.PostListItem
import com.brandon.campingmate.utils.mappers.toPostListItem

class SearchPostUseCase(private val searchPostRepository: SearchPostRepository) {
    suspend operator fun invoke(keyword: String): Result<List<PostListItem.PostItem>> {
        return searchPostRepository.searchPost(keyword).map { posts -> posts.toPostListItem() }
    }
}