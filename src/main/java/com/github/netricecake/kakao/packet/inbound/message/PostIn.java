package com.github.netricecake.kakao.packet.inbound.message;

import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class PostIn {

    private int status;

    private long o;

    public PostIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        this.status = jsonObject.get("status").getAsInt();
        this.o = jsonObject.get("o").getAsLong();
    }

}
