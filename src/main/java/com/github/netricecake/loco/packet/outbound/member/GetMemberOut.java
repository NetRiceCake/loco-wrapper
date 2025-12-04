package com.github.netricecake.loco.packet.outbound.member;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;

public class GetMemberOut {

    private long chatId;

    public GetMemberOut(long chatId) {
        this.chatId = chatId;
    }

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chatId", chatId);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
