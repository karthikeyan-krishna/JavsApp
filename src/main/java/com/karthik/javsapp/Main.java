package com.karthik.javsapp;

import com.karthik.javsapp.common.Constants;
import com.karthik.javsapp.common.Util;
import com.karthik.javsapp.security.WhatsAppCredentials;
import com.karthik.javsapp.whatsapp.WhatsApp;
import com.karthik.javsapp.whatsapp.WhatsAppEvents;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Main {
    static WhatsApp app;

    public static void main(String... args) {
        app = new WhatsApp() {
            @Override
            public void saveAuth(WhatsAppCredentials credentials) {
                Main.saveAuth(credentials);
            }

            @Override
            public WhatsAppCredentials loadAuth() {
                return Main.loadAuth();
            }
        };
        app.connect();
        app.setListener(new WhatsAppEvents() {
            @Override
            public void login(int httpCode) {
                if (httpCode == 200) {
                    System.out.println("Logged in successfully! Code: " + httpCode);
                } else {
                    System.out.println("Restore of previous session failed! Code: " + httpCode);
                }
            }

            @Override
            public void qr(BufferedImage img) {
                saveQRCode(img);
            }
        });
        while (!app.isLoggedIn()) {
            Util.waitMillis(1000);
        }
        for (int i = 0; i < 100; i++) {
            app.sendText("9150504488", "Test " + i + " after changes");
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(ImageIO.read(new File("qr.jpg")), "jpg", baos);
            app.uploadMedia("9150504488", baos.toByteArray(), "Caption", Constants.FileType.image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveQRCode(BufferedImage img) {
        try {
            ImageIO.write(img, "jpg", new File("qr.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveAuth(WhatsAppCredentials credentials) {
        try {
            FileWriter writer = new FileWriter("auth.json");
            writer.write(credentials.toJson().toString(2));
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static WhatsAppCredentials loadAuth() {
        try {
            File file = new File("auth.json");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                StringBuilder data = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }
                return new WhatsAppCredentials(new JSONObject(data.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
