package com.brandon.campingmate.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * firestore 의 경우
 * kotlinx-coroutines-play-services 에서 제공하는 await를 사용할 수 있지만
 * 다음과 같은 이유로 Deferred 로 통합하여 사용한다
 * 1. 다양한 비동기 소스의 일관된 처리
 * 2. 코루틴 기반의 프로젝트 아키텍처
 */

fun Timestamp?.toFormattedString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return this?.toDate()?.let { dateFormat.format(it) } ?: ""
}