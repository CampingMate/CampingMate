package com.brandon.campingmate.domain.repository

import com.brandon.campingmate.domain.model.Post

interface SearchPostRepository {
    suspend fun searchPost(
        keyword: String
    ): Result<List<Post>>
}