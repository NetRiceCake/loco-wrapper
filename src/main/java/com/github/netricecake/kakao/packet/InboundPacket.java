package com.github.netricecake.kakao.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class InboundPacket {

    public String getStringOrNull(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? null : jsonElement.getAsString();
    }

    public int getIntOrNull(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? 0 : jsonElement.getAsInt();
    }

    public boolean getBoolOrNull(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null || jsonElement.getAsBoolean();
    }

    public long getLongOrNull(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement == null ? 0L : jsonElement.getAsLong();
    }

}
