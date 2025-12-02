package com.github.netricecake.loco.packet.inbound.room;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class InfoLinkIn extends InboundPacket {

    private String name;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        name = jsonObject.get("ols").getAsJsonArray().get(0).getAsJsonObject().get("ln").getAsString();
    }

}
