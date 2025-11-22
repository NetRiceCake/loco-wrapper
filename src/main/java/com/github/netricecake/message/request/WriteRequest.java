package com.github.netricecake.message.request;

import com.github.netricecake.message.LocoRequest;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WriteRequest implements LocoRequest {

    private long chatId;
    private long msgId; // 이거 뭐임???
    private String message;
    private int type = 1;
    private boolean noSeen = false;
    private String extra = "{}";
    private int scope = 1;

    public WriteRequest() {
        msgId = (long) Math.ceil(Math.random() * 99999999);
    }

    @Override
    public String getMethod() {
        return "WRITE";
    }

    @Override
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
