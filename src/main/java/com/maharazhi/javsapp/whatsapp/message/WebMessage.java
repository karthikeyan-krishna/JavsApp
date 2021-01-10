package com.maharazhi.javsapp.whatsapp.message;

import com.maharazhi.javsapp.proto.ProtoBuf;

class WebMessage {
    private final String remoteJid;
    private final String id;
    private final boolean fromMe;
    private final long messageTimestamp;
    private final ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS status;

    public WebMessage(ProtoBuf.WebMessageInfo message) {
        remoteJid = message.getKey().getRemoteJid();
        id = message.getKey().getId();
        fromMe = message.getKey().getFromMe();
        messageTimestamp = message.getMessageTimestamp();
        status = message.getStatus();
    }

    public String getRemoteJid() {
        return remoteJid;
    }

    public String getId() {
        return id;
    }

    public boolean getFromMe() {
        return fromMe;
    }

    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    public ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS getStatus() {
        return status;
    }
}
