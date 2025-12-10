package com.github.netricecake.kakao.packet.outbound.message;

import com.github.netricecake.kakao.util.BsonUtil;
import com.github.netricecake.kakao.structs.MessageType;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;

@Getter
@Setter
public class WriteOut {

    private long chatId;

    private long msgId = new SecureRandom().nextLong();

    private String message;

    private int type = MessageType.TEXT;

    private boolean noSeen = false;

    private String extra = "{}";

    private int scope = 1;

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chatId", chatId);
        jsonObject.addProperty("msgId", msgId);
        jsonObject.addProperty("msg", message);
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("noSeen", noSeen);
        jsonObject.addProperty("extra", extra);
        jsonObject.addProperty("scope", scope);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }
}
