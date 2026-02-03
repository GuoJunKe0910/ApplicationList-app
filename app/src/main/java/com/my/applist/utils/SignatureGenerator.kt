package com.my.applist.utils
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.Throws
import kotlin.text.equals
import kotlin.text.isNotBlank
import kotlin.text.lowercase
import kotlin.text.toByteArray

/**
 * HmacSHA256 加密工具类
 * @author 辅助开发
 */
object HmacSHA256Utils {

    /**
     * HmacSHA256 加密字符串
     * @param data 待加密的原始字符串
     * @param secretKey 加密密钥（服务端约定的密钥）
     * @param charset 字符编码（默认 UTF-8，建议保持和服务端一致）
     * @return 加密后的16进制小写字符串（符合通用签名规范）
     * @throws NoSuchAlgorithmException 算法不存在（理论上不会出现）
     * @throws InvalidKeyException 密钥无效（如空密钥）
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun encrypt(
        data: String,
        secretKey: String,
        charset: Charset = StandardCharsets.UTF_8
    ): String {
        // 1. 校验参数合法性
        require(data.isNotBlank()) { "待加密字符串不能为空" }
        require(secretKey.isNotBlank()) { "加密密钥不能为空" }

        // 2. 初始化 HmacSHA256 加密实例
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(charset), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)

        // 3. 执行加密并转换为16进制字符串
        val encryptedBytes = mac.doFinal(data.toByteArray(charset))
        return bytesToHex(encryptedBytes)
    }

    /**
     * 安全加密方法（捕获异常，返回null）
     * 适合不想处理异常的场景，建议在业务层判断返回值
     */
    fun encryptSafe(
        data: String,
        secretKey: String,
        charset: Charset = StandardCharsets.UTF_8
    ): String? {
        return try {
            encrypt(data, secretKey, charset)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    /**
     * 验证 HMAC-SHA256 签名是否正确
     * @param data 原始数据
     * @param expectedSignature 期望的签名
     * @param secretKey 密钥
     * @return 签名是否有效
     */
    fun verifySignature(
        data: String,
        expectedSignature: String,
        secretKey: String
    ): Boolean {
        return try {
            val calculatedSignature = encrypt(data, secretKey)
            calculatedSignature.equals(expectedSignature, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    /**
     * 字节数组转16进制字符串（小写，通用规范）
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexString = kotlin.text.StringBuilder()
        for (b in bytes) {
            // 转换为无符号整数
            val hex = Integer.toHexString(0xff and b.toInt())
            // 补零（确保单个字节占两位）
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString().lowercase()
    }
}