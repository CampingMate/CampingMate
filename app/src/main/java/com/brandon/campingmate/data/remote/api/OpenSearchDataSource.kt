package com.brandon.campingmate.data.remote.api

import com.brandon.campingmate.domain.model.Post

interface OpenSearchDataSource {
    suspend fun searchPosts(keyword: String): Result<List<Post>>
}