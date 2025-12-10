package com.github.netricecake.kakao.packet.inbound.member;

import com.github.netricecake.kakao.packet.InboundPacket;
import com.github.netricecake.kakao.util.BsonUtil;
import com.github.netricecake.kakao.structs.MemberType;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class MemberIn extends InboundPacket {

    private long userId;

    private int type;

    private String nickName;

    private String profileImageUrl;

    private String fullProfileImageUrl;

    private String originalProfileImageUrl;

    private long profileLinkId;

    private int memberType = MemberType.MEMBER;

    private int profileType;

    public MemberIn(byte[] bson) {
        JsonObject json = BsonUtil.bsonToJsonObject(bson).get("members").getAsJsonArray().get(0).getAsJsonObject();
        userId = json.get("userId").getAsLong();
        type = json.get("type").getAsInt();
        nickName = json.get("nickName").getAsString();
        profileImageUrl = json.get("pi").getAsString();
        fullProfileImageUrl = json.get("fpi").getAsString();
        originalProfileImageUrl = json.get("opi").getAsString();
        if (type == 1000) {
            memberType = json.get("mt").getAsInt();
            profileType = json.get("ptp").getAsInt();
            profileLinkId = profileType == 16 ? json.get("pli").getAsLong() : 0;
        }
    }

}
