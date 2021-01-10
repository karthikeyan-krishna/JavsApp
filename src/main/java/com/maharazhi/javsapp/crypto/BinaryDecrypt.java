package com.maharazhi.javsapp.crypto;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

public class BinaryDecrypt {
    public static byte[] decrypt(byte[] message, EncryptionKeyPair keyPair) throws DecoderException {
        String hexMessage = Hex.encodeHexString(message, true);
        int commaIndex = hexMessage.indexOf("2c");
        if (commaIndex < 1) {
            System.err.println("Invalid binary message");
            return null;
        }
        String strMessageContent = hexMessage.substring(commaIndex + 2);
        byte[] messageContent = Hex.decodeHex(strMessageContent);
        byte[] checksum = Arrays.copyOfRange(messageContent, 0, 32);
        messageContent = Arrays.copyOfRange(messageContent, 32, messageContent.length);
        byte[] hmacComputedChecksum = CryptoUtil.signHMAC(keyPair.getMacKey(), messageContent);
        if (Arrays.equals(hmacComputedChecksum, checksum)) {
            return AES.decrypt(messageContent, keyPair.getEncKey());
        }
        return null;
    }
}
