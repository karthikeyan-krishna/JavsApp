package com.maharazhi.javsapp.crypto;

import com.google.protobuf.InvalidProtocolBufferException;
import com.maharazhi.javsapp.common.Constants;
import com.maharazhi.javsapp.proto.ProtoBuf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class BinaryDecoder {

    private byte[] buffer;
    private int index = 0;


    public JSONArray decode(byte[] buffer) {
        this.buffer = buffer;
        index = 0;
        return readNode();
    }

    private int unpackNibble(int value) {
        if (value >= 0 && value <= 9) {
            return (int) '0' + value;
        }
        switch (value) {
            case 10:
                return '-';
            case 11:
                return '.';
            case 15:
                return '\0';
        }
        return 0;
    }

    private int unpackHex(int value) {
        if (value >= 0 && value <= 15) {
            return value < 10 ? '0' + value : 'A' + value - 10;
        }

        System.err.println("Invalid hex: " + value);
        return 0;
    }

    private int unpackByte(int tag, int value) {
        if (tag == Constants.TAGS.NIBBLE_8) {
            return unpackNibble(value);
        } else if (tag == Constants.TAGS.HEX_8) {
            return unpackHex(value);
        } else {
            System.err.println("Unknown tag: " + tag);
        }
        return 0;
    }

    private int readInt(int n) {
        checkEOS(n);
        int val = 0;
        for (int i = 0; i < n; i++) {
            int shift = n - 1 - i;
            val |= next() << (shift * 8);
        }
        return val;
    }

    private int readInt20() {
        checkEOS(3);
        int a = next() & 0xff;
        int b = next() & 0xff;
        int c = next() & 0xff;
        return ((a & 15) << 16) + (b << 8) + c;
    }

    private String readPacked8(int tag) {
        byte startByte = readByte();
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < (startByte & 127); i++) {
            int curByte = readByte();

            int nibbleOne = unpackByte(tag, ((curByte & 0xf0)) >> 4);
            int nibbleSecond = unpackByte(tag, (curByte & 0x0f));

            value.append(String.valueOf(Character.toChars(nibbleOne)));
            value.append(String.valueOf(Character.toChars(nibbleSecond)));
        }

        if (startByte >> 7 != 0) {
            value = new StringBuilder(value.substring(0, value.length() - 1));
        }
        return value.toString();
    }

    private byte[] readBytes(int n) {
        checkEOS(n);
        byte[] byteArray = Arrays.copyOfRange(buffer, index, index + n);
        index += n;
        return byteArray;
    }

    private byte readByte() {
        checkEOS(1);
        return next();
    }

    private boolean isListTag(int tag) {
        return tag == Constants.TAGS.LIST_EMPTY || tag == Constants.TAGS.LIST_8 || tag == Constants.TAGS.LIST_16;
    }

    private int readListSize(int tag) {
        switch (tag) {
            case Constants.TAGS.LIST_EMPTY:
                return 0;
            case Constants.TAGS.LIST_8:
                return readByte();
            case Constants.TAGS.LIST_16:
                return readInt(2);
        }

        System.err.println("Invalid tag for list size: " + tag);
        return 0;
    }

    private String readStringFromCharacters(int length) {
        checkEOS(length);
        byte[] value = Arrays.copyOfRange(buffer, index, index + length);
        index += length;
        return new String(value);
    }

    private String getToken(int index) {
        if (index < 3 || index >= Constants.SINGLE_BYTE_TOKENS.length) {
            System.err.println("Invalid token index: " + index);
        }
        return Constants.SINGLE_BYTE_TOKENS[index];
    }

    private String getDoubleToken(int a, int b) {
        int n = a * 256 + b;
        if (n < 0 || n > Constants.DOUBLE_BYTE_TOKENS.length) {
            System.err.println("Invalid token index: " + index);
        }
        try {
            return Constants.DOUBLE_BYTE_TOKENS[n];
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String readString(int tag) {
        if (tag >= 3 && tag <= 235) {
            String token = getToken(tag);
            return token.equals("s.whatsapp.net") ? "c.us" : token;
        }
        switch (tag) {
            case Constants.TAGS.DICTIONARY_0:
            case Constants.TAGS.DICTIONARY_1:
            case Constants.TAGS.DICTIONARY_2:
            case Constants.TAGS.DICTIONARY_3:
                return getDoubleToken(tag - Constants.TAGS.DICTIONARY_0, readByte());
            case Constants.TAGS.LIST_EMPTY:
                return null;
            case Constants.TAGS.BINARY_8:
                return readStringFromCharacters(readByte());
            case Constants.TAGS.BINARY_20:
                return readStringFromCharacters(readInt20());
            case Constants.TAGS.BINARY_32:
                return readStringFromCharacters(readInt(4));
            case Constants.TAGS.JID_PAIR:
                String i = readString(readByte() & 0xff);
                String j = readString(readByte() & 0xff);
                if (i != null && j != null) {
                    return i + "@" + j;
                }
                System.err.println("Invalid jid pair: " + i + ", " + j);
            case Constants.TAGS.NIBBLE_8:
            case Constants.TAGS.HEX_8:
                return readPacked8(tag);
            default:
                System.err.println("Invalid tag: " + tag);
        }
        return null;
    }

    private JSONObject readAttributes(int n) {
        if (n != 0) {
            JSONObject attributeMap = new JSONObject();
            for (int i = 0; i < n; i++) {
                String key = readString(readByte() & 0xff);
                String value = readString(readByte() & 0xff);
                attributeMap.put(key, value);
            }
            return attributeMap;
        }
        return null;
    }

    private JSONArray readNode() {
        int listSize = readListSize(readByte() & 0xff);
        int descriptionTag = readByte() & 0xff;
        if (descriptionTag == Constants.TAGS.STREAM_END) {
            System.err.println("Unexpected stream end");
        }
        String description = readString(descriptionTag);
        if (listSize == 0 || description == null) {
            System.err.println("Invalid node");
        }
        JSONObject attrs = readAttributes((listSize - 1) >> 1);
        JSONArray contentArray = new JSONArray();
        if (listSize % 2 == 0) {
            int tag = readByte() & 0xff;
            if (isListTag(tag)) {
                contentArray = readList(tag);
            } else {
                String base64Decoded;
                try {
                    switch (tag) {
                        // Conversation messages
                        case Constants.TAGS.BINARY_8:
                            byte[] bin8 = readBytes(readByte() & 0xff);
                            base64Decoded = CryptoUtil.base64EncodeToString(
                                    ProtoBuf.WebMessageInfo.parseFrom(bin8).toByteArray());
                            break;
                        // Image, video, extended and rarely conversation messages
                        case Constants.TAGS.BINARY_20:
                            byte[] bin20 = readBytes(readInt20());
                            base64Decoded = CryptoUtil.base64EncodeToString(
                                    ProtoBuf.WebMessageInfo.parseFrom(bin20).toByteArray());
                            break;
                        // ?
                        case Constants.TAGS.BINARY_32:
                            byte[] bin32 = readBytes(readInt(4));
                            base64Decoded = CryptoUtil.base64EncodeToString(
                                    ProtoBuf.WebMessageInfo.parseFrom(bin32).toByteArray());
                            break;
                        default:
                            base64Decoded = readString(tag);
                            break;
                    }
                    contentArray = new JSONArray().put(base64Decoded);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return new JSONArray().put(description).put(attrs).put(contentArray);
    }

    private JSONArray readList(int tag) {
        int length = readListSize(tag);
        JSONArray array = new JSONArray();
        for (int i = 0; i < length; i++) {
            array.put(readNode());
        }
        return array;
    }

    private void checkEOS(int length) {
        if (index + length > buffer.length) {
            System.err.println("End of stream");
        }
    }

    private byte next() {
        return buffer[index++];
    }
}
