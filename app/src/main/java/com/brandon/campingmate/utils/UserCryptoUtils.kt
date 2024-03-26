package com.brandon.campingmate.utils

import android.util.Base64
import com.brandon.campingmate.data.remote.dto.UserDTO
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object UserCryptoUtils {
    // AES 암호화에 사용할 무작위로 생성된 256비트 키
    val AES_KEY: ByteArray = generateRandomAESKey()
    private fun generateRandomAESKey(): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, SecureRandom())
        val secretKey: SecretKey = keyGen.generateKey()
        return secretKey.encoded
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
    // 대칭키 복호화 함수
    fun decrypt(encryptedData: String, key: ByteArray): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
        val secretKeySpec = SecretKeySpec(key, "AES")
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