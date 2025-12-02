package com.github.netricecake.loco.packet.inbound.member;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class MemberIn extends InboundPacket {

    private String nickName;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        this.nickName = jsonObject.get("members").getAsJsonArray().get(0).getAsJsonObject().get("nickName").getAsString();
    }

}
