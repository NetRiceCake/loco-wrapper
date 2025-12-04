package com.github.netricecake.loco.packet.inbound.member;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class MemberIn extends InboundPacket {

    private long userId;

    private String nickName;

    private int memberType = 2;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        userId = jsonObject.get("members").getAsJsonArray().get(0).getAsJsonObject().get("userId").getAsLong();
        this.nickName = jsonObject.get("members").getAsJsonArray().get(0).getAsJsonObject().get("nickName").getAsString();
        try {
            memberType = jsonObject.get("members").getAsJsonArray().get(0).getAsJsonObject().get("mt").getAsInt();
        } catch (Exception e) {}
    }

}
