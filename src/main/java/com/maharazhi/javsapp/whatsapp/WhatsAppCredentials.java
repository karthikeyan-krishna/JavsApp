package com.maharazhi.javsapp.whatsapp;

import com.maharazhi.javsapp.crypto.CryptoUtil;
import com.maharazhi.javsapp.crypto.EncryptionKeyPair;
import com.maharazhi.javsapp.whatsapp.WhatsAppUtil;
import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

/**
 * WhatsApp login credentials are stored in this format
 */
public class WhatsAppCredentials {
    private final String clientId;
    private String serverToken;
    private String clientToken;
    private String encKey;
    private String macKey;
    private Curve25519KeyPair curveKeys;

    public WhatsAppCredentials(String clientId, String serverToken, String clientToken, String encKey, String macKey) {
        this.clientId = clientId;
        this.clientToken = clientToken;
        this.serverToken = serverToken;
        this.encKey = encKey;
        this.macKey = macKey;
    }

    public WhatsAppCredentials(JSONObject json) {
        this(json.getString("clientId"), json.getString("serverToken"), json.getString("clientToken"),
                json.getString("encKey"), json.getString("macKey"));
    }

    WhatsAppCredentials() {
        curveKeys = Curve25519.getInstance(Curve25519.BEST).generateKeyPair();
        clientId = WhatsAppUtil.getClientId();
    }

    public String getClientId() {
        return clientId;
    }

    public String getServerToken() {
        return serverToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getEncKey() {
        return encKey;
    }

    public String getMacKey() {
        return macKey;
    }

    public Curve25519KeyPair getCurveKeys() {
        return curveKeys;
    }

    public byte[] getPublicKey() {
        return getCurveKeys().getPublicKey();
    }

    public byte[] getPrivateKey() {
        return getCurveKeys().getPrivateKey();
    }

    void setServerToken(String serverToken) {
        this.serverToken = serverToken;
    }

    public EncryptionKeyPair getEncryptionKeyPair() {
        return new EncryptionKeyPair(CryptoUtil.base64Decode(encKey),
                CryptoUtil.base64Decode(macKey));
    }

    void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    void setEncKey(String encKey) {
        this.encKey = encKey;
    }

    void setMacKey(String macKey) {
        this.macKey = macKey;
    }

    void setCurveKeys(Curve25519KeyPair curveKeys) {
        this.curveKeys = curveKeys;
    }

    public JSONObject toJson() {
        return new JSONObject().put("serverToken", serverToken).put("clientToken", clientToken)
                .put("encKey", encKey).put("macKey", macKey).put("clientId", clientId);
    }
}
