package com.github.netricecake.loco.packet.inbound.room;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ChatInfoIn extends InboundPacket {

    private String type;

    private JsonArray chatMetas;

    private JsonArray displayMembers;

    private long linkId = 0;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        type = jsonObject.get("chatInfo").getAsJsonObject().get("type").getAsString();
        try {
            linkId = jsonObject.get("chatInfo").getAsJsonObject().get("li").getAsLong();
        } catch (Exception e) {}
        try {
            chatMetas = jsonObject.get("chatInfo").getAsJsonObject().get("chatMetas").getAsJsonArray();
            displayMembers = jsonObject.get("chatInfo").getAsJsonObject().get("displayMembers").getAsJsonArray();
        } catch (Exception e) {}
    }

}
