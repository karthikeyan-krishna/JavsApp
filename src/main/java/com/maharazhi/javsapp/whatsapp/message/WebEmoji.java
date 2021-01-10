package com.maharazhi.javsapp.whatsapp.message;

public class WebEmoji {

    private final String code;
    private final double value;

    public WebEmoji(String code, double value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public double getValue() {
        return value;
    }
}

