package com.maharazhi.javsapp.crypto;

import at.favre.lib.crypto.HKDF;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This contains various supporting methods for Cryptography
 */
public interface CryptoUtil {
    static String base64EncodeToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    static byte[] base64Decode(String str) {
        return Base64.getDecoder().decode(str);
    }

    static byte[] extractAndExpand(byte[] key, int length) {
        return extractAndExpand(key, null, length);
    }

    static byte[] extractAndExpand(byte[] key, byte[] info, int length) {
        return HKDF.fromHmacSha256().expand(HKDF.fromHmacSha256().extract((SecretKey) null, key), info, length);
    }

    static byte[] signHMAC(byte[] key, String message) {
        return signHMAC(key, base64Decode(message));
    }

    static byte[] signHMAC(byte[] key, byte[] message) {
        try {
            Mac hash = Mac.getInstance("HmacSHA256");
            hash.init(new SecretKeySpec(key, hash.getAlgorithm()));
            return hash.doFinal(message);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    static byte[] aesEncryptCbC(byte[] message, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        return cipher.doFinal(message);
    }

    static byte[] sha256(byte[] message) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(message);
    }

    static byte[] urlToEncMedia(String url) {
        try {
            return IOUtils.toByteArray(new URL(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
