package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ChatInfoIn extends InboundPacket {

    private String type;

    private long linkId;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        type = jsonObject.get("chatInfo").getAsJsonObject().get("type").getAsString();
        try {
            linkId = jsonObject.get("chatInfo").getAsJsonObject().get("li").getAsLong();
        } catch (Exception e) {}
    }

}
