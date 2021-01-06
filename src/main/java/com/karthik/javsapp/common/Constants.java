package com.karthik.javsapp.common;

import org.json.JSONArray;

public interface Constants {
    String WHATSAPP_SERVER = "wss://web.whatsapp.com/ws";
    String HEADER_ORIGIN = "https://web.whatsapp.com";
    String HEADER_USER_AGENT = "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0";
    JSONArray WHATSAPP_WEB_VERSION = new JSONArray().put(2).put(2035).put(14);

    class TAGS {
        public static final int LIST_EMPTY = 0,
                STREAM_END = 2,
                DICTIONARY_0 = 236,
                DICTIONARY_1 = 237,
                DICTIONARY_2 = 238,
                DICTIONARY_3 = 239,
                LIST_8 = 248,
                LIST_16 = 249,
                JID_PAIR = 250,
                HEX_8 = 251,
                BINARY_8 = 252,
                BINARY_20 = 253,
                BINARY_32 = 254,
                NIBBLE_8 = 255,
                SINGLE_BYTE_MAX = 256,
                PACKED_MAX = 254;
    }

    String[] DOUBLE_BYTE_TOKENS = {};

    String[] SINGLE_BYTE_TOKENS = {
            null,
            null,
            null,
            "200",
            "400",
            "404",
            "500",
            "501",
            "502",
            "action",
            "add",
            "after",
            "archive",
            "author",
            "available",
            "battery",
            "before",
            "body",
            "broadcast",
            "chat",
            "clear",
            "code",
            "composing",
            "contacts",
            "count",
            "create",
            "debug",
            "delete",
            "demote",
            "duplicate",
            "encoding",
            "error",
            "false",
            "filehash",
            "from",
            "g.us",
            "group",
            "groups_v2",
            "height",
            "id",
            "image",
            "in",
            "index",
            "invis",
            "item",
            "jid",
            "kind",
            "last",
            "leave",
            "live",
            "log",
            "media",
            "message",
            "mimetype",
            "missing",
            "modify",
            "name",
            "notification",
            "notify",
            "out",
            "owner",
            "participant",
            "paused",
            "picture",
            "played",
            "presence",
            "preview",
            "promote",
            "query",
            "raw",
            "read",
            "receipt",
            "received",
            "recipient",
            "recording",
            "relay",
            "remove",
            "response",
            "resume",
            "retry",
            "s.whatsapp.net",
            "seconds",
            "set",
            "size",
            "status",
            "subject",
            "subscribe",
            "t",
            "text",
            "to",
            "true",
            "type",
            "unarchive",
            "unavailable",
            "url",
            "user",
            "value",
            "web",
            "width",
            "mute",
            "read_only",
            "admin",
            "creator",
            "short",
            "update",
            "powersave",
            "checksum",
            "epoch",
            "block",
            "previous",
            "409",
            "replaced",
            "reason",
            "spam",
            "modify_tag",
            "message_info",
            "delivery",
            "emoji",
            "title",
            "description",
            "canonical-url",
            "matched-text",
            "star",
            "unstar",
            "media_key",
            "filename",
            "identity",
            "unread",
            "page",
            "page_count",
            "search",
            "media_message",
            "security",
            "call_log",
            "profile",
            "ciphertext",
            "invite",
            "gif",
            "vcard",
            "frequent",
            "privacy",
            "blacklist",
            "whitelist",
            "verify",
            "location",
            "document",
            "elapsed",
            "revoke_invite",
            "expiration",
            "unsubscribe",
            "disable",
            "vname",
            "old_jid",
            "new_jid",
            "announcement",
            "locked",
            "prop",
            "label",
            "color",
            "call",
            "offer",
            "call-id",
            "quick_reply",
            "sticker",
            "pay_t",
            "accept",
            "reject",
            "sticker_pack",
            "invalid",
            "canceled",
            "missed",
            "connected",
            "result",
            "audio",
            "video",
            "recent"
    };

    enum FileType {
        image("WhatsApp Image Keys", ""),
        video("WhatsApp Video Keys", "videoMessage"),
        document("WhatsApp Document Keys", "documentMessage"),
        audio("WhatsApp Audio Keys", "audioMessage"),
        sticker("WhatsApp Image Keys", "stickerMessage");
        private final String hkdfKey;
        private final String type;

        FileType(String hkdfKey, String type) {
            this.hkdfKey = hkdfKey;
            this.type = type;
        }

        public String getHkdfKey() {
            return hkdfKey;
        }

        public String getType() {
            return type;
        }
    }

    class MediaMap {
        public static final String IMAGE = "/mms/image", VIDEO = "/mms/video", DOCUMENT = "/mms/document",
                AUDIO = "/mms/audio", STICKER = "/mms/image";
    }

    class WAMetric {

        public static byte debugLog = 1,
                queryResume = 2,
                liveLocation = 3,
                queryMedia = 4,
                queryChat = 5,
                queryContact = 6,
                queryMessages = 7,
                presence = 8,
                presenceSubscribe = 9,
                group = 10,
                read = 11,
                chat = 12,
                received = 13,
                picture = 14,
                status = 15,
                message = 16,
                queryActions = 17,
                block = 18,
                queryGroup = 19,
                queryPreview = 20,
                queryEmoji = 21,
                queryVCard = 29,
                queryStatus = 30,
                queryStatusUpdate = 31,
                queryLiveLocation = 33,
                queryLabel = 36,
                queryQuickReply = 39;
    }

    class WAFlag {
        public static byte ignore = (byte) (1 << 7),
                acknowledge = 1 << 6,
                available = 1 << 5,
                unavailable = 1 << 4,
                expires = 1 << 3,
                skipOffline = 1 << 2;
    }

    class MimeType {
        public static String jpeg = "image/jpeg",
                png = "image/png",
                mp4 = "video/mp4",
                gif = "video/gif",
                pdf = "application/pdf",
                ogg = "audio/ogg; codecs=opus",
                mp4Audio = "audio/mp4",
                webp = "image/webp";
    }
}
