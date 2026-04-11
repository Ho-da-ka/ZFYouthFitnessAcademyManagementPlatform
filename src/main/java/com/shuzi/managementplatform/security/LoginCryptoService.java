package com.shuzi.managementplatform.security;

import com.shuzi.managementplatform.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decrypts login password payload encrypted on client side.
 */
@Service
public class LoginCryptoService {

    private final byte[] keyBytes;

    public LoginCryptoService(@Value("${security.login-crypto.aes-key}") String aesKey) {
        this.keyBytes = aesKey == null ? new byte[0] : aesKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("security.login-crypto.aes-key must be 16/24/32 bytes");
        }
    }

    public String decrypt(String encryptedPassword, String iv) {
        try {
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] cipherBytes = Base64.getDecoder().decode(encryptedPassword);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new IvParameterSpec(ivBytes)
            );
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "登录凭证解析失败");
        }
    }
}

