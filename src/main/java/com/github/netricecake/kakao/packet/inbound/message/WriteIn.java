package com.github.netricecake.kakao.packet.inbound.message;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class WriteIn extends InboundPacket {

    private int status;

    public WriteIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        status = jsonObject.get("status").getAsInt();
    }

}
