package com.github.netricecake.loco.packet.inbound.message;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class PostIn {

    private int status;

    private long o;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        this.status = jsonObject.get("status").getAsInt();
        this.o = jsonObject.get("o").getAsLong();
    }

}
