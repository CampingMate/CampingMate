package com.brandon.campingmate.presentation.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.brandon.campingmate.R
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.databinding.ActivityLoginBinding
import com.brandon.campingmate.presentation.login.LoginActivity.Constants.AES_KEY
import com.brandon.campingmate.utils.profileImgUpload
import com.bumptech.glide.Glide
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
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSighInClient: GoogleSignInClient
    private lateinit var googleLoginResult: ActivityResultLauncher<Intent>
    object Constants{
//        const val AES_KEY = "abcdefghijklmnopabcdefghijklmnop"
        // AES 암호화에 사용할 무작위로 생성된 256비트 키
        val AES_KEY: ByteArray = generateRandomAESKey()
        private fun generateRandomAESKey(): ByteArray {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256, SecureRandom())
            val secretKey: SecretKey = keyGen.generateKey()
            return secretKey.encoded
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Glide.with(binding.root)
            .asGif()
            .load(R.drawable.ic_brand_img_gif)
            .into(binding.ivLogin)

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
                        binding.clLoginLoading.visibility = View.GONE
                        finish()
                        Timber.tag("KakaoLoginError").d("기타 에러")
                        //Toast.makeText(this, "기타 에러", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (token != null) {
                binding.clLoginLoading.visibility = View.VISIBLE
                val db = Firebase.firestore
                UserApiClient.instance.me { user, _ ->
                    val documentRef = db.collection("users").document("Kakao${user?.id}")
                    val encryptedNickName =
                        user?.kakaoAccount?.profile?.nickname?.take(10)
                            ?.let { encrypt(it, AES_KEY) }
                    val encryptedUserEmail = user?.kakaoAccount?.email?.let { encrypt(it, AES_KEY) }
                    val userModel = hashMapOf(
                        "userId" to "Kakao${user?.id}",
                        "nickName" to encryptedNickName,
                        "profileImage" to "${user?.kakaoAccount?.profile?.profileImageUrl}",
                        "userEmail" to encryptedUserEmail,
                        "bookmarked" to null
                    )
                    documentRef.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documentSnapshot = task.result
                            if (!documentSnapshot.exists()) {
                                profileImgUpload(Uri.parse(user?.kakaoAccount?.profile?.profileImageUrl), "Kakao${user?.id}")
                                documentRef.set(userModel)
                                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Toast.makeText(this, "환영합니다.", Toast.LENGTH_SHORT).show()
                        EncryptedPrefs.saveMyId("Kakao${user?.id}")
                        binding.clLoginLoading.visibility = View.GONE
                        finish()
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
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    // Google 로그인 실패 처리
                    Timber.tag("GoogleLoginError").d("Google 로그인 실패: ${e.statusCode}")
                    finish()
                }
            } else {
                // 사용자가 로그인을 취소했거나 결과가 OK가 아닐 때의 처리
                Timber.tag("GoogleLoginError").d("로그인 사용자 취소 또는 실패")
                finish()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        binding.clLoginLoading.visibility = View.VISIBLE
        val db = Firebase.firestore
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener { result ->
            val documentRef = db.collection("users").document("Google${result.user?.uid}")
            val encryptedNickName = result.user?.displayName?.take(10)?.let { encrypt(it, AES_KEY) }
            val encryptedUserEmail = result.user?.email?.let { encrypt(it, AES_KEY) }
            val userModel = hashMapOf(
                "userId" to "Google${result.user?.uid}",
                "nickName" to encryptedNickName,
                "profileImage" to "${result.user?.photoUrl}",
                "userEmail" to encryptedUserEmail,
                "bookmarked" to null
            )
            documentRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (!documentSnapshot.exists()) {
                        profileImgUpload(Uri.parse(result.user?.photoUrl.toString()), "Google${result.user?.uid}")
                        documentRef.set(userModel)
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(this, "환영합니다.", Toast.LENGTH_SHORT).show()
                EncryptedPrefs.saveMyId("Google${result.user?.uid}")
                binding.clLoginLoading.visibility = View.GONE
                finish()
            }
        }
            .addOnFailureListener {
                Timber.tag("GoogleLoginError").d(it.toString())
                binding.clLoginLoading.visibility = View.GONE
                finish()
            }
    }

    private fun googleSignIn() {
        val signInIntent = googleSighInClient.signInIntent
        googleLoginResult.launch(signInIntent)
    }
    // 대칭키 암호화 함수
    fun encrypt(data: String, key: ByteArray): String {
        //AES알고리즘 사용해서 ECB모드와 PKCS5Padding패딩방식을 지정
        //ECB모드는 암호화할 블록이 서로 독립적으로 처리되는 방식
        //PKCS5Padding은 블록 크기에 맞춰 패딩을 추가하는 방식
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key, "AES") //SecretKeySpec클래스 사용해서 암호화 키 생성
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec) //객체를 암호화 모드로 초기화
        val encryptedBytes = cipher.doFinal(data.toByteArray()) //주어진 데이터를 암호화, 바이트배열로 전환
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT) //Base64로 인코딩해서 문자열로 반환
    }
}