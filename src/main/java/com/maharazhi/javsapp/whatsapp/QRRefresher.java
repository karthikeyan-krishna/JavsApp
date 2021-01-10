package com.maharazhi.javsapp.whatsapp;

import com.maharazhi.javsapp.common.Util;

/**
 * This thread will be running unless the QR Code is scanned
 * If the old QR is expired, a nw QR will be generated
 */
public class QRRefresher extends Thread {

    private final WhatsApp client;
    private final String clientId;
    private final byte[] publicKey;

    QRRefresher(WhatsApp client, String clientId, byte[] publicKey) {
        this.clientId = clientId;
        this.publicKey = publicKey;
        this.client = client;
    }

    @Override
    public void run() {
        int count = 0;
        while (!client.isLoggedIn() && count != 5) {
            Util.waitMillis(20000);
            if (!client.isLoggedIn()) {
                client.requestNewServerId();
            }
            count++;
        }
        if (count == 5) {
            client.events.qrRefreshClosed(client);
        }
    }

    void newQRCode(String ref) {
        String message = QRGenerator.generateQRCode(clientId, ref, publicKey);
        client.events.qr(client, message);
    }
}
