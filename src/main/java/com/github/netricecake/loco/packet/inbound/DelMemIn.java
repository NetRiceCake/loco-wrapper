package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

@Getter
public class DelMemIn {

    private long chatId;

    private long userId;

    private String nickname;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson).get("chatLog").getAsJsonObject();
        chatId = jsonObject.get("chatId").getAsLong();
        userId = jsonObject.get("authorId").getAsLong();
        JsonObject msgObject = JsonParser.parseString(jsonObject.get("message").getAsString()).getAsJsonObject();
        nickname = msgObject.get("member").getAsJsonObject().get("nickName").getAsString();
    }

}
