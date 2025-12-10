package com.github.netricecake.kakao.packet.inbound.room;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class InfoLinkIn extends InboundPacket {

    private String name;

    public InfoLinkIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        name = jsonObject.get("ols").getAsJsonArray().get(0).getAsJsonObject().get("ln").getAsString();
    }

}
