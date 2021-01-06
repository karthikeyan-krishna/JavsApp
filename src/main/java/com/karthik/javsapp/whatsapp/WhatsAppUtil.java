package com.karthik.javsapp.whatsapp;

import com.karthik.javsapp.common.Util;
import com.karthik.javsapp.crypto.CryptoUtil;
import org.apache.commons.codec.binary.Hex;

import java.util.Random;


public class WhatsAppUtil {
    public static String getClientId() {
        return CryptoUtil.base64EncodeToString(Util.randomBytes(16));
    }

    static String getMessageTag() {
        return Util.timestamp() + ".--" + WhatsApp.reqCount++;
    }

    static String getBinaryMessageTag() {
        if (WhatsApp.binaryMessageTag.equals("")) {
            WhatsApp.binaryMessageTag = (new Random().nextInt(900) + 100) + "";
        }
        return WhatsApp.binaryMessageTag + ".--" + WhatsApp.reqCount++;
    }

    static String toId(String phone) {
        if (phone.length() == 10) {
            phone = "91" + phone;
        }
        return phone.contains("@") ? phone : phone + "@s.whatsapp.net";
    }

    static String generateMessageID() {
        return Hex.encodeHexString(Util.randomBytes(10), false);
    }

}
