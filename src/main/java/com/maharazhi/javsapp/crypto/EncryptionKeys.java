package com.maharazhi.javsapp.crypto;

import org.whispersystems.curve25519.Curve25519;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class EncryptionKeys {
    public static EncryptionKeyPair generate(String base64Secret, byte[] privateKey) {
        byte[] secret = CryptoUtil.base64Decode(base64Secret);
        if (secret.length != 144) {
            System.err.println("Invalid secret length received: " + secret.length);
            return null;
        }
        byte[] publicKey = Arrays.copyOfRange(secret, 0, 32);
        byte[] sharedSecret = Curve25519.getInstance(Curve25519.BEST).calculateAgreement(publicKey, privateKey);
        byte[] sharedSecretExpanded = CryptoUtil.extractAndExpand(sharedSecret, 80);
        assert hmacValidate(sharedSecretExpanded, secret);

        byte[] sharedEnc = Arrays.copyOfRange(sharedSecretExpanded, 64, sharedSecretExpanded.length);
        byte[] secretEnc = Arrays.copyOfRange(secret, 64, secret.length);
        byte[] keysEncrypted = ByteBuffer.allocate(sharedEnc.length + secretEnc.length).put(sharedEnc)
                .put(secretEnc).array();
        byte[] decryptKey = Arrays.copyOfRange(sharedSecretExpanded, 0, 32);
        byte[] keysDecrypted = AES.decrypt(keysEncrypted, decryptKey);
        assert keysDecrypted != null && keysDecrypted.length == 64;
        byte[] encKey = Arrays.copyOfRange(keysDecrypted, 0, 32);
        byte[] macKey = Arrays.copyOfRange(keysDecrypted, 32, 64);
        return new EncryptionKeyPair(encKey, macKey);
    }

    private static boolean hmacValidate(byte[] sharedSecretExpanded, byte[] secret) {
        byte[] hmacValidationKey = Arrays.copyOfRange(sharedSecretExpanded, 32, 64);
        byte[] hmacSecretA = Arrays.copyOfRange(secret, 0, 32);
        byte[] hmacSecretB = Arrays.copyOfRange(secret, 64, secret.length);
        byte[] hmacValidationMessage = ByteBuffer.allocate(hmacSecretA.length + hmacSecretB.length)
                .put(hmacSecretA).put(hmacSecretB).array();
        byte[] hmac = CryptoUtil.signHMAC(hmacValidationKey, hmacValidationMessage);
        return Arrays.equals(hmac, Arrays.copyOfRange(secret, 32, 64));
    }
}
