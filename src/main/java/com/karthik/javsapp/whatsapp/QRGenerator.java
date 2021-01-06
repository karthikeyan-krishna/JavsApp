package com.karthik.javsapp.whatsapp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.karthik.javsapp.crypto.CryptoUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

public class QRGenerator {
    public static BufferedImage generateQRCode(String clientId, String serverId, byte[] publicKey) {
        String base64PubKey = CryptoUtil.base64EncodeToString(publicKey);
        String qrCodeString = serverId + "," + base64PubKey + "," + clientId;
        return generateQRCode(qrCodeString);
    }

    private static BufferedImage generateQRCode(String input) {
        int size = 500;
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.MARGIN, 1);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(input, BarcodeFormat.QR_CODE, size, size, hintMap);
            int qrWidth = byteMatrix.getWidth();
            int qrHeight = byteMatrix.getHeight();
            BufferedImage image = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, qrWidth, qrHeight);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < qrWidth; i++) {
                for (int j = 0; j < qrWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            return image;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}