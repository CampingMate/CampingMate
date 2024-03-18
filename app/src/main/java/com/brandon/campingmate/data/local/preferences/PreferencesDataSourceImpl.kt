package com.brandon.campingmate.data.local.preferences

import android.content.SharedPreferences

// TODO 현재 사용하지 않음, 싱글톤 사용중
class PreferencesDataSourceImpl(private val sharedPreferences: SharedPreferences) :
    PreferencesDataSource {
    override fun getUserId(): String? {
        return sharedPreferences.getString("myID", null)
    }
}