package com.github.netricecake.kakao.packet.inbound.room;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.github.netricecake.kakao.structs.ChatRoomType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ChatInfoIn extends InboundPacket {

    private int status;

    private String type;

    private JsonArray chatMetas;

    private JsonArray displayMembers;

    private long linkId = 0;

    public ChatInfoIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        status = jsonObject.get("status").getAsInt();
        if (status == 0) {
            type = jsonObject.get("chatInfo").getAsJsonObject().get("type").getAsString();
            chatMetas = jsonObject.get("chatInfo").getAsJsonObject().get("chatMetas").getAsJsonArray();
            displayMembers = jsonObject.get("chatInfo").getAsJsonObject().get("displayMembers").getAsJsonArray();
            if (type.equals(ChatRoomType.OPEN_CHAT) || type.equals(ChatRoomType.OPEN_DIRECT)) {
                linkId = jsonObject.get("chatInfo").getAsJsonObject().get("li").getAsLong();
            }
        }
    }

}
