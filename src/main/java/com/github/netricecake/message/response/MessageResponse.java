package com.github.netricecake.message.response;

import com.github.netricecake.message.LocoResponse;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class MessageResponse implements LocoResponse {

    private long chatId;
    private long logId;
    private int type;
    private long authorId;
    private String message;

    @Override
    public String getMethod() {
        return "MSG";
    }

    @Override
    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        chatId = jsonObject.get("chatId").getAsLong();
        logId = jsonObject.get("logId").getAsLong();
        type = jsonObject.get("chatLog").getAsJsonObject().get("type").getAsInt();
        message = jsonObject.get("chatLog").getAsJsonObject().get("message").getAsString();
    }
}
