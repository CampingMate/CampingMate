package com.brandon.campingmate.utils

import android.util.Base64
import com.brandon.campingmate.data.remote.dto.UserDTO
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object UserCryptoUtils {
    var AES_KEY = ""
    // 대칭키 암호화 함수
    fun encrypt(data: String, key: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES") //SecretKeySpec클래스 사용해서 암호화 키 생성
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec) //객체를 암호화 모드로 초기화
        val encryptedBytes = cipher.doFinal(data.toByteArray()) //주어진 데이터를 암호화, 바이트배열로 전환
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT) //Base64로 인코딩해서 문자열로 반환
    }
    // 대칭키 복호화 함수
    fun decrypt(encryptedData: String, key: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
        return String(decryptedBytes)
    }

    fun UserDTO.toDecryptedUser(): UserDTO {
        return UserDTO(
            userId = userId,
            userEmail = userEmail?.let { decrypt(it, AES_KEY) },
            nickName = nickName?.let { decrypt(it, AES_KEY) },
            profileImage = profileImage,
            bookmarked = bookmarked
        )
    }
}