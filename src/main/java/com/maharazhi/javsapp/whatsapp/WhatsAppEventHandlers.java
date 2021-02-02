package com.maharazhi.javsapp.whatsapp;

import com.maharazhi.javsapp.whatsapp.message.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class WhatsAppEventHandlers {
    public void login(WhatsApp app, int httpCode) {
    }

    public void disconnect(WhatsApp app, JSONObject json) {
    }

    public void replaced(WhatsApp app) {
    }

    public void qr(WhatsApp app, String message) {
    }

    public void textMessage(WhatsApp app, String message) {
    }

    public void textMessage(WhatsApp app, String tag, Object message) {
    }

    public void binaryMessage(WhatsApp app, byte[] message) {
    }

    public void onWebConversationMessage(WhatsApp app, WebConversationMessage message) {
    }

    public void onWebImageMessage(WhatsApp app, WebImageMessage message) {
    }

    public void onWebVideoMessage(WhatsApp app, WebVideoMessage message) {
    }

    public void onWebChat(WhatsApp app, WebChat[] chats) {
    }

    public void onWebContact(WhatsApp app, WebContact[] contacts) {
    }

    public void onWebStatus(WhatsApp app, WebStatus[] status) {
    }

    public void onWebEmoji(WhatsApp app, WebEmoji[] emojis) {
    }

    public void onDpUpdate(WhatsApp app, String url) {
    }

    public void unpaired(WhatsApp app) {
    }

    public void accessDenied(WhatsApp app, String tos) {
    }

    public void alreadyLoggedIn(WhatsApp app) {
    }

    public void anotherLogin(WhatsApp app) {
    }

    public void reusePreviousQR(WhatsApp app) {
    }

    public void qrDenied(WhatsApp app) {
    }

    public void otherError(WhatsApp app, JSONObject json) {
    }

    public void other(WhatsApp app, JSONObject json) {
    }

    public void other(WhatsApp app, JSONArray array) {
    }

    public void blockList(WhatsApp app, JSONArray array) {
    }

    public void stream(WhatsApp app, JSONArray array) {
    }

    public void props(WhatsApp app, JSONArray array) {
    }

    public void msg(WhatsApp app, JSONObject json) {
    }

    public void presence(WhatsApp app, JSONArray array) {
    }

    public void qrRefreshClosed(WhatsApp app) {
    }

    public void request(WhatsApp app, String tag, Object request) {
    }
}
