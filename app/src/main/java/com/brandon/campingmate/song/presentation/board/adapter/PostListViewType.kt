package com.brandon.campingmate.song.presentation.board.adapter

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