package com.brandon.campingmate.presentation.login

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.databinding.ActivityLoginBinding
import com.brandon.campingmate.utils.profileImgUpload
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
                    }

                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                    }

                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT).show()
                    }

                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
                    }

                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
                    }

                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "설정이 올바르지 않음(android key hash)", Toast.LENGTH_SHORT).show()
                    }

                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "서버 내부 에러", Toast.LENGTH_SHORT).show()
                    }

                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
                    }

                    else -> { // Unknown
                        Toast.makeText(this, "기타 에러", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (token != null) {
                Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                val db = Firebase.firestore
                UserApiClient.instance.me { user, _ ->
                    profileImgUpload(Uri.parse(user?.kakaoAccount?.profile?.profileImageUrl), "Kakao${user?.id}")

                    val documentRef = db.collection("users").document("Kakao${user?.id}")
                    val userModel = hashMapOf(
                        "userId" to "Kakao${user?.id}",
                        "nickName" to "${user?.kakaoAccount?.profile?.nickname}",
                        "profileImage" to null,
                        "userEmail" to "${user?.kakaoAccount?.email}",
                        "bookmarked" to null
                    )
                    documentRef.get().addOnSuccessListener {
                        if (!it.exists()) {
                            documentRef.set(userModel)
                            Firebase.storage.getReference("profileImage").child("Kakao${user?.id}").downloadUrl.addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val profileImgURI = hashMapOf<String, Any>(
                                        "profileImage" to it.result.toString()
                                    )
                                    documentRef.update(profileImgURI)
                                }
                            }
                        }
                    }

                    //SharedPreferences 암호화 후 저장
                    val masterKeyAlias = MasterKey
                        .Builder(applicationContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    val pref = EncryptedSharedPreferences.create(
                        this, //Context
                        "userID",   //file name
                        masterKeyAlias,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,  //key 암호화
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM     //value 암호화
                    )
                    EncryptedPrefs.saveMyId("Kakao${user?.id}")
                    pref.edit().putString("myID", "Kakao${user?.id}").apply()
                    finish()
                }
            }
        }
        val kakao_login_button = binding.cvKakao
        //로그인하기 버튼 클릭시
        kakao_login_button.setOnClickListener {
            //카카오톡이 설치되어 있으면, 카카오톡로그인
            if (LoginClient.instance.isKakaoTalkLoginAvailable(this)) {
                LoginClient.instance.loginWithKakaoTalk(this, callback = callback)
                //설치되어 있지 않으면, 카카오 계정 로그인(웹)
            } else {
                LoginClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        binding.tvNoLogin.setOnClickListener {
            finish()
        }
    }
}