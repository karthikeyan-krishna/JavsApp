package com.maharazhi.javsapp.whatsapp;

import com.maharazhi.javsapp.common.Util;
import com.maharazhi.javsapp.crypto.CryptoUtil;
import org.apache.commons.codec.binary.Hex;

import java.util.Random;

/**
 * Methods supporting the WhatsApp transactions
 */
public interface WhatsAppUtil {
    static String getClientId() {
        return CryptoUtil.base64EncodeToString(Util.randomBytes(16));
    }

    static String getMessageTag() {
        return Util.timestamp() + ".--" + WhatsApp.reqCount++;
    }

    static String getBinaryMessageTag() {
        if (WhatsApp.binaryMessageTag.equals("")) {
            WhatsApp.binaryMessageTag = String.valueOf((new Random().nextInt(900) + 100));
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
