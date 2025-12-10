package com.github.netricecake.kakao.packet.inbound.message;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class MessageIn extends InboundPacket {

    private long chatId;

    private long logId;

    private long authorId;

    private String authorNickname;

    private int type;

    private long sendAt;

    private String message;

    private String attachment;

    public MessageIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        chatId = jsonObject.get("chatId").getAsLong();
        logId = jsonObject.get("logId").getAsLong();
        authorId = jsonObject.get("chatLog").getAsJsonObject().get("authorId").getAsLong();
        try {
            authorNickname = jsonObject.get("authorNickname").getAsString(); // 옵챗 아니면 이필드가 없음;;
        } catch (Exception e) {}
        type = jsonObject.get("chatLog").getAsJsonObject().get("type").getAsInt();
        sendAt = jsonObject.get("chatLog").getAsJsonObject().get("sendAt").getAsLong();
        message = jsonObject.get("chatLog").getAsJsonObject().get("message").getAsString();
        try {
            attachment = jsonObject.get("chatLog").getAsJsonObject().get("attachment").getAsString();
        } catch (Exception e) {
            attachment = "";
        }
    }

}
