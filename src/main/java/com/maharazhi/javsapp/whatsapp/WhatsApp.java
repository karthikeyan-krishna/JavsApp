package com.maharazhi.javsapp.whatsapp;

import com.maharazhi.javsapp.common.Constants;
import com.maharazhi.javsapp.common.Util;
import com.maharazhi.javsapp.crypto.*;
import com.maharazhi.javsapp.proto.ProtoBuf;
import com.maharazhi.javsapp.whatsapp.message.*;
import com.neovisionaries.ws.client.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * All the operation of the WhatsApp are declared here
 * This is an abstract Class and has to be extended for implementing
 * saveAuth() and loadAuth() methods have to be implemented for storing and retrieving WhatsApp Credentials
 */
public class WhatsApp extends WebSocketAdapter {
    static long reqCount = 0;
    private boolean loggedIn;
    public WhatsAppCredentials credentials;
    protected WebSocket ws;
    private QRRefresher refresher;
    public WhatsAppEventHandlers events;
    private final ArrayList<WhatsAppMedia> media = new ArrayList<>();
    private KeepAlive keepAlive;
    private final String description;
    private boolean challengeSent = false;
    private User user;
    private JSONObject mediaConn;
    private long lastPong = 0L;

    public void saveAuth(WhatsAppCredentials credentials) {
    }

    public WhatsAppCredentials loadAuth() {
        return null;
    }

    public WhatsApp(String description) {
        this(description, new WhatsAppEventHandlers());
    }

    public WhatsApp(String description, WhatsAppEventHandlers events) {
        this.description = description;
        this.events = events;
        init();
    }

    public WhatsApp() {
        this("Maharazhi by Karthik");
    }

    private void init() {
        loggedIn = false;
        credentials = loadAuth();
        if (credentials == null) {
            credentials = new WhatsAppCredentials();
            refresher = new QRRefresher(this, credentials.getClientId(), credentials.getPublicKey());
            refresher.start();
        }
        connect();
    }

    private WebSocket createSocket() throws IOException {
        WebSocket ws = new WebSocketFactory().createSocket(Constants.WHATSAPP_SERVER);
        ws.addHeader("Origin", Constants.HEADER_ORIGIN);
        ws.addHeader("User-Agent", Constants.HEADER_USER_AGENT);
        ws.addListener(this);
        return ws;
    }

    public final void connect() {
        try {
            if (ws != null && ws.isOpen()) {
                ws.disconnect();
            }
            if (ws == null) {
                ws = createSocket();
            }
            if (ws.getState() == WebSocketState.CLOSED) {
                ws = ws.recreate();
            }
            ws.connect();
            sendText(WebSocketRequest.login(credentials.getClientId(), description));
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
    }

    private void sendText(WebSocketRequest request) {
        String message = request.getMessageString();
        events.request(this, request.getTag(), message);
        ws.sendText(message);
    }

    private void sendBinary(WebSocketRequest request) {
        byte[] bytes = request.getMessageBytes();
        events.request(this, request.getTag(), bytes);
        ws.sendBinary(bytes);
    }

    /**
     * Disconnect the web socket alone
     */
    public final void disconnectApp() {
        ws.disconnect();
        keepAlive.kill();
    }

    /**
     * Logout the session and connect with a new session
     */
    public final void disconnect() {
        ws.disconnect();
        init();
    }

    /**
     * Logout the session and disconnect the WebSocket without reconnecting
     */
    public final void disconnectPermanently() {
        if (ws != null && ws.isOpen()) {
            keepAlive.kill();
            if (isLoggedIn()) {
                sendText(WebSocketRequest.logout());
            }
            ws.disconnect();
        }
    }

    public final boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * All messages including Media are received in Binary
     * The binary message has to be decrypted to obtain the Message
     *
     * @param websocket WebSocket Instance
     * @param binary    Binary message received
     * @throws Exception Exception thrown
     * @see BinaryDecrypt will take care of decryption
     */
    @Override
    public final void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        events.binaryMessage(this, binary);
        byte[] decrypted = BinaryDecrypt.decrypt(binary, credentials.getEncryptionKeyPair());
        Object[] objects = WhatsAppMessage.jsonToObject(new BinaryDecoder().decode(decrypted));
        if (objects != null) {
            if (objects[0] instanceof WebChat) {
                events.onWebChat(this, Arrays.stream(objects).toArray(WebChat[]::new));
            } else if (objects[0] instanceof WebContact) {
                events.onWebContact(this, Arrays.stream(objects).toArray(WebContact[]::new));
            } else if (objects[0] instanceof WebStatus) {
                events.onWebStatus(this, Arrays.stream(objects).toArray(WebStatus[]::new));
            } else if (objects[0] instanceof WebEmoji) {
                events.onWebEmoji(this, Arrays.stream(objects).toArray(WebEmoji[]::new));
            }

            for (Object obj : objects) {
                if (obj instanceof WebImageMessage) {
                    events.onWebImageMessage(this, (WebImageMessage) obj);
                } else if (obj instanceof WebVideoMessage) {
                    events.onWebVideoMessage(this, (WebVideoMessage) obj);
                } else if (obj instanceof WebConversationMessage) {
                    events.onWebConversationMessage(this, (WebConversationMessage) obj);
                }
            }
        }
    }

    /**
     * All meta data are received in String as a TextMessage
     * The message has to be parsed and the content has to be obtained
     *
     * @param websocket Websocket Instance
     * @param message   Message in String
     */
    @Override
    public final void onTextMessage(WebSocket websocket, String message) {
        events.textMessage(this, message);
        if (message.startsWith("!")) {
            return;
        }
        JSONObject jsonMessage = Util.encodeValid(message);
        Object object = jsonMessage.get("raw");
        events.textMessage(this, jsonMessage.getString("tag"), object);
        if (object instanceof JSONObject) {
            JSONObject json = (JSONObject) object;
            String status = json.optString("status");
            switch (status) {
                case "200":
                    JSONObject mediaConn = json.optJSONObject("media_conn");
                    String ref = json.optString("ref", null);
                    String eurl = json.optString("eurl", null);
                    if (mediaConn != null) {
                        mediaConn.put("time", System.currentTimeMillis() + (mediaConn.getLong("ttl") * 1000));
                        this.mediaConn = mediaConn;
                        WhatsAppMedia.uploadMedia(mediaConn, this.media, credentials.getEncryptionKeyPair(), this);
                    } else if (ref != null) {
                        if (credentials.getClientToken() == null) {
                            refresher.newQRCode(ref);
                        } else {
                            restoreSession();
                        }
                    } else if (challengeSent) {
                        challengeSent = false;
                        events.login(this, 200);
                        if (keepAlive == null || !keepAlive.running) {
                            post();
                            keepAlive = new KeepAlive();
                            keepAlive.start();
                        }
                        loggedIn = true;
                    } else if (eurl != null) {
                        events.onDpUpdate(this, eurl);
                    } else {
                        events.other(this, json);
                    }
                    break;
                case "401":
                    events.unpaired(this);
                    break;
                case "403":
                    events.accessDenied(this, json.optString("tos"));
                    break;
                case "405":
                    events.alreadyLoggedIn(this);
                    break;
                case "409":
                    events.anotherLogin(this);
                    break;
                case "304":
                    events.reusePreviousQR(this);
                    break;
                case "429":
                    events.qrDenied(this);
                    break;
                default:
                    events.otherError(this, json);
            }
        } else if (object instanceof JSONArray) {
            JSONArray array = (JSONArray) object;
            String tag = array.optString(0);
            switch (tag) {
                case "Conn":
                    JSONObject cred = array.getJSONObject(1);
                    if (cred.has("serverToken") && cred.has("clientToken")) {
                        updateCredentials(array.getJSONObject(1));
                        loggedIn = true;
                        events.login(this, 200);
                    }
                    if (keepAlive == null || !keepAlive.running) {
                        post();
                        keepAlive = new KeepAlive();
                        keepAlive.start();
                    }
                    break;
                case "Cmd":
                    JSONObject json = array.getJSONObject(1);
                    String type = json.optString("type");
                    if (type.equals("challenge")) {
                        solveChallenge(json.getString("challenge"));
                        challengeSent = true;
                    } else if (type.equals("disconnect")) {
                        events.disconnect(this, json);
                        if (json.optString("kind").equals("replaced")) {
                            events.replaced(this);
                            reconnect();
                        }
                    }
                    break;
                case "BlockList":
                    events.blockList(this, array);
                    break;
                case "Stream":
                    events.stream(this, array);
                    break;
                case "Props":
                    events.props(this, array);
                    break;
                case "Msg":
                    events.msg(this, array.getJSONObject(1));
                    break;
                case "Presence":
                    events.presence(this, array);
                    break;
                case "Pong":
                    if (array.getBoolean(1)) {
                        lastPong = System.currentTimeMillis();
                    } else {
                        reconnect();
                    }
                    break;
                default:
                    events.other(this, array);
            }
        }
    }

    public final void reconnect() {
        new Reconnect().start();
    }

    final void requestNewServerId() {
        sendText(WebSocketRequest.newServerId());
    }

    private void restoreSession() {
        sendText(WebSocketRequest.restoreSession(credentials));
    }

    private void ping() {
        ws.sendText("?,,");
    }

    private void test() {
        sendText(WebSocketRequest.test());
    }

    private void updateCredentials(JSONObject json) {
        user = new User(json);
        String serverToken = json.getString("serverToken");
        String clientToken = json.getString("clientToken");
        String secret = json.getString("secret");
        EncryptionKeyPair keyPair = EncryptionKeys.generate(secret, credentials.getPrivateKey());
        assert keyPair != null;
        credentials.setClientToken(clientToken);
        credentials.setServerToken(serverToken);
        credentials.setEncKey(CryptoUtil.base64EncodeToString(keyPair.getEncKey()));
        credentials.setMacKey(CryptoUtil.base64EncodeToString(keyPair.getMacKey()));
        saveAuth(credentials);
    }

    private void solveChallenge(String challenge) {
        byte[] signedChallenge = CryptoUtil.signHMAC(CryptoUtil.base64Decode(credentials.getMacKey()), challenge);
        String signedChallengeBase64 = CryptoUtil.base64EncodeToString(signedChallenge);
        String serverToken = credentials.getServerToken();
        String clientId = credentials.getClientId();
        sendText(WebSocketRequest.solveChallenge(signedChallengeBase64, serverToken, clientId));
    }

    public final User getUser() {
        return user;
    }

    private void post() {
        sendBinary(WebSocketRequest.contacts(credentials.getEncryptionKeyPair(), "1"));
        sendBinary(WebSocketRequest.chats(credentials.getEncryptionKeyPair(), "1"));
        sendBinary(WebSocketRequest.status(credentials.getEncryptionKeyPair(), "1"));
        sendBinary(WebSocketRequest.label(credentials.getEncryptionKeyPair(), "1"));
        sendBinary(WebSocketRequest.emoji(credentials.getEncryptionKeyPair(), "1"));
        sendBinary(WebSocketRequest.presence(credentials.getEncryptionKeyPair(), "1"));
    }

    public final void checkPong() {
        if (System.currentTimeMillis() - lastPong >= 5 * 60 * 1000) {
            disconnect();
        }
    }

    /**
     * This will ping for keeping the connection stable
     */
    private class KeepAlive extends Thread {
        boolean running = true;

        @Override
        public void run() {
            boolean test = false;
            while (running) {
                ping();
                if (test) {
                    test();
                }
                Util.waitMillis(30_000);
                if (test) {
                    checkPong();
                }
                test = !test;
            }
        }

        public void kill() {
            System.err.println("Killing WhatsApp");
            running = false;
        }
    }

    /**
     * This thread is used to reconnect automatically when the phone connection disconnects
     */
    private final class Reconnect extends Thread {
        @Override
        public void run() {
            loggedIn = false;
            disconnect();
            Util.waitMillis(10_000);
            while (!isLoggedIn()) {
                connect();
                Util.waitMillis(10_000);
            }
        }
    }

    /**
     * A WhatsApp message can be sent by calling this method
     *
     * @param phone Receiver Phone number
     * @param text  Text Message
     */
    public final String sendText(String phone, String text) {
        phone = WhatsAppUtil.toId(phone);
        String id = WhatsAppUtil.generateMessageID();
        ProtoBuf.WebMessageInfo message = ProtoBuf.WebMessageInfo.newBuilder()
                .setMessage(ProtoBuf.Message.newBuilder().setExtendedTextMessage(
                        ProtoBuf.ExtendedTextMessage.newBuilder().setText(text).build()))
                .setKey(ProtoBuf.MessageKey.newBuilder().setFromMe(true).setRemoteJid(phone)
                        .setId(id).build())
                .setMessageTimestamp(Util.timestamp())
                .setStatus(ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS.PENDING).build();
        try {
            sendBinary(WebSocketRequest.sendMessage(message, credentials.getEncryptionKeyPair()));
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    synchronized JSONObject getMediaConn() {
        return mediaConn;
    }

    public void addMedia(WhatsAppMedia media) {
        this.media.add(media);
    }

    /**
     * For uploading a media, the WhatsAppMedia object is added to the media list, and the media url is requested
     * Once the media URL is obtained, all the media in the list is uploaded via the URL and the list is cleared
     *
     * @param number  Number to be sent
     * @param media   Media in byte[]
     * @param caption Caption for the media
     * @param type    FileType Object
     * @param mime    Mime String
     */
    public final synchronized String sendMedia(String number, byte[] media, String caption, Constants.FileType type, String mime) {
        WhatsAppMedia whatsAppMedia = new WhatsAppMedia(media, number, caption, type, mime);
        JSONObject mediaConn = getMediaConn();
        if (mediaConn == null || System.currentTimeMillis() - mediaConn.getLong("time") >= 10000) {
            addMedia(whatsAppMedia);
            sendText(WebSocketRequest.uploadMediaURL());
            return whatsAppMedia.getId();
        } else {
            return WhatsAppMedia.uploadMedia(mediaConn, whatsAppMedia, credentials.getEncryptionKeyPair(), this);
        }
    }

    /**
     * An Image can be sent by calling this method
     *
     * @param number  Receiver Phone Number
     * @param media   Image in bytes[]
     * @param caption Caption to be sent with the Image
     * @param mime    Mime Type
     */
    public final String sendImage(String number, byte[] media, String caption, String mime) {
        return sendMedia(number, media, caption, Constants.FileType.image, mime);
    }

    public final String sendSticker(String number, byte[] media) {
        return sendMedia(number, media, null, Constants.FileType.sticker, "image/webp");
    }

    /**
     * A Video can be sent by calling this method
     * TODO: Need to generate thumbnail for sending the Video. For now, the video will be sent without any thumbnail
     *
     * @param number  Receiver Phone Number
     * @param media   Video in bytes[]
     * @param caption Caption to be sent with the Image
     * @param mime    Mime Type
     */
    public final String sendVideo(String number, byte[] media, String caption, String mime) {
        return sendMedia(number, media, caption, Constants.FileType.video, mime);
    }

    /**
     * Update the current user's DisplayPicture
     *
     * @param media Image in byte[]
     */
    public final void updateDisplayPicture(byte[] media) {
        sendBinary(WebSocketRequest.updateDisplayPicture(getUser().getPhone(), media, credentials.getEncryptionKeyPair()));
    }

    /**
     * Upload an Image as WhatsApp Story
     *
     * @param media   Image in byte[]
     * @param caption Caption
     * @param mime    Mime of the Image
     */
    public final String sendStoryImage(byte[] media, String caption, String mime) {
        return sendImage("status@broadcast", media, caption, mime);
    }

    /**
     * Upload a Video as WhatsApp Story
     *
     * @param media   Video in Bytes
     * @param caption Caption
     * @param mime    Mime of the Video
     */
    public final String sendStoryVideo(byte[] media, String caption, String mime) {
        return sendVideo("status@broadcast", media, caption, mime);
    }

    /**
     * Send a Document here
     *
     * @param document Document in bytes[]
     * @param number   Receiver's number
     * @param title    Title to the Document
     * @param mime     Mime of the Document
     */
    public final String sendDocument(String number, byte[] document, String title, String mime) {
        return sendMedia(number, document, title, Constants.FileType.document, mime);
    }

    public void loadConversation(String jId, int count) {
        sendBinary(WebSocketRequest.loadConversation(jId, count, credentials.getEncryptionKeyPair()));
    }

    public void loadConversation(String jId, int count, String id, boolean owner) {
        sendBinary(WebSocketRequest.loadConversation(jId, count, id, owner, credentials.getEncryptionKeyPair()));
    }
}
