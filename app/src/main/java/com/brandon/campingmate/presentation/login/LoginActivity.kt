package com.brandon.campingmate.presentation.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.brandon.campingmate.R
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.databinding.ActivityLoginBinding
import com.brandon.campingmate.utils.profileImgUpload
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import timber.log.Timber

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSighInClient: GoogleSignInClient
    private lateinit var googleLoginResult: ActivityResultLauncher<Intent>

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
                val db = Firebase.firestore
                UserApiClient.instance.me { user, _ ->
                    val documentRef = db.collection("users").document("Kakao${user?.id}")
                    documentRef.get().addOnSuccessListener { documentSnapshot ->
                        if (!documentSnapshot.exists()) {
                            val userId = "Kakao${user?.id}"
                            val imageUri = Uri.parse(user?.kakaoAccount?.profile?.profileImageUrl)

                            // profileImgUpload 함수 호출
                            profileImgUpload(imageUri, userId) { isSuccess, uri ->
                                if (isSuccess && uri != null) {
                                    // 업로드 및 downloadUrl 성공
                                    val userModel = hashMapOf(
                                        "userId" to userId,
                                        "nickName" to "${user?.kakaoAccount?.profile?.nickname}",
                                        "profileImage" to uri.toString(),
                                        "userEmail" to "${user?.kakaoAccount?.email}",
                                        "bookmarked" to null
                                    )
                                    documentRef.set(userModel).addOnSuccessListener {
                                        EncryptedPrefs.saveMyId(userId)
                                        Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }.addOnFailureListener {
                                        // 문서 저장 실패
                                        Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                } else {
                                    // 업로드 실패 혹은 downloadUrl 실패
                                    Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        } else {
                            // 이미 문서가 존재하는 경우
                            Toast.makeText(this, "환영합니다!", Toast.LENGTH_SHORT).show()
                            EncryptedPrefs.saveMyId("Kakao${user?.id}")
                            finish()
                        }
                    }
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

        initActivityResult()
        firebaseAuth = FirebaseAuth.getInstance()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestId()
            .requestEmail()
            .requestProfile()
            .build()
        googleSighInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.cvGoogle.setOnClickListener {
            googleSignIn()
        }
    }

    private fun initActivityResult() {
        googleLoginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener { result ->
            val db = Firebase.firestore
            val documentRef = db.collection("users").document("Google${result.user?.uid}")
            val userModel = hashMapOf(
                "userId" to "Google${result.user?.uid}",
                "nickName" to "${result.user?.displayName}",
                "profileImage" to null,
                "userEmail" to "${result.user?.email}",
                "bookmarked" to null
            )
            documentRef.get().addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    // 이미지 업로드 후 사용자 문서 저장
                    profileImgUpload(Uri.parse(result.user?.photoUrl.toString()), "Google${result.user?.uid}") { isSuccess, uri ->
                        if (isSuccess && uri != null) {
                            // 업로드 성공, 다운로드 URL을 포함하여 문서 저장
                            userModel["profileImage"] = uri.toString()
                            documentRef.set(userModel).addOnSuccessListener {
                                EncryptedPrefs.saveMyId("Google${result.user?.uid}")
                                Timber.tag("로그인UserID검사").d("Google${result.user?.uid}")
                                Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                            }.addOnFailureListener {
                                Timber.tag("GoogleLoginError:문서저장실패").d(it.toString())
                                finish()
                            }
                        } else {
                            // 업로드 실패
                            Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    // 문서가 이미 존재하는 경우
                    EncryptedPrefs.saveMyId("Google${result.user?.uid}")
                    Toast.makeText(this, "환영합니다!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.addOnFailureListener { e ->
            Timber.tag("GoogleLoginError:인증실패").d(e.toString())
            finish()
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSighInClient.signInIntent
        googleLoginResult.launch(signInIntent)
    }
}