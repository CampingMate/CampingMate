package com.brandon.campingmate.data.local.preferences

// TODO 현재 사용하지 않음, 싱글톤 사용중
interface PreferencesDataSource {
    fun getUserId(): String?
}