package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GetConfIn extends InboundPacket {

    private int status;

    private String addr;

    private int port;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        status = jsonObject.get("status").getAsInt();
        try {
            addr = jsonObject.get("ticket").getAsJsonObject().get("lsl").getAsJsonArray().get(0).getAsString();
            port = jsonObject.get("wifi").getAsJsonObject().get("ports").getAsJsonArray().get(0).getAsInt();
        } catch (Exception e) {}
    }

}
