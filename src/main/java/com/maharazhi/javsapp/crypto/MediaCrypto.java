package com.maharazhi.javsapp.crypto;

import com.maharazhi.javsapp.common.Util;
import com.maharazhi.javsapp.whatsapp.WhatsAppMedia;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Media messages are encrypted here
 * Implementation as per https://github.com/sigalor/whatsapp-web-reveng
 */
public class MediaCrypto {
    private final String fileSha256Str;
    private final String fileEncSha256B64Str;
    private final byte[] fileSha256;
    private final byte[] fileSha256B64;
    private final byte[] body;
    private final byte[] mediaKey;

    private MediaCrypto(String fileSha256Str, String fileEncSha256B64Str, byte[] fileSha256, byte[] fileSha256B64,
                        byte[] body, byte[] mediaKey) {
        this.fileSha256Str = fileSha256Str;
        this.fileSha256 = fileSha256;
        this.fileSha256B64 = fileSha256B64;
        this.fileEncSha256B64Str = fileEncSha256B64Str;
        this.body = body;
        this.mediaKey = mediaKey;
    }

    private static byte[] mediaKey() {
        return Util.randomBytes(32);
    }

    public static MediaCrypto encrypt(WhatsAppMedia media) throws Exception {
        byte[] file = media.getFile();
        byte[] info = media.getMediaType().getHkdfKey().getBytes();

        byte[] mediaKey = mediaKey();
        byte[] mediaKeys = CryptoUtil.extractAndExpand(mediaKey, info, 112);

        byte[] iv = Arrays.copyOfRange(mediaKeys, 0, 16);
        byte[] cipherKey = Arrays.copyOfRange(mediaKeys, 16, 48);
        byte[] macKey = Arrays.copyOfRange(mediaKeys, 48, 80);

        byte[] enc = CryptoUtil.aesEncryptCbC(file, cipherKey, iv);
        byte[] mac = CryptoUtil.signHMAC(macKey, Util.concat(iv, enc));
        assert mac != null;
        mac = Arrays.copyOfRange(mac, 0, 10);
        byte[] body = Util.concat(enc, mac);
        byte[] fileSha256 = CryptoUtil.sha256(file);
        byte[] fileEncSha256 = CryptoUtil.sha256(body);

        String fileSha256Str = CryptoUtil.base64EncodeToString(fileSha256);
        String fileEncSha256B64Str = CryptoUtil.base64EncodeToString(fileEncSha256);
        return new MediaCrypto(fileSha256Str, fileEncSha256B64Str, fileSha256, fileEncSha256, body, mediaKey);
    }

    public static byte[] decrypt(byte[] mediaKey, String url, String khdfKey) {
        // Expand mediaKey to 112 bytes and add mediaInfo
        byte[] mediaKeyExpanded = CryptoUtil.extractAndExpand(mediaKey, khdfKey.getBytes(), 112);

        byte[] iv = Arrays.copyOfRange(mediaKeyExpanded, 0, 16);
        byte[] cipherKey = Arrays.copyOfRange(mediaKeyExpanded, 16, 48);
        byte[] macKey = Arrays.copyOfRange(mediaKeyExpanded, 48, 80);
        // refKey mediaKeyExpanded[80:112] not used

        // Download encrypted media
        byte[] encryptedMedia = CryptoUtil.urlToEncMedia(url);

        if (encryptedMedia != null) {
            byte[] file = Arrays.copyOfRange(encryptedMedia, 0, encryptedMedia.length - 10);
            byte[] mac = Arrays.copyOfRange(encryptedMedia, encryptedMedia.length - 10, encryptedMedia.length);

            // Hmac sign message
            byte[] message = ByteBuffer.allocate(iv.length + file.length).put(iv).put(file).array();

            // Validate macKey of mediaKeyExpanded with mac key of the encrypted media
            byte[] hmacSign = CryptoUtil.signHMAC(macKey, message);

            // Media validated
            assert hmacSign != null;
            if (Arrays.equals(mac, Arrays.copyOfRange(hmacSign, 0, 10))) {
                return AES.decrypt(file, cipherKey, iv);
            }
        }
        return null;
    }

    public String getFileSha256Str() {
        return fileSha256Str;
    }

    public String getFileEncSha256B64Str() {
        return fileEncSha256B64Str;
    }

    public byte[] getFileSha256() {
        return fileSha256;
    }

    public byte[] getFileSha256B64() {
        return fileSha256B64;
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] getMediaKey() {
        return mediaKey;
    }
}
