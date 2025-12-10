package com.github.netricecake.kakao.packet.outbound.room;

import com.github.netricecake.kakao.util.BsonUtil;
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
