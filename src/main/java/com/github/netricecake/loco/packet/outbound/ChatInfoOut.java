package com.github.netricecake.loco.packet.outbound;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;

public class ChatInfoOut {

    private long chatId;

    public ChatInfoOut(long chatId) {
        this.chatId = chatId;
    }

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chatId", chatId);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
