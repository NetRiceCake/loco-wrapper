package com.github.netricecake.kakao.packet.inbound.member;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class SyncLinkPfIn extends InboundPacket {

    private long chatId;

    private long linkId;

    private long userId;

    private int profileType;

    private String nickName;

    private String profileImageUrl;

    private String fullProfileImageUrl;

    private String originalProfileImageUrl;

    private long profileLinkId;

    public SyncLinkPfIn(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        chatId = jsonObject.get("c").getAsLong();
        linkId = jsonObject.get("li").getAsLong();
        JsonObject olu =  jsonObject.get("olu").getAsJsonObject();
        userId = olu.get("userId").getAsLong();
        profileType = olu.get("ptp").getAsInt();
        nickName = olu.get("nn").getAsString();
        profileImageUrl = olu.get("pi").getAsString();
        fullProfileImageUrl = olu.get("fpi").getAsString();
        originalProfileImageUrl = olu.get("opi").getAsString();
        profileLinkId = profileType == 16 ? olu.get("pli").getAsLong() : 0;
    }

}
