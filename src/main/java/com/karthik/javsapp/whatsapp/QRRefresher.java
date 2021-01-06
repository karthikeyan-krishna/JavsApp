package com.karthik.javsapp.whatsapp;

import com.karthik.javsapp.common.Util;

import java.awt.image.BufferedImage;

public class QRRefresher {

    private final WhatsApp client;
    private final String clientId;
    private final byte[] publicKey;

    private boolean scanned = false;

    public QRRefresher(WhatsApp client, String clientId, byte[] publicKey) {
        this.clientId = clientId;
        this.publicKey = publicKey;
        this.client = client;
    }

    public void start() {
        new Thread(() -> {
            int run = 0;
            while (!scanned && run != 5) {
                Util.waitMillis(20000);
                if (!scanned) {
                    client.requestNewServerId();
                }
                run++;
            }
        }).start();
    }

    public void newQRCode(String message) {
        String serverId = Util.encodeValidJson(message).getString("ref");
        BufferedImage img = QRGenerator.generateQRCode(clientId, serverId, publicKey);
        client.whatsAppEvents.qr(img);
    }

    public void setQRCodeScanned(boolean scanned) {
        this.scanned = scanned;
    }
}
