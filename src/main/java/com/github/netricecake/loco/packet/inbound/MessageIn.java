package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class MessageIn {

    private long chatId;

    private long logId;

    private long authorId;

    private String authorNickname;

    private int type;

    private String message;

    private String attachment;

    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        chatId = jsonObject.get("chatId").getAsLong();
        logId = jsonObject.get("logId").getAsLong();
        authorId = jsonObject.get("chatLog").getAsJsonObject().get("authorId").getAsLong();
        try {
            authorNickname = jsonObject.get("authorNickname").getAsString(); // 옵챗 아니면 이필드가 없음;;
        } catch (Exception e) {}
        type = jsonObject.get("chatLog").getAsJsonObject().get("type").getAsInt();
        message = jsonObject.get("chatLog").getAsJsonObject().get("message").getAsString();
        try {
            attachment = jsonObject.get("chatLog").getAsJsonObject().get("attachment").getAsString();
        } catch (Exception e) {
            attachment = "";
        }
    }

}
