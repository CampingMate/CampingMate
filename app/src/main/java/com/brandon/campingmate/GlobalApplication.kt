package com.brandon.campingmate

import android.app.Application
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))

        EncryptedPrefs.initialize(this)  // application context 전달
    }
}