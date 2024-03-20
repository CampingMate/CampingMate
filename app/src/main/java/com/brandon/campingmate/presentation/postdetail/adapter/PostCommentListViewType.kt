package com.brandon.campingmate.presentation.postdetail.adapter

enum class PostCommentListViewType {
    ITEM,
    LOADING,
    UNKNOWN,
    ;

    companion object {
        fun from(ordinal: Int): PostCommentListViewType = entries.find {
            it.ordinal == ordinal
        } ?: UNKNOWN
    }
}