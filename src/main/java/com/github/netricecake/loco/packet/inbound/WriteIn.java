package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class WriteIn extends InboundPacket {

    private int status;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        status = jsonObject.get("status").getAsInt();
    }

}
