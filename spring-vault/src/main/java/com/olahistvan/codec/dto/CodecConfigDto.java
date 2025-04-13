package com.olahistvan.codec.dto;

import java.util.Map;

public class CodecConfigDto {

    private String currentKeyId;
    private Map<String, String> keys;

    public String getCurrentKeyId() {
        return currentKeyId;
    }

    public void setCurrentKeyId(String currentKeyId) {
        this.currentKeyId = currentKeyId;
    }

    public Map<String, String> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, String> keys) {
        this.keys = keys;
    }
}
