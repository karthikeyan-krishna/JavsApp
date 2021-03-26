package com.maharazhi.javsapp.whatsapp.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.maharazhi.javsapp.crypto.CryptoUtil;
import com.maharazhi.javsapp.proto.ProtoBuf;
import org.json.JSONArray;
import org.json.JSONObject;

public class WhatsAppMessage {
    public static Object[] jsonToObject(JSONArray json) {
        JSONObject attributes = json.optJSONObject(1);
        if (attributes == null) return null;
        JSONArray array = json.getJSONArray(2);
        Object[] objects = null;
        if (attributes.has("add")) {
            objects = messageToObject(array);
        } else if (!attributes.has("duplicate")) {
            String typeValue = attributes.optString("type");
            switch (typeValue) {
                case "message":
                    objects = messageToObject(array);
                    break;
                case "chat":
                    objects = chatToObject(array);
                    break;
                case "contacts":
                    objects = contactToObject(array);
                    break;
                case "status":
                    objects = statusToObject(array);
                    break;
                case "emoji":
                    objects = emojiToObject(array);
                    break;
            }
        }
        return objects;
    }

    private static Object[] messageToObject(JSONArray array) {
        Object[] objects = new Object[array.length()];
        for (int i = 0; i < array.length(); i++) {
            byte[] byteMessage = CryptoUtil.base64Decode(array.getJSONArray(i).getJSONArray(2).getString(0));
            try {
                ProtoBuf.WebMessageInfo messageInfo = ProtoBuf.WebMessageInfo.parseFrom(byteMessage);
                ProtoBuf.Message message = messageInfo.getMessage();
                if (message.hasImageMessage()) {
                    objects[i] = new WebImageMessage(messageInfo);
                } else if (message.hasVideoMessage()) {
                    objects[i] = new WebVideoMessage(messageInfo);
                } else if (message.hasConversation() || message.hasExtendedTextMessage()) {
                    objects[i] = new WebConversationMessage(messageInfo);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    /**
     * Parsing the array and converting it to WebChat objects
     *
     * @param chats JSONArray containing the chats
     * @return Array of WebChat
     */
    private static WebChat[] chatToObject(JSONArray chats) {
        WebChat[] objects = new WebChat[chats.length()];
        for (int i = 0; i < chats.length(); i++) {
            JSONObject chat = chats.getJSONArray(i).getJSONObject(1);
            String jid = chat.getString("jid");
            String name = chat.optString("name", null);
            int unreadMessages = chat.getInt("count");
            long lastInteraction = chat.getLong("t");
            boolean muted = chat.optBoolean("mute");
            objects[i] = new WebChat(jid, name, unreadMessages, lastInteraction, muted);
        }
        return objects;
    }

    /**
     * Parsing the array and converting it to WebContact objects
     *
     * @param contacts JSONArray containing the contacts
     * @return Array of WebContact
     */
    private static WebContact[] contactToObject(JSONArray contacts) {
        WebContact[] objects = new WebContact[contacts.length()];
        for (int i = 0; i < contacts.length(); i++) {
            JSONObject contact = contacts.getJSONArray(i).getJSONObject(1);
            objects[i] = new WebContact(contact.getString("jid"), contact.optString("name", null),
                    contact.optString("notify", null));
        }
        return objects;
    }

    /**
     * Parsing the array and converting it to WebStatus objects
     *
     * @param status JSONArray containing the contacts
     * @return Array of WebStatus
     */
    private static WebStatus[] statusToObject(JSONArray status) {
        JSONArray statuses = status.getJSONArray(0).getJSONArray(2);
        WebStatus[] objects = new WebStatus[statuses.length()];
        for (int i = 0; i < statuses.length(); i++) {
            try {
                ProtoBuf.WebMessageInfo messageInfo = ProtoBuf.WebMessageInfo.parseFrom(CryptoUtil.base64Decode(
                        statuses.getJSONArray(i).getJSONArray(2).getString(0)));
                ProtoBuf.Message message = messageInfo.getMessage();
                if (message.hasImageMessage()) {
                    objects[i] = new WebStatus(new WebImageMessage(messageInfo));
                } else if (message.hasVideoMessage()) {
                    objects[i] = new WebStatus(new WebVideoMessage(messageInfo));
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    /**
     * Parsing the array and converting it to WebEmoji objects
     *
     * @param emojis JSONArray containing the contacts
     * @return Array of WebEmoji
     */
    private static WebEmoji[] emojiToObject(JSONArray emojis) {
        WebEmoji[] objects = new WebEmoji[emojis.length()];
        for (int i = 0; i < emojis.length(); i++) {
            JSONObject emoji = emojis.getJSONArray(i).getJSONObject(1);
            String code = emoji.getString("code");
            double value = emoji.optDouble("value");
            objects[i] = new WebEmoji(code, value);
        }
        return objects;
    }
}
