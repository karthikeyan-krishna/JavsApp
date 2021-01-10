package com.maharazhi.javsapp.whatsapp;

import com.maharazhi.javsapp.crypto.CryptoUtil;

/**
 * QR Code generation is handled here
 */
public class QRGenerator {
    /**
     * Buffered Image is generated from the given clientId and the serverId
     *
     * @param clientId  ClientID
     * @param serverId  ServerID
     * @param publicKey Public Key
     * @return BufferedImage of the QR Code
     */
    static String generateQRCode(String clientId, String serverId, byte[] publicKey) {
        String base64PubKey = CryptoUtil.base64EncodeToString(publicKey);
        return serverId + "," + base64PubKey + "," + clientId;
    }
}