package com.karthik.javsapp.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;

public class Util {
    public static byte[] randomBytes(int size) {
        Random rand = new Random();
        byte[] bytes = new byte[size];
        rand.nextBytes(bytes);
        return bytes;
    }

    public static long timestamp() {
        return Instant.now().getEpochSecond();
    }

    public static void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject encodeValidJson(String message) {
        String raw = message.replaceFirst("[,]", "##").split("##")[1];
        return new JSONObject(raw);
    }

    public static JSONArray encodeValidJsonArray(String message) {
        String raw = message.replaceFirst("[,]", "##").split("##")[1];
        return new JSONArray(raw);
    }

    public static byte[] concat(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(a);
        outputStream.write(b);
        return outputStream.toByteArray();
    }
}
