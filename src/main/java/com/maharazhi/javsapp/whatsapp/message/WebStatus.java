package com.maharazhi.javsapp.whatsapp.message;

public class WebStatus {

    private final WebImageMessage imageMessage;
    private final WebVideoMessage videoMessage;

    public WebStatus(WebImageMessage imageMessage) {
        this.imageMessage = imageMessage;
        this.videoMessage = null;
    }

    public WebStatus(WebVideoMessage videoMessage) {
        this.videoMessage = videoMessage;
        this.imageMessage = null;
    }

    public boolean isWebImageMessage() {
        return imageMessage != null;
    }

    public boolean isWebVideoMessage() {
        return videoMessage != null;
    }

    public WebImageMessage getWebImageMessage() {
        return imageMessage;
    }

    public WebVideoMessage getWebVideoMessage() {
        return videoMessage;
    }
}
