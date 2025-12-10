package com.github.netricecake.kakao.packet.outbound.member;

import com.github.netricecake.kakao.util.BsonUtil;
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
