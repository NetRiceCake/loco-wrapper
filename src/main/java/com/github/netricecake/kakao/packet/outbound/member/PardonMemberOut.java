package com.github.netricecake.kakao.packet.outbound.member;

import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PardonMemberOut {

    private long chatId;

    private long linkId;

    private long memberId;

    public byte[] toBson() {
        JsonObject json = new JsonObject();
        json.addProperty("li", linkId);
        json.addProperty("c", chatId);
        json.addProperty("mid", memberId);
        return BsonUtil.jsonObjectToBson(json);
    }

}
