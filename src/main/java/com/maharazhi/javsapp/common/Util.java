package com.maharazhi.javsapp.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;

/**
 * All the Common Util methods are defined in this Interface
 */
public interface Util {
    /**
     * Generate a byte array containing random bytes with the size mentioned
     *
     * @param size Size of the byte array
     * @return Byte Array of the given size
     */
    static byte[] randomBytes(int size) {
        Random rand = new Random();
        byte[] bytes = new byte[size];
        rand.nextBytes(bytes);
        return bytes;
    }

    /**
     * Get Current Timestamp
     *
     * @return Timestamp in Seconds
     */
    static long timestamp() {
        return Instant.now().getEpochSecond();
    }

    /**
     * Implemented Thread.sleep and handled the Exception
     *
     * @param millis milliseconds
     */
    static void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * WhatsApp Websocket messages will be containing a Tag and then followed by the message
     * To obtain the Object alone, this method is used
     * This maybe a JSONObject JSONArray or a simple String
     *
     * @param message Output from WhatsApp WebSocket
     * @return Object generated from the message
     */
    static JSONObject encodeValid(String message) {
        String[] split = message.replaceFirst("[,]", "##").split("##");
        String raw = split[1];
        return new JSONObject().put("tag", split[0])
                .put("raw", raw.startsWith("{") ? new JSONObject(raw) : raw.startsWith("[") ? new JSONArray(raw) : raw);
    }

    /**
     * Concat two Byte Arrays
     *
     * @param a First Byte Array
     * @param b Second Byte Array
     * @return Concat Byte Array
     * @throws IOException Exception from Stream
     */
    static byte[] concat(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(a);
        outputStream.write(b);
        return outputStream.toByteArray();
    }
}
