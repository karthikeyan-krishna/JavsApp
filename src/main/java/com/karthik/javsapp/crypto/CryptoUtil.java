package com.karthik.javsapp.crypto;

import at.favre.lib.crypto.HKDF;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CryptoUtil {
    public static String base64EncodeToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] extractAndExpand(byte[] key, int length) {
        return extractAndExpand(key, null, length);
    }

    public static byte[] extendHKDF(byte[] key, byte[] info, int length) {
        byte[] pseudoRandomKey = HKDF.fromHmacSha256().extract(ByteBuffer.allocate(32).array(), key);
        return HKDF.fromHmacSha256().expand(pseudoRandomKey, info, length);
    }

    public static byte[] extractAndExpand(byte[] key, byte[] info, int length) {
        return HKDF.fromHmacSha256().extractAndExpand(ByteBuffer.allocate(32).array(), key, info, length);
    }

    public static byte[] signHMAC(byte[] key, String message) {
        return signHMAC(key, Base64.getDecoder().decode(message));
    }

    public static byte[] signHMAC(byte[] key, byte[] message) {
        try {
            Mac hash = Mac.getInstance("HmacSHA256");
            hash.init(new SecretKeySpec(key, hash.getAlgorithm()));
            return hash.doFinal(message);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] aesEncryptCbC(byte[] message, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        return cipher.doFinal(message);
    }

    public static byte[] sha256(byte[] message) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(message);
    }
}
