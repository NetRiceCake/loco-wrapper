package com.github.netricecake.kakao.packet.inbound.member;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

@Getter
public class DelMemIn extends InboundPacket {

    private long chatId;

    private long userId;

    private String nickname;

    public DelMemIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson).get("chatLog").getAsJsonObject();
        chatId = jsonObject.get("chatId").getAsLong();
        userId = jsonObject.get("authorId").getAsLong();
        JsonObject msgObject = JsonParser.parseString(jsonObject.get("message").getAsString()).getAsJsonObject();
        nickname = msgObject.get("member").getAsJsonObject().get("nickName").getAsString();
    }

}
