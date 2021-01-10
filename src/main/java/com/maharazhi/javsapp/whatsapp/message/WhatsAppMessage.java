package com.maharazhi.javsapp.whatsapp.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.maharazhi.javsapp.crypto.CryptoUtil;
import com.maharazhi.javsapp.proto.ProtoBuf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

public class WhatsAppMessage {
    public static Object[] jsonToObject(JSONArray json) {

        JSONObject attributes = json.getJSONObject(1);
        // Attributes key values
        Set<String> keys = attributes.keySet();

        // Contains node content
        JSONArray childrenArray = json.getJSONArray(2);

        Object[] objects = null;

        if (keys.contains("add")) { // Generic message
            objects = messageToObject(childrenArray);
        } else if (!keys.contains("duplicate") && keys.contains("type")) {

            String typeValue = attributes.getString("type");

            switch (typeValue) {
                case "message":
                    // Also generic message but gets called as response of the loadConversation() method
                    objects = messageToObject(childrenArray);
                    break;
                case "chat":
                    objects = chatToObject(childrenArray);
                    break;
                case "contacts":
                    objects = contactToObject(childrenArray);
                    break;
                case "status":
                    objects = statusToObject(childrenArray);
                    break;
                case "emoji":
                    objects = emojiToObject(childrenArray);
                    break;
            }
        }
        return objects;
    }

    private static Object[] messageToObject(JSONArray childrenArray) {
        Object[] objects = new Object[childrenArray.length()];

        for (int i = 0; i < childrenArray.length(); i++) {
            // WebMessageInfo objects are encoded with base64 and need to be decoded
            String base64Message = childrenArray.getJSONArray(i).getJSONArray(2).getString(0);
            byte[] byteMessage = CryptoUtil.base64Decode(base64Message);
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

    private static Object[] chatToObject(JSONArray childrenArray) {
        Object[] objects = new Object[childrenArray.length()];

        for (int i = 0; i < childrenArray.length(); i++) {
            JSONObject chatAttributes = childrenArray.getJSONArray(i).getJSONObject(1);
            String jid = chatAttributes.getString("jid");
            String name = chatAttributes.has("name") ? chatAttributes.getString("name") : null;
            int unreadMessages = chatAttributes.getInt("count");
            long lastInteraction = chatAttributes.getLong("t");
            boolean muted = chatAttributes.optBoolean("mute");

            objects[i] = new WebChat(jid, name, unreadMessages, lastInteraction, muted);
        }
        return objects;
    }

    // Convert json message of the type "contacts" > WebChat
    private static Object[] contactToObject(JSONArray childrenArray) {
        Object[] objects = new Object[childrenArray.length()];

        for (int i = 0; i < childrenArray.length(); i++) {
            JSONObject chatAttributes = childrenArray.getJSONArray(i).getJSONObject(1);
            String jid = chatAttributes.getString("jid");
            String name = chatAttributes.has("name") ? chatAttributes.getString("name")
                    : chatAttributes.has("notify") ? chatAttributes.getString("notify") : null;

            objects[i] = new WebContact(jid, name);
        }
        return objects;
    }

    // Convert json message of the type "status" > WebStatus
    private static Object[] statusToObject(JSONArray childrenArray) {
        JSONArray messageArray = childrenArray.getJSONArray(0).getJSONArray(2);
        Object[] objects = new Object[messageArray.length()];

        for (int i = 0; i < messageArray.length(); i++) {
            String base64Message = messageArray.getJSONArray(i).getJSONArray(2).getString(0);
            try {
                ProtoBuf.WebMessageInfo messageInfo = ProtoBuf.WebMessageInfo.parseFrom(CryptoUtil.base64Decode(base64Message));
                ProtoBuf.Message message = messageInfo.getMessage();
                if (message.hasImageMessage()) {
                    // WebMessageInfo to WebImageMessage object
                    objects[i] = new WebStatus(new WebImageMessage(messageInfo));
                } else if (message.hasVideoMessage()) {
                    // WebMessageInfo to WebVideoMessage object
                    objects[i] = new WebStatus(new WebVideoMessage(messageInfo));
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    // Convert json message of the type "status" > WebEmoji
    private static Object[] emojiToObject(JSONArray childrenArray) {
        Object[] objects = new Object[childrenArray.length()];

        for (int i = 0; i < childrenArray.length(); i++) {
            JSONObject emojiAttributes = childrenArray.getJSONArray(i).getJSONObject(1);
            String code = emojiAttributes.getString("code");
            double value = Double.parseDouble(emojiAttributes.getString("value"));
            objects[i] = new WebEmoji(code, value);
        }
        return objects;
    }
}
