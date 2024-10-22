package com.maharazhi.javsapp.whatsapp.message;

public class WebChat {

    private final String jid;
    private final String name;

    private final int unreadMessages;
    private final long lastInteraction;
    private final boolean muted;

    public WebChat(String jid, String name, int unreadMessages, long lastInteraction, boolean muted) {
        this.jid = jid;
        this.name = name;
        this.unreadMessages = unreadMessages;
        this.lastInteraction = lastInteraction;
        this.muted = muted;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name != null ? name : jid;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public long getLastInteraction() {
        return lastInteraction;
    }

    public boolean isMuted() {
        return muted;
    }
}
