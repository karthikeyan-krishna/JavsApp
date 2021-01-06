package com.karthik.javsapp.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AES {
    public static byte[] encrypt(byte[] strToEncrypt, byte[] secret, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(strToEncrypt);
            return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static byte[] decrypt(byte[] encrypted, byte[] secretKey, int start, int end) {
        return decrypt(encrypted, secretKey, Arrays.copyOfRange(encrypted, start, end));
    }

    static byte[] decrypt(byte[] encrypted, byte[] secretKey, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            byte[] encryptedMessage = Arrays.copyOfRange(encrypted, 16, encrypted.length);
            return cipher.doFinal(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}