package com.maharazhi.javsapp.whatsapp;

import com.maharazhi.javsapp.common.Constants;
import com.maharazhi.javsapp.crypto.AES;
import com.maharazhi.javsapp.crypto.BinaryEncoder;
import com.maharazhi.javsapp.crypto.CryptoUtil;
import com.maharazhi.javsapp.crypto.EncryptionKeyPair;
import com.maharazhi.javsapp.proto.ProtoBuf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;

class WebSocketRequest {
    private final String tag;
    private final Object message;

    private WebSocketRequest(String tag, Object message) {
        this.tag = tag;
        this.message = message;
    }

    String getTag() {
        return tag;
    }

    String getMessageString() {
        return (String) message;
    }

    byte[] getMessageBytes() {
        return (byte[]) message;
    }

    static WebSocketRequest login(String clientId, String description) {
        JSONArray array = new JSONArray().put("admin").put("init").put(Constants.WHATSAPP_WEB_VERSION)
                .put(new JSONArray().put(description).put("Version 1.0"))
                .put(clientId).put(true);
        String tag = WhatsAppUtil.getMessageTag();
        return new WebSocketRequest(tag, tag + "," + array);
    }

    static WebSocketRequest newServerId() {
        String tag = WhatsAppUtil.getMessageTag();
        return new WebSocketRequest(tag, tag + "," + new JSONArray().put("admin").put("Conn").put("reref"));
    }

    static WebSocketRequest restoreSession(WhatsAppCredentials credentials) {
        String tag = WhatsAppUtil.getMessageTag();
        return new WebSocketRequest(tag, tag + "," + new JSONArray().put("admin").put("login")
                .put(credentials.getClientToken()).put(credentials.getServerToken()).put(credentials.getClientId())
                .put("takeover"));
    }

    static WebSocketRequest solveChallenge(String signed, String serverToken, String clientId) {
        String tag = WhatsAppUtil.getMessageTag();
        return new WebSocketRequest(tag, tag + "," + new JSONArray().put("admin").put("challenge").put(signed)
                .put(serverToken).put(clientId));
    }

    public static WebSocketRequest uploadMediaURL() {
        String tag = WhatsAppUtil.getMessageTag();
        return new WebSocketRequest(tag, tag + "," + new JSONArray().put("query").put("mediaConn"));
    }

    static WebSocketRequest sendMessage(ProtoBuf.WebMessageInfo message, EncryptionKeyPair keyPair) {
        JSONArray json = new JSONArray().put("action").put(new JSONObject().put("epoch", String.valueOf(WhatsApp.reqCount))
                .put("type", "relay"))
                .put(new JSONArray().put(new JSONArray().put("message").put(JSONObject.NULL).put(message)));
        byte[] encoded = new BinaryEncoder().encode(json);
        byte[] encrypted = AES.encrypt(encoded, keyPair.getEncKey());
        assert encrypted != null;
        byte[] hmacSign = CryptoUtil.signHMAC(keyPair.getMacKey(), encrypted);
        assert hmacSign != null;
        String tag = WhatsAppUtil.getMessageTag();
        byte[] messageTag = (tag + ",").getBytes();
        byte[] tags = new byte[]{Constants.WAMetric.message, Constants.WAFlag.ignore};
        return new WebSocketRequest(tag, ByteBuffer.allocate(messageTag.length + tags.length + hmacSign.length + encrypted.length)
                .put(messageTag).put(tags).put(hmacSign).put(encrypted).array());
    }

    static WebSocketRequest logout() {
        String tag = "goodbye";
        return new WebSocketRequest(tag, tag + "," + new JSONArray().put("admin").put("Conn").put("disconnect"));
    }

    static WebSocketRequest test() {
        String tag = WhatsAppUtil.getMessageTag();
        return new WebSocketRequest(tag, tag + "," + new JSONArray().put("admin").put("test"));
    }

    static WebSocketRequest updateDisplayPicture(String jid, byte[] image, EncryptionKeyPair keyPair) {
        jid = WhatsAppUtil.toId(jid);
        String tag = WhatsAppUtil.getMessageTag();
        JSONArray array = new JSONArray().put("action").put(new JSONObject().put("epoch", String.valueOf(WhatsApp.reqCount))
                .put("type", "set"))
                .put(new JSONArray().put(new JSONArray().put("picture")
                        .put(new JSONObject().put("jid", jid).put("id", tag).put("type", "set"))
                        .put(new JSONArray()
                                .put(new JSONArray().put("image").put(JSONObject.NULL).put(image))
                                .put(new JSONArray().put("preview").put(JSONObject.NULL).put(image)))));
        byte[] tags = new byte[]{Constants.WAMetric.picture, (byte) 136};
        byte[] messageTag = (tag + ",").getBytes();
        byte[] encoded = new BinaryEncoder().encode(array);
        byte[] encrypted = AES.encrypt(encoded, keyPair.getEncKey());
        assert encrypted != null;
        byte[] hmacSign = CryptoUtil.signHMAC(keyPair.getMacKey(), encrypted);
        assert hmacSign != null;
        return new WebSocketRequest(tag, ByteBuffer.allocate(messageTag.length + tags.length + hmacSign.length + encrypted.length)
                .put(messageTag).put(tags).put(hmacSign).put(encrypted).array());
    }
}
