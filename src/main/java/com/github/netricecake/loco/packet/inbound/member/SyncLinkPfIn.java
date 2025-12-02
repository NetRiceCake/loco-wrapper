package com.github.netricecake.loco.packet.inbound.member;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

@Getter
public class SyncLinkPfIn extends InboundPacket {

    private long chatId;

    private long linkId;

    private long userId;

    private String nickName;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        chatId = jsonObject.get("c").getAsLong();
        linkId = jsonObject.get("li").getAsLong();
        JsonObject olu =  jsonObject.get("olu").getAsJsonObject();
        userId = olu.get("userId").getAsLong();
        nickName = olu.get("nn").getAsString();
    }

}
