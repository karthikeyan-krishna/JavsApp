package com.karthik.javsapp.whatsapp;

import com.karthik.javsapp.common.Constants;
import com.karthik.javsapp.crypto.BinaryEncoder;
import com.karthik.javsapp.crypto.BinaryEncryption;
import com.karthik.javsapp.crypto.CryptoUtil;
import com.karthik.javsapp.crypto.EncryptionKeyPair;
import com.karthik.javsapp.security.WhatsAppCredentials;
import com.karthik.javsapp.whatsapp.proto.ProtoBuf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;

import static com.karthik.javsapp.common.Constants.WHATSAPP_WEB_VERSION;
import static com.karthik.javsapp.whatsapp.WhatsAppUtil.getMessageTag;

public class WebSocketRequest {
    public static String login(String clientId) {
        JSONArray array = new JSONArray().put("admin").put("init").put(WHATSAPP_WEB_VERSION)
                .put(new JSONArray().put("Karthikeyan's Tech").put("Version 1.0"))
                .put(clientId).put(true);
        return getMessageTag() + "," + array;
    }

    public static String newServerId() {
        return getMessageTag() + "," + new JSONArray().put("admin").put("Conn").put("refef");
    }

    public static String restoreSession(WhatsAppCredentials credentials) {
        return getMessageTag() + "," + new JSONArray().put("admin").put("login").put(credentials.getClientToken())
                .put(credentials.getServerToken()).put(credentials.getClientId()).put("takeover");
    }

    public static String solveChallenge(String signed, String serverToken, String clientId) {
        return getMessageTag() + "," + new JSONArray().put("admin").put("challenge").put(signed).put(serverToken)
                .put(clientId);
    }

    public static String uploadMediaURL() {
        return getMessageTag() + "," + new JSONArray().put("query").put("mediaConn");
    }

    public static byte[] sendMessage(ProtoBuf.WebMessageInfo message, EncryptionKeyPair keyPair) {
        JSONArray json = new JSONArray().put("action").put(new JSONObject().put("epoch", String.valueOf(WhatsApp.reqCount))
                .put("type", "relay"))
                .put(new JSONArray().put(new JSONArray().put("message").put(JSONObject.NULL).put(message)));
        byte[] encoded = new BinaryEncoder().encode(json);
        byte[] encrypted = BinaryEncryption.encrypt(encoded, keyPair);
        byte[] hmacSign = CryptoUtil.signHMAC(keyPair.getMacKey(), encrypted);
        if (hmacSign != null) {
            byte[] messageTag = (WhatsAppUtil.getBinaryMessageTag() + ",").getBytes();
            byte[] tags = new byte[]{Constants.WAMetric.message, Constants.WAFlag.ignore};
            return ByteBuffer.allocate(
                    messageTag.length + tags.length + hmacSign.length + encrypted.length)
                    .put(messageTag).put(tags).put(hmacSign).put(encrypted).array();
        }
        return null;
    }
}
