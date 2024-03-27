package com.brandon.campingmate.utils

import android.util.Base64
import com.brandon.campingmate.data.remote.dto.UserDTO
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object UserCryptoUtils {
    var AES_KEY = ""
    // 대칭키 암호화 함수
    fun encrypt(data: String, key: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec) //객체를 암호화 모드로 초기화
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined,0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    // 대칭키 복호화 함수
    fun decrypt(encryptedData: String, key: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val encryptedBytesWithIV = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = encryptedBytesWithIV.copyOfRange(0, 12)
        val encryptedBytes = encryptedBytesWithIV.copyOfRange(12, encryptedBytesWithIV.size)
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val gcmParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
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