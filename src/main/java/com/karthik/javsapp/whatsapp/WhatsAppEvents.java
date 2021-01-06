package com.karthik.javsapp.whatsapp;

import java.awt.image.BufferedImage;

public interface WhatsAppEvents {
    void login(int httpCode);

    void qr(BufferedImage img);
}
