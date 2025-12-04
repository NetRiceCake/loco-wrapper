package com.github.netricecake.loco.packet.inbound.member;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class GetMemberIn extends InboundPacket {

    private int status;

    private JsonArray members;

    public void fromBson(byte[] bson) {
        JsonObject json = BsonUtil.bsonToJsonObject(bson);
        status = json.get("status").getAsInt();
        if (status == 0) members = json.get("members").getAsJsonArray();
    }

}
