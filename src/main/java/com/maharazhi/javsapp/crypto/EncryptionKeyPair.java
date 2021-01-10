package com.maharazhi.javsapp.crypto;

/**
 * A simple POJO for handling the enc and mac keys
 */
public class EncryptionKeyPair {
    private final byte[] encKey;
    private final byte[] macKey;

    public EncryptionKeyPair(byte[] encKey, byte[] macKey) {
        this.encKey = encKey;
        this.macKey = macKey;
    }

    public byte[] getEncKey() {
        return encKey;
    }

    public byte[] getMacKey() {
        return macKey;
    }
}
