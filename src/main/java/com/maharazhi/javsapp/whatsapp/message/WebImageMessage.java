package com.maharazhi.javsapp.whatsapp.message;

import com.maharazhi.javsapp.common.Constants;
import com.maharazhi.javsapp.crypto.MediaCrypto;
import com.maharazhi.javsapp.proto.ProtoBuf;

public class WebImageMessage extends WebMessage {

    private final String mimetype;
    private final String url;
    private final String caption;
    private final byte[] fileSha256;
    private final byte[] mediaKey;
    private final byte[] jpegThumbnail;
    private final long fileLength;
    private final int width;
    private final int height;


    public WebImageMessage(ProtoBuf.WebMessageInfo message) {
        super(message);

        ProtoBuf.ImageMessage imageMessage = message.getMessage().getImageMessage();

        url = imageMessage.getUrl();
        mimetype = imageMessage.getMimetype();
        fileSha256 = imageMessage.getFileSha256().toByteArray();
        fileLength = imageMessage.getFileLength();
        height = imageMessage.getHeight();
        width = imageMessage.getWidth();
        mediaKey = imageMessage.getMediaKey().toByteArray();
        jpegThumbnail = imageMessage.getJpegThumbnail().toByteArray();
        caption = imageMessage.getCaption();
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getUrl() {
        return url;
    }

    public String getCaption() {
        return caption;
    }

    public byte[] getFileSha256() {
        return fileSha256;
    }

    public byte[] getMediaKey() {
        return mediaKey;
    }

    public byte[] getJpegThumbnail() {
        return jpegThumbnail;
    }

    public byte[] getJpegFullResolution() {
        return MediaCrypto.decrypt(mediaKey, url, Constants.FileType.image.getHkdfKey());
    }

    public long getFileLength() {
        return fileLength;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
