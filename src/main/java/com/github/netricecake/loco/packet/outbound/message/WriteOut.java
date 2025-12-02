package com.github.netricecake.loco.packet.outbound.message;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;

@Getter
@Setter
public class WriteOut {

    private long chatId;
    private long msgId = new SecureRandom().nextLong();
    private String message;
    private int type = 1;
    private boolean noSeen = false;
    private String extra = "{}";
    private int scope = 1;

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chatId", chatId);
        jsonObject.addProperty("msgId", msgId);
        jsonObject.addProperty("msg", message);
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("noSeen", noSeen);
        jsonObject.addProperty("extra", extra);
        jsonObject.addProperty("scope", scope);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }
}
