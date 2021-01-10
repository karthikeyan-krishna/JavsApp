package com.maharazhi.javsapp.whatsapp;

import com.google.protobuf.ByteString;
import com.maharazhi.javsapp.common.Constants;
import com.maharazhi.javsapp.common.Util;
import com.maharazhi.javsapp.crypto.EncryptionKeyPair;
import com.maharazhi.javsapp.crypto.MediaCrypto;
import com.maharazhi.javsapp.http.HttpUtil;
import com.maharazhi.javsapp.proto.ProtoBuf;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

import java.util.ArrayList;
import java.util.HashMap;

public class WhatsAppMedia {
    String number;
    String caption;
    Constants.FileType mediaType;
    String mime;
    byte[] file;

    WhatsAppMedia(byte[] file, String number, String caption, Constants.FileType mediaType, String mime) {
        this.file = file;
        this.number = number;
        this.caption = caption;
        this.mediaType = mediaType;
        this.mime = mime;
    }

    static synchronized void uploadMedia(JSONObject mediaConn, ArrayList<WhatsAppMedia> medias, EncryptionKeyPair keyPair, WhatsApp app) {
        medias.parallelStream().forEach(m -> uploadMedia(mediaConn, m, keyPair, app));
        medias.clear();
    }

    static void uploadMedia(JSONObject mediaConn, WhatsAppMedia media, EncryptionKeyPair keyPair, WhatsApp app) {
        JSONArray hosts = mediaConn.getJSONArray("hosts");
        for (int i = 0; i < hosts.length(); i++) {
            try {
                MediaCrypto crypto = MediaCrypto.encrypt(media);
                String fileEncSha256B54 = crypto.getFileEncSha256B64Str();
                String encodedFile = Encode.forUriComponent(fileEncSha256B54
                        .replaceFirst("\\+", "-")
                        .replaceFirst("/", "_")
                        .replaceFirst("=+$", "")
                );
                String url = "https://" + hosts.getJSONObject(i).getString("hostname") +
                        media.getMediaType().getUrl() + "/" + encodedFile;
                HashMap<String, String> params = new HashMap<>();
                params.put("auth", Encode.forUriComponent(mediaConn.getString("auth")));
                params.put("token", encodedFile);
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "multipart/form-data");
                String response = HttpUtil.fetch(url, "POST", params, crypto.getBody(), headers);
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("url")) {
                    String uploadUrl = jsonResponse.getString("url");
                    String directPath = jsonResponse.getString("direct_path");
                    long timestamp = Util.timestamp();
                    ProtoBuf.Message message = null;
                    switch (media.getMediaType()) {
                        case image:
                            ProtoBuf.ImageMessage image = ProtoBuf.ImageMessage.newBuilder()
                                    .setUrl(uploadUrl)
                                    .setMediaKey(ByteString.copyFrom(crypto.getMediaKey()))
                                    .setMimetype(media.getMime())

                                    .setFileSha256(ByteString.copyFrom(crypto.getFileSha256()))
                                    .setFileEncSha256(ByteString.copyFrom(crypto.getFileSha256B64()))

                                    .setFileLength(media.getFile().length)
                                    .setCaption(media.getCaption())
                                    .setDirectPath(directPath)
                                    .setMediaKeyTimestamp(timestamp)
                                    .setJpegThumbnail(ByteString.copyFrom(media.getFile()))
                                    .build();
                            message = ProtoBuf.Message.newBuilder().setImageMessage(image).build();
                            break;
                        case video:
                            ProtoBuf.VideoMessage video = ProtoBuf.VideoMessage.newBuilder()
                                    .setUrl(uploadUrl)
                                    .setMediaKey(ByteString.copyFrom(crypto.getMediaKey()))
                                    .setMimetype(media.getMime())

                                    .setFileSha256(ByteString.copyFrom(crypto.getFileSha256()))
                                    .setFileEncSha256(ByteString.copyFrom(crypto.getFileSha256B64()))

                                    .setFileLength(media.getFile().length)
                                    .setCaption(media.getCaption())
                                    .setDirectPath(directPath)
                                    .setMediaKeyTimestamp(timestamp)
                                    .build();
                            message = ProtoBuf.Message.newBuilder().setVideoMessage(video).build();
                            break;
                        case document:
                            ProtoBuf.DocumentMessage document = ProtoBuf.DocumentMessage.newBuilder()
                                    .setUrl(uploadUrl)
                                    .setMediaKey(ByteString.copyFrom(crypto.getMediaKey()))
                                    .setMimetype(media.getMime())

                                    .setFileSha256(ByteString.copyFrom(crypto.getFileSha256()))
                                    .setFileEncSha256(ByteString.copyFrom(crypto.getFileSha256B64()))

                                    .setFileLength(media.getFile().length)
                                    .setTitle(media.getCaption())
                                    .setDirectPath(directPath)
                                    .setFileName(media.getCaption())
                                    .setMediaKeyTimestamp(timestamp)
                                    .build();
                            message = ProtoBuf.Message.newBuilder().setDocumentMessage(document).build();
                            break;
                        case sticker:
                            ProtoBuf.StickerMessage sticker = ProtoBuf.StickerMessage.newBuilder()
                                    .setUrl(uploadUrl)
                                    .setMediaKey(ByteString.copyFrom(crypto.getMediaKey()))
                                    .setMimetype(media.getMime())

                                    .setFileSha256(ByteString.copyFrom(crypto.getFileSha256()))
                                    .setFileEncSha256(ByteString.copyFrom(crypto.getFileSha256B64()))

                                    .setFileLength(media.getFile().length)
                                    .setDirectPath(directPath)
                                    .setMediaKeyTimestamp(timestamp)
                                    .build();
                            message = ProtoBuf.Message.newBuilder().setStickerMessage(sticker).build();
                            break;
                    }
                    ProtoBuf.WebMessageInfo messageInfo = ProtoBuf.WebMessageInfo.newBuilder().setKey(
                            ProtoBuf.MessageKey.newBuilder().setFromMe(true)
                                    .setRemoteJid(WhatsAppUtil.toId(media.getNumber()))
                                    .setId(WhatsAppUtil.generateMessageID()).build())
                            .setMessage(message)
                            .setMessageTimestamp(timestamp)
                            .setStatus(ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS.PENDING).build();

                    app.ws.sendBinary(WebSocketRequest.sendMessage(messageInfo, keyPair).getMessageBytes());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    public String getMime() {
        return mime;
    }
}
