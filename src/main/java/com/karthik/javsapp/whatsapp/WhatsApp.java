package com.karthik.javsapp.whatsapp;

import com.karthik.javsapp.common.ExpectedResponse;
import com.karthik.javsapp.common.Util;
import com.karthik.javsapp.crypto.CryptoUtil;
import com.karthik.javsapp.crypto.EncryptionKeyPair;
import com.karthik.javsapp.crypto.EncryptionKeys;
import com.karthik.javsapp.security.WhatsAppCredentials;
import com.karthik.javsapp.whatsapp.proto.ProtoBuf.ExtendedTextMessage;
import com.karthik.javsapp.whatsapp.proto.ProtoBuf.Message;
import com.karthik.javsapp.whatsapp.proto.ProtoBuf.MessageKey;
import com.karthik.javsapp.whatsapp.proto.ProtoBuf.WebMessageInfo;
import com.karthik.javsapp.whatsapp.proto.ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

import static com.karthik.javsapp.common.Constants.*;


public abstract class WhatsApp extends WebSocketAdapter {
    public static String binaryMessageTag = "";
    public static long reqCount = 0;
    private boolean loggedIn;
    protected byte expectedResponse;
    private WhatsAppCredentials credentials;
    protected WebSocket ws;
    private QRRefresher refresher;
    public WhatsAppEvents whatsAppEvents;
    private final ArrayList<WhatsAppMedia> media = new ArrayList<>();
    private KeepAlive keepAlive;

    abstract public void saveAuth(WhatsAppCredentials credentials);

    abstract public WhatsAppCredentials loadAuth();

    public WhatsApp() {
        credentials = loadAuth();
        if (credentials == null) {
            credentials = new WhatsAppCredentials();
        }
    }

    public void connect() {
        try {
            disconnect();
            WebSocketFactory factory = new WebSocketFactory();
            ws = factory.createSocket(WHATSAPP_SERVER);
            ws.addHeader("Origin", HEADER_ORIGIN);
            ws.addHeader("User-Agent", HEADER_USER_AGENT);
            ws.addListener(this);
            ws.connect();
            keepAlive = new KeepAlive(ws);
            keepAlive.start();
            loggedIn = false;
            expectedResponse = ExpectedResponse.LOGIN;
            ws.sendText(WebSocketRequest.login(credentials.getClientId()));
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) {
        if (message.startsWith("!")) {
            keepAlive.updatePong();
            return;

        }
        JSONArray array;
        String tag;
        switch (expectedResponse) {
            case ExpectedResponse.LOGIN:
                if (credentials.getClientToken() == null) {
                    refresher = new QRRefresher(this, credentials.getClientId(), credentials.getPublicKey());
                    refresher.start();
                    refresher.newQRCode(message);
                    expectedResponse = ExpectedResponse.SCAN_QR_CODE;
                } else {
                    expectedResponse = ExpectedResponse.RESTORE_SESSION;
                    restoreSession();
                }
                break;
            case ExpectedResponse.SCAN_QR_CODE:
                array = Util.encodeValidJsonArray(message);
                tag = array.getString(0);
                if (tag.equals("Conn")) {
                    refresher.setQRCodeScanned(true);
                    JSONObject json = array.getJSONObject(1);
                    updateCredentials(json);
                    loggedIn = true;
                } else if (tag.equals("ref")) {
                    refresher.newQRCode(message);
                }
                break;
            case ExpectedResponse.RESTORE_SESSION:
                array = Util.encodeValidJsonArray(message);
                tag = array.getString(0);
                JSONObject json = array.getJSONObject(1);
                if ("Cmd".equals(tag)) {
                    if (json.optString("type").equals("challenge")) {
                        String challenge = json.getString("challenge");
                        solveChallenge(challenge);
                        break;
                    }
                } else if ("Conn".equals(tag)) {
                    whatsAppEvents.login(200);
                    loggedIn = true;
                }
                break;
            case ExpectedResponse.MEDIA_UPLOAD_URL:
                json = Util.encodeValidJson(message);
                if ("200".equals(json.optString("status"))) {
                    WhatsAppMedia.uploadMedia(json.getJSONObject("media_conn"), media,
                            credentials.getEncryptionKeyPair(), ws);
                }
                break;
        }
        System.out.println(message);
    }

    public void disconnect() {
        if (ws != null && ws.isOpen()) {
            ws.disconnect();
        }
    }

    public void setListener(WhatsAppEvents whatsAppEvents) {
        this.whatsAppEvents = whatsAppEvents;
    }

    protected void requestNewServerId() {
        ws.sendText(WebSocketRequest.newServerId());
    }

    private void restoreSession() {
        ws.sendText(WebSocketRequest.restoreSession(credentials));
    }

    private void updateCredentials(JSONObject json) {
        String serverToken = json.getString("serverToken");
        String clientToken = json.getString("clientToken");
        String secret = json.getString("secret");
        EncryptionKeyPair keyPair = EncryptionKeys.generate(secret, credentials.getPrivateKey());
        if (keyPair != null) {
            credentials.setClientToken(clientToken);
            credentials.setServerToken(serverToken);
            credentials.setEncKey(CryptoUtil.base64EncodeToString(keyPair.getEncKey()));
            credentials.setMacKey(CryptoUtil.base64EncodeToString(keyPair.getMacKey()));
            saveAuth(credentials);
        }
    }

    private void solveChallenge(String challenge) {
        byte[] signedChallenge = CryptoUtil.signHMAC(Base64.getDecoder().decode(credentials.getMacKey()), challenge);
        String signedChallengeBase64 = CryptoUtil.base64EncodeToString(signedChallenge);
        String serverToken = credentials.getServerToken();
        String clientId = credentials.getClientId();
        ws.sendText(WebSocketRequest.solveChallenge(signedChallengeBase64, serverToken, clientId));
    }

    static class KeepAlive extends Thread {
        private long lastPong;

        private final WebSocket ws;

        public KeepAlive(WebSocket ws) {
            this.ws = ws;
        }

        @Override
        public void run() {
            boolean running = true;
            Random rand = new Random();
            while (running) {
                Util.waitMillis((rand.nextInt(21) + 15) * 1000);
                ws.sendText("?,,");
                Util.waitMillis(3000);
                if (Instant.now().getEpochSecond() - lastPong >= 5) {
                    running = false;
                }
            }
        }

        public void updatePong() {
            lastPong = Instant.now().getEpochSecond();
        }
    }

    public void sendText(String phone, String text) {
        phone = WhatsAppUtil.toId(phone);
        WebMessageInfo message = WebMessageInfo.newBuilder()
                .setMessage(Message.newBuilder().setExtendedTextMessage(
                        ExtendedTextMessage.newBuilder().setText(text).build()))
                .setKey(MessageKey.newBuilder().setFromMe(true).setRemoteJid(phone)
                        .setId(WhatsAppUtil.generateMessageID()).build())
                .setMessageTimestamp(Util.timestamp())
                .setStatus(WEB_MESSAGE_INFO_STATUS.PENDING).build();
        try {
            ws.sendBinary(WebSocketRequest.sendMessage(message, credentials.getEncryptionKeyPair()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadMedia(String number, byte[] media, String caption, FileType fileType) {
        this.media.add(new WhatsAppMedia(media, number, caption, fileType));
        expectedResponse = ExpectedResponse.MEDIA_UPLOAD_URL;
        ws.sendText(WebSocketRequest.uploadMediaURL());
    }
}
