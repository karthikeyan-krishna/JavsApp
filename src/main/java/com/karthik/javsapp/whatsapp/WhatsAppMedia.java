package com.karthik.javsapp.whatsapp;

import com.google.protobuf.ByteString;
import com.karthik.javsapp.common.Constants;
import com.karthik.javsapp.common.Util;
import com.karthik.javsapp.crypto.EncryptionKeyPair;
import com.karthik.javsapp.crypto.MediaCrypto;
import com.karthik.javsapp.http.HttpUtil;
import com.karthik.javsapp.whatsapp.proto.ProtoBuf;
import com.neovisionaries.ws.client.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

import java.util.ArrayList;
import java.util.HashMap;

public class WhatsAppMedia {
    String number;
    String caption;
    Constants.FileType mediaType;
    byte[] file;

    public WhatsAppMedia(byte[] file, String number, String caption, Constants.FileType mediaType) {
        this.file = file;
        this.number = number;
        this.caption = caption;
        this.mediaType = mediaType;
    }

    public static void uploadMedia(JSONObject mediaConnection, ArrayList<WhatsAppMedia> media, EncryptionKeyPair keyPair, WebSocket ws) {
        JSONArray hosts = mediaConnection.getJSONArray("hosts");
        media.forEach(m -> {
            for (int i = 0; i < hosts.length(); i++) {
                try {
                    JSONObject host = hosts.getJSONObject(i);
                    MediaCrypto crypto = MediaCrypto.encrypt(m);
                    String fileEncSha256B54 = crypto.getFileEncSha256B64Str();
                    String encodedFile = Encode.forUriComponent(
                            fileEncSha256B54
                                    .replaceFirst("\\+", "-")
                                    .replaceFirst("/", "_")
                                    .replaceFirst("=+$", "")
                    );
                    String url = "https://" + host.getString("hostname") + Constants.MediaMap.IMAGE + "/" +
                            encodedFile;
                    HashMap<String, String> params = new HashMap<>();
                    params.put("auth", Encode.forUriComponent(mediaConnection.getString("auth")));
                    params.put("token", encodedFile);
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "multipart/form-data");
                    String response = HttpUtil.fetch(url, "POST", params, crypto.getBody(), headers);
                    JSONObject jsonResponse = new JSONObject(response);
                    System.out.println(jsonResponse);
                    if (jsonResponse.has("url")) {
                        long timestamp = Util.timestamp();
                        ProtoBuf.ImageMessage image = ProtoBuf.ImageMessage.newBuilder()
                                .setUrl(jsonResponse.getString("url"))
                                .setMediaKey(ByteString.copyFrom(crypto.getMediaKey()))
                                .setMimetype(Constants.MimeType.jpeg)

                                .setFileSha256(ByteString.copyFrom(crypto.getFileSha256()))
                                .setFileEncSha256(ByteString.copyFrom(crypto.getFileSha256B64()))

                                .setFileLength(m.getFile().length)
                                .setCaption(m.getCaption())
                                .setDirectPath(jsonResponse.getString("direct_path"))
                                .setMediaKeyTimestamp(timestamp)
                                .build();
                        ProtoBuf.WebMessageInfo messageInfo = ProtoBuf.WebMessageInfo.newBuilder().setKey(
                                ProtoBuf.MessageKey.newBuilder().setFromMe(true)
                                        .setRemoteJid(WhatsAppUtil.toId(m.getNumber()))
                                        .setId(WhatsAppUtil.generateMessageID())
                                        .build())
                                .setMessage(ProtoBuf.Message.newBuilder().setImageMessage(image).build())
                                .setMessageTimestamp(timestamp)
                                .setStatus(ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS.PENDING).build();
                        ws.sendBinary(WebSocketRequest.sendMessage(messageInfo, keyPair));
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getNumber() {
        return number;
    }

    public String getCaption() {
        return caption;
    }

    public Constants.FileType getMediaType() {
        return mediaType;
    }

    public byte[] getFile() {
        return file;
    }
}
