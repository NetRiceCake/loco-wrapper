package com.github.netricecake.kakao.packet.outbound.member;

import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KickMemberOut {

    private long chatId;

    private long linkId;

    private long memberId;

    public KickMemberOut(long chatId, long linkId, long memberId) {
        this.chatId = chatId;
        this.linkId = linkId;
        this.memberId = memberId;
    }

    public byte[] toBson() {
        JsonObject json = new JsonObject();
        json.addProperty("li", linkId);
        json.addProperty("c", chatId);
        json.addProperty("mid", memberId);
        json.addProperty("r", false);
        return BsonUtil.jsonObjectToBson(json);
    }
}
