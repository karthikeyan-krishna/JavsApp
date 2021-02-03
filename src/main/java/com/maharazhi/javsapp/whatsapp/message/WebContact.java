package com.maharazhi.javsapp.whatsapp.message;

public class WebContact {

    private final String jid;
    private final String name;
    private final String notify;

    public WebContact(String jid, String name, String notify) {
        this.jid = jid;
        this.name = name;
        this.notify = notify;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name;
    }

    public String getNotify() {
        return notify;
    }
}
