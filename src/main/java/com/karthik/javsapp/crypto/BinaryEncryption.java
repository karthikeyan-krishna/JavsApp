package com.karthik.javsapp.crypto;

import com.karthik.javsapp.common.Util;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

public class BinaryEncryption {

    public static byte[] decrypt(byte[] message, EncryptionKeyPair keyPair) throws DecoderException {
        String hexMessage = Hex.encodeHexString(message, true);

        int commaIndex = hexMessage.indexOf("2c");

        if (commaIndex < 1) {
            // This message is invalid
            System.err.println("Invalid binary message");
            return null;
        }

        String strMessageContent = hexMessage.substring(commaIndex + 2);
        byte[] messageContent = Hex.decodeHex(strMessageContent);
        byte[] checksum = Arrays.copyOfRange(messageContent, 0, 32);
        messageContent = Arrays.copyOfRange(messageContent, 32, messageContent.length);
        byte[] hmacComputedChecksum = CryptoUtil.signHMAC(keyPair.getMacKey(), messageContent);
        if (Arrays.equals(hmacComputedChecksum, checksum)) {
            return AES.decrypt(messageContent, keyPair.getEncKey(), 0, 16);
        }
        return null;
    }

    public static byte[] encrypt(byte[] message, EncryptionKeyPair keyPair) {
        return AES.encrypt(message, keyPair.getEncKey(), Util.randomBytes(16));
    }
}
