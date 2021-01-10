package com.maharazhi.javsapp.whatsapp;

import org.json.JSONObject;

public class User {
    private final String phone;
    private final String pushName;
    private final String platform;
    private final String wid;

    User(JSONObject json) {
        this.wid = json.optString("wid");
        this.phone = wid.substring(0, wid.indexOf("@"));
        this.pushName = json.optString("pushname");
        this.platform = json.optString("platform");
    }

    public String getPhone() {
        return phone;
    }

    public String getPushName() {
        return pushName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getWid() {
        return wid;
    }
}
