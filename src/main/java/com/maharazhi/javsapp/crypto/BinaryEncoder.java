package com.maharazhi.javsapp.crypto;

import com.maharazhi.javsapp.common.Constants;
import com.maharazhi.javsapp.proto.ProtoBuf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Binary Encoder
 * This implementation is taken from Baileys
 * https://github.com/adiwajshing/Baileys
 */
public class BinaryEncoder {

    private List<Byte> data;

    public byte[] encode(JSONArray buffer) {
        data = new ArrayList<>();
        writeNode(buffer);
        byte[] dataArray = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dataArray[i] = data.get(i);
        }
        return dataArray;
    }

    private void writeNode(JSONArray node) {
        if (node != null) {
            if (node.length() != 3) {
                System.err.println("Invalid node: " + node);
            } else {
                Object children = node.get(2);
                Object obj = node.get(1);
                JSONObject jsonAttributes = null;
                Set<String> validAttributes = null;
                int jsonAttributesNum = 0;
                if (obj != JSONObject.NULL) {
                    jsonAttributes = node.getJSONObject(1);
                    validAttributes = jsonAttributes.keySet();
                    jsonAttributesNum = jsonAttributes.length();
                }
                writeListStart(2 * jsonAttributesNum + 1 + (children != null ? 1 : 0));
                writeString(node.getString(0), false);
                obj = node.opt(1);
                if (obj != JSONObject.NULL && validAttributes != null) {
                    writeAttributes(jsonAttributes, validAttributes);
                }
                writeChildren(children);
            }
        }
    }

    private void writeChildren(Object children) {
        if (children == null) {
            return;
        }
        if (children instanceof String) {
            writeString((String) children, true);
        } else if (children instanceof JSONArray) {
            writeListStart(((JSONArray) children).length());
            for (Object element : (JSONArray) children) {
                if (element != null) {
                    writeNode((JSONArray) element);
                }
            }
        } else if (children instanceof ProtoBuf.WebMessageInfo) {
            byte[] message = ((ProtoBuf.WebMessageInfo) children).toByteArray();
            int[] intMessage = new int[message.length];
            for (int i = 0; i < message.length; i++) {
                intMessage[i] = message[i] & 0xff;
            }
            writeByteLength(intMessage.length);
            pushBytes(intMessage);
        } else if (children instanceof byte[]) {
            byte[] message = (byte[]) children;
            int[] intMessage = new int[message.length];
            for (int i = 0; i < message.length; i++) {
                intMessage[i] = message[i] & 0xff;
            }
            writeByteLength(intMessage.length);
            pushBytes(intMessage);
        }
    }

    private void writeAttributes(JSONObject attrs, Set<String> keys) {
        for (String key : keys) {
            writeString(key, false);
            writeString(attrs.optString(key, null), false);
        }
    }

    private void writeByteLength(int length) {
        if (length >= (1 << 20)) {
            pushByte(Constants.TAGS.BINARY_32);
            pushInt(length, 4, false);
        } else if (length >= 256) {
            pushByte(Constants.TAGS.BINARY_20);
            pushInt20(length);
        } else {
            pushByte(Constants.TAGS.BINARY_8);
            pushByte(length);
        }
    }

    private void writeStringRaw(String str) {
        writeByteLength(str.length());
        pushString(str);
    }

    private void writeJid(String left, String right) {
        pushByte(Constants.TAGS.JID_PAIR);
        if (left != null && left.length() > 0) {
            writeString(left, false);
        } else {
            writeToken(Constants.TAGS.LIST_EMPTY);
        }
        writeString(right, false);
    }

    private void writeListStart(int listSize) {
        if (listSize == 0) {
            pushByte(Constants.TAGS.LIST_EMPTY);
        } else if (listSize < 256) {
            pushBytes(new int[]{Constants.TAGS.LIST_8, listSize});
        } else {
            pushBytes(new int[]{Constants.TAGS.LIST_16, listSize});
        }
    }

    private void writeString(String token, boolean i) {
        if (token != null && token.equals("c.us")) {
            token = "s.whatsapp.net";
        }

        int tokenIndex = 0;
        for (int t = 0; t < Constants.SINGLE_BYTE_TOKENS.length; t++) {
            if (Constants.SINGLE_BYTE_TOKENS[t] == null && token == null) {
                tokenIndex = t;
                break;
            } else if (Constants.SINGLE_BYTE_TOKENS[t] != null) {
                if (Constants.SINGLE_BYTE_TOKENS[t].equals(token)) {
                    tokenIndex = t;
                    break;
                }
            }
        }

        if (!i && token != null && token.equals("s.whatsapp.net")) {
            writeToken(tokenIndex);
        } else if (tokenIndex > 0) {
            writeToken(tokenIndex);
        } else if (token != null) {
            int jidSepIndex = token.indexOf('@');
            if (jidSepIndex <= 0) {
                writeStringRaw(token);
            } else {
                writeJid(token.substring(0, jidSepIndex), token.substring(jidSepIndex + 1));
            }
        }
    }

    private void writeToken(int token) {
        if (token < 245) {
            pushByte(token);
        } else if (token <= 500) {
            System.err.println("Invalid token");
        }
    }

    private void pushByte(int value) {
        data.add((byte) (value & 0xff));
    }

    private void pushBytes(int[] intArray) {
        for (int i : intArray) {
            data.add((byte) i);
        }
    }

    private void pushString(String str) {
        byte[] byteArray = str.getBytes();
        int[] intArray = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = byteArray[i] & 0xff;
        }
        pushBytes(intArray);
    }

    private void pushInt(int value, int n, boolean littleEndian) {
        for (int i = 0; i < n; n++) {
            int curShift = littleEndian ? i : n - 1 - i;
            data.add((byte) ((value >> (curShift * 8)) & 0xff));
        }
    }

    private void pushInt20(int value) {
        pushBytes(new int[]{(value >> 16) & 0x0f, (value >> 8) & 0xff, value & 0xff});
    }
}
