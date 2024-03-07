package com.brandon.campingmate.presentation.board

import com.brandon.campingmate.domain.model.PostEntity

sealed class BoardEvent {
    object NothingToFetchMore : BoardEvent() // 스크롤이 게시물 리스트의 끝에 도달했음을 나타냄
    object NoPostsAvailable : BoardEvent() // 게시물 리스트가 비어있음
    object NavigateToPostCreation : BoardEvent() // 게시물 작성 화면으로 이동
    object ScrollPerformed : BoardEvent()

    data class ViewPostDetail(
        val postEntity: PostEntity, // 게시물 엔티티에 대한 참조
    ) : BoardEvent() // 게시물의 상세 내용 보기

    object NothingToFetch : BoardEvent() // 게시물 리스트 새로고침
    data class RequestPostList(
        val trigger: BoardViewModel.PostLoadTrigger // 게시물 더 불러오기 트리거
    ) : BoardEvent() // 추가 게시물 불러오기 요청

}