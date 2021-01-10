package com.maharazhi.javsapp.crypto;

import com.maharazhi.javsapp.common.Util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * AES Encryption and Decryption are handled here
 */
public class AES {
    /**
     * AES Encryptor
     * Random byte[16] is used as IV and appended to the starting of the cipher
     *
     * @param plain  Plain Text to be Encrypted
     * @param secret Secret Key
     * @return AES Encrypted
     */
    public static byte[] encrypt(byte[] plain, byte[] secret) {
        try {
            byte[] iv = Util.randomBytes(16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plain);
            return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES Decryption is done here
     *
     * @param encrypted Encrypted Cipher Text
     * @param secretKey Secret Key
     * @return AES Decrypted
     */
    static byte[] decrypt(byte[] encrypted, byte[] secretKey) {
        byte[] iv = Arrays.copyOfRange(encrypted, 0, 16);
        return decrypt(Arrays.copyOfRange(encrypted, 16, encrypted.length), secretKey, iv);
    }

    /**
     * AES Decryption is done here
     *
     * @param encrypted Encrypted Cipher Text
     * @param secretKey Secret Key
     * @param iv        IV
     * @return AES Decrypted
     */
    static byte[] decrypt(byte[] encrypted, byte[] secretKey, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}