package com.github.netricecake.loco.packet.outbound.member;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberOut {

    private long chatId;

    private long memberId;

    public MemberOut(long chatId, long memberId) {
        this.chatId = chatId;
        this.memberId = memberId;
    }

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chatId", chatId);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(memberId);
        jsonObject.add("memberIds", jsonArray);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
