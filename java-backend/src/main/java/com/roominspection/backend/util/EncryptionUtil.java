package com.roominspection.backend.util;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类
 * 提供AES对称加密、SHA256哈希等功能
 * 用于敏感数据的加密存储
 */
@Component
public class EncryptionUtil {

    /**
     * AES密钥长度（256位）
     */
    private static final int AES_KEY_SIZE = 256;

    /**
     * GCM认证标签长度（128位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM IV长度（96位）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * 默认加密密钥（从配置文件读取）
     */
    private String encryptionKey;

    public EncryptionUtil() {
        // 默认密钥，生产环境应从配置文件或环境变量读取
        this.encryptionKey = "default-256-bit-encryption-key-change-me";
    }

    public void setEncryptionKey(String key) {
        this.encryptionKey = key;
    }

    /**
     * 生成AES密钥
     */
    public String generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }

    /**
     * AES-GCM加密
     * 使用AES-GCM模式，提供认证加密
     *
     * @param plainText 明文
     * @return Base64编码的密文（IV + 密文）
     */
    public String encrypt(String plainText) {
        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 准备密钥
            byte[] keyBytes = hashKey(encryptionKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

            // 初始化加密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);

            // 加密
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 组合IV和密文
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // Base64编码
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * AES-GCM解密
     *
     * @param cipherText Base64编码的密文（IV + 密文）
     * @return 明文
     */
    public String decrypt(String cipherText) {
        try {
            // Base64解码
            byte[] combined = Base64.getDecoder().decode(cipherText);

            // 分离IV和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            // 准备密钥
            byte[] keyBytes = hashKey(encryptionKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec);

            // 解密
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * SHA256哈希
     * 用于密码加密等场景
     *
     * @param input 输入字符串
     * @return 十六进制哈希值
     */
    public String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA256哈希失败", e);
        }
    }

    /**
     * 密钥哈希
     * 将任意长度的密钥转换为256位
     */
    private byte[] hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("密钥哈希失败", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 加密敏感字段（如手机号、身份证号等）
     * 采用部分加密方式，保留部分明文用于展示
     *
     * @param sensitiveData 敏感数据
     * @param maskLength    掩码长度
     * @return 加密后的数据
     */
    public String encryptSensitiveField(String sensitiveData, int maskLength) {
        if (sensitiveData == null || sensitiveData.isEmpty()) {
            return sensitiveData;
        }

        int length = sensitiveData.length();
        if (length <= maskLength * 2) {
            // 数据太短，全部加密
            return encrypt(sensitiveData);
        }

        // 保留前后部分，中间加密
        String prefix = sensitiveData.substring(0, maskLength);
        String suffix = sensitiveData.substring(length - maskLength);
        String middle = sensitiveData.substring(maskLength, length - maskLength);

        return prefix + "****" + suffix;
    }

    /**
     * 加密JSON对象
     * 用于加密操作日志的参数和响应结果
     *
     * @param jsonObject JSON字符串
     * @return 加密后的字符串
     */
    public String encryptJson(String jsonObject) {
        if (jsonObject == null || jsonObject.isEmpty()) {
            return jsonObject;
        }
        return encrypt(jsonObject);
    }

    /**
     * 解密JSON对象
     *
     * @param encryptedJson 加密的JSON字符串
     * @return 解密后的JSON字符串
     */
    public String decryptJson(String encryptedJson) {
        if (encryptedJson == null || encryptedJson.isEmpty()) {
            return encryptedJson;
        }
        return decrypt(encryptedJson);
    }
}
