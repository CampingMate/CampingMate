package com.brandon.campingmate.data.remote.api

import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.utils.mappers.toPost
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber

class OpenSearchDataSourceImpl(private val openSearchService: OpenSearchService) :
    OpenSearchDataSource {
    override suspend fun searchPosts(keyword: String): Result<List<Post>> {
        return withContext(IO) {
            runCatching {
                val response = openSearchService.searchPosts(keyword)
                Timber.i("OpenSearchResult: $response")
                val postHits = response.hits?.postHits
                val posts = postHits?.mapNotNull { it.post } ?: emptyList()
                Timber.d("OpenSearch posts: $posts")
                posts.map { it.toPost() }
            }.onFailure { e ->
                Timber.e(e, "Error occurred while fetching posts")
            }
        }
    }
}