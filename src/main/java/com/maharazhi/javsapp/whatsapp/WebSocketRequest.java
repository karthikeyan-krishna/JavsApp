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

    static WebSocketRequest build(JSONArray array, EncryptionKeyPair keyPair, byte... tags) {
        return build(array, keyPair, null, tags);
    }

    static WebSocketRequest build(JSONArray array, EncryptionKeyPair keyPair, String tag, byte... tags) {
        byte[] encoded = new BinaryEncoder().encode(array);
        byte[] encrypted = AES.encrypt(encoded, keyPair.getEncKey());
        assert encrypted != null;
        byte[] hmacSign = CryptoUtil.signHMAC(keyPair.getMacKey(), encrypted);
        assert hmacSign != null;
        if (tag == null) {
            tag = WhatsAppUtil.getMessageTag();
        }
        byte[] messageTag = (tag + ",").getBytes();

        return new WebSocketRequest(tag, ByteBuffer.allocate(messageTag.length + tags.length + hmacSign.length + encrypted.length)
                .put(messageTag).put(tags).put(hmacSign).put(encrypted).array());
    }

    static WebSocketRequest sendMessage(ProtoBuf.WebMessageInfo message, EncryptionKeyPair keyPair) {
        JSONArray json = new JSONArray().put("action").put(new JSONObject().put("epoch", String.valueOf(WhatsApp.reqCount++))
                .put("type", "relay"))
                .put(new JSONArray().put(new JSONArray().put("message").put(JSONObject.NULL).put(message)));
        byte[] tags = new byte[]{Constants.WAMetric.message, Constants.WAFlag.ignore};
        return build(json, keyPair, tags);
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
        JSONArray array = new JSONArray().put("action").put(new JSONObject().put("epoch", String.valueOf(WhatsApp.reqCount++))
                .put("type", "set"))
                .put(new JSONArray().put(new JSONArray().put("picture")
                        .put(new JSONObject().put("jid", jid).put("id", tag).put("type", "set"))
                        .put(new JSONArray()
                                .put(new JSONArray().put("image").put(JSONObject.NULL).put(image))
                                .put(new JSONArray().put("preview").put(JSONObject.NULL).put(image)))));
        byte[] tags = new byte[]{Constants.WAMetric.picture, (byte) 136};
        return build(array, keyPair, tag, tags);
    }

    static WebSocketRequest contacts(EncryptionKeyPair keyPair) {
        return contacts(keyPair, String.valueOf(WhatsApp.reqCount++));
    }

    static WebSocketRequest contacts(EncryptionKeyPair keyPair, String epoch) {
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "contacts").put("epoch", epoch))
                .put(JSONObject.NULL);
        byte[] tags = {Constants.WAMetric.queryContact, Constants.WAFlag.ignore};
        return build(array, keyPair, tags);
    }

    static WebSocketRequest chats(EncryptionKeyPair keyPair) {
        return chats(keyPair, String.valueOf(WhatsApp.reqCount++));
    }

    static WebSocketRequest chats(EncryptionKeyPair keyPair, String epoch) {
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "chat").put("epoch", epoch))
                .put(JSONObject.NULL);
        byte[] tags = {Constants.WAMetric.queryChat, Constants.WAFlag.ignore};
        return build(array, keyPair, tags);
    }

    static WebSocketRequest status(EncryptionKeyPair keyPair) {
        return status(keyPair, String.valueOf(WhatsApp.reqCount++));
    }

    static WebSocketRequest status(EncryptionKeyPair keyPair, String epoch) {
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "status").put("epoch", epoch))
                .put(JSONObject.NULL);
        byte[] tags = {Constants.WAMetric.queryStatus, Constants.WAFlag.ignore};
        return build(array, keyPair, tags);
    }

    static WebSocketRequest label(EncryptionKeyPair keyPair) {
        return label(keyPair, String.valueOf(WhatsApp.reqCount++));
    }

    static WebSocketRequest label(EncryptionKeyPair keyPair, String epoch) {
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "label").put("epoch", epoch))
                .put(JSONObject.NULL);
        byte[] tags = {Constants.WAMetric.queryLabel, Constants.WAFlag.ignore};
        return build(array, keyPair, tags);
    }

    static WebSocketRequest emoji(EncryptionKeyPair keyPair) {
        return emoji(keyPair, String.valueOf(WhatsApp.reqCount++));
    }

    static WebSocketRequest emoji(EncryptionKeyPair keyPair, String epoch) {
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "emoji").put("epoch", epoch))
                .put(JSONObject.NULL);
        byte[] tags = {Constants.WAMetric.queryEmoji, Constants.WAFlag.ignore};
        return build(array, keyPair, tags);
    }

    static WebSocketRequest presence(EncryptionKeyPair keyPair) {
        return presence(keyPair, String.valueOf(WhatsApp.reqCount++));
    }

    static WebSocketRequest presence(EncryptionKeyPair keyPair, String epoch) {
        JSONArray array = new JSONArray().put("action").put(new JSONObject().put("type", "set").put("epoch", epoch))
                .put(new JSONArray().put(new JSONArray().put("presence").put(new JSONObject().put("type", "available"))
                        .put(JSONObject.NULL)));
        byte[] tags = {Constants.WAMetric.presence, (byte) 160};
        return build(array, keyPair, tags);
    }

    static WebSocketRequest quickReply(EncryptionKeyPair keyPair) {
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "quick_reply").put("epoch", "1"))
                .put(JSONObject.NULL);
        byte[] tags = {Constants.WAMetric.queryQuickReply, Constants.WAFlag.ignore};
        return build(array, keyPair, tags);
    }

    static WebSocketRequest loadConversation(String jId, int count, EncryptionKeyPair keyPair) {
        jId = WhatsAppUtil.toId(jId);
        byte[] tags = {Constants.WAMetric.queryMessages, Constants.WAFlag.ignore};
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "message")
                .put("epoch", String.valueOf(WhatsApp.reqCount++)).put("jid", jId).put("kind", "before")
                .put("count", String.valueOf(count))).put(JSONObject.NULL);
        return build(array, keyPair, tags);
    }

    static WebSocketRequest loadConversation(String jId, int count, String id, boolean owner, EncryptionKeyPair keyPair) {
        jId = WhatsAppUtil.toId(jId);
        byte[] tags = {Constants.WAMetric.queryMessages, Constants.WAFlag.ignore};
        JSONArray array = new JSONArray().put("query").put(new JSONObject().put("type", "message")
                .put("epoch", String.valueOf(WhatsApp.reqCount++)).put("jid", jId).put("kind", "before")
                .put("count", String.valueOf(count)).put("index", id).put("owner", owner)).put(JSONObject.NULL);
        return build(array, keyPair, tags);
    }
}
