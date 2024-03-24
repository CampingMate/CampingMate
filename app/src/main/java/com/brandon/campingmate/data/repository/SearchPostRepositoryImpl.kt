package com.brandon.campingmate.data.repository

import com.brandon.campingmate.data.remote.api.OpenSearchDataSource
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.repository.SearchPostRepository

class SearchPostRepositoryImpl(
    private val openSearchDataSource: OpenSearchDataSource
) : SearchPostRepository {
    override suspend fun searchPost(keyword: String): Result<List<Post>> {
        return openSearchDataSource.searchPosts(keyword)
    }
}