package com.maharazhi.javsapp.whatsapp.message;

import com.maharazhi.javsapp.common.Constants;
import com.maharazhi.javsapp.crypto.MediaCrypto;
import com.maharazhi.javsapp.proto.ProtoBuf;

public class WebVideoMessage extends WebMessage {

    private final String mimetype, url;
    private final byte[] fileSha256, mediaKey, jpegThumbnail;
    private final long fileLength;
    private final int seconds;
    private final ProtoBuf.VideoMessage.VIDEO_MESSAGE_ATTRIBUTION gifAttribution;
    private final boolean gifPlayback;
    private final String caption;


    public WebVideoMessage(ProtoBuf.WebMessageInfo message) {
        super(message);

        ProtoBuf.VideoMessage videoMessage = message.getMessage().getVideoMessage();

        url = videoMessage.getUrl();
        mimetype = videoMessage.getMimetype();
        fileSha256 = videoMessage.getFileSha256().toByteArray();
        fileLength = videoMessage.getFileLength();
        seconds = videoMessage.getSeconds();
        mediaKey = videoMessage.getMediaKey().toByteArray();
        gifPlayback = videoMessage.getGifPlayback();
        jpegThumbnail = videoMessage.getJpegThumbnail().toByteArray();
        gifAttribution = videoMessage.getGifAttribution();
        caption = videoMessage.getCaption();
    }

    public byte[] getJpegThumbnail() {
        return jpegThumbnail;
    }

    public boolean isGifPlayback() {
        return gifPlayback;
    }

    public String getCaption() {
        return caption;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getUrl() {
        return url;
    }

    public byte[] getFileSha256() {
        return fileSha256;
    }

    public byte[] getMediaKey() {
        return mediaKey;
    }

    public byte[] getMp4Thumbnail() {
        return jpegThumbnail;
    }

    public byte[] getMp4FullResolution() {
        return MediaCrypto.decrypt(mediaKey, url, Constants.FileType.video.getHkdfKey());
    }

    public long getFileLength() {
        return fileLength;
    }

    public int getSeconds() {
        return seconds;
    }

    public ProtoBuf.VideoMessage.VIDEO_MESSAGE_ATTRIBUTION getGifAttribution() {
        return gifAttribution;
    }

    public boolean getGifPlayback() {
        return gifPlayback;
    }
}
