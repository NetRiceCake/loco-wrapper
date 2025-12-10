package com.github.netricecake.kakao.packet.inbound.message;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ShipIn extends InboundPacket {

    private int status;

    private String key;

    private String vhost;

    private String vhost6;

    private int port;

    public ShipIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        status = jsonObject.get("status").getAsInt();
        if (status != 0) return;
        key = jsonObject.get("k").getAsString();
        vhost = jsonObject.get("vh").getAsString();
        vhost6 = jsonObject.get("vh6").getAsString();
        port = jsonObject.get("p").getAsInt();
    }

}
