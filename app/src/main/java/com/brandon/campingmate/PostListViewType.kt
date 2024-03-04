package com.brandon.campingmate

enum class PostListViewType {
    POSTITEM,
    LOADING,
    UNKNOWN,
    ;

    companion object {
        fun from(ordinal: Int): PostListViewType = entries.find {
            it.ordinal == ordinal
        } ?: UNKNOWN
    }
}