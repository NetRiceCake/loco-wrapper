package com.github.netricecake.kakao.packet.outbound.login;

import com.github.netricecake.kakao.KakaoApi;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInOut {

    private long userId;

    private String os = KakaoApi.AGENT;

    private int ntype = KakaoApi.NETWORK_TYPE;

    private String appVer = KakaoApi.VERSION;

    private String lang =  KakaoApi.LANGUAGE;

    private String MCCMNC = KakaoApi.MCCMNC;

    public CheckInOut(long userId) {
        this.userId = userId;
    }

    public byte[] toBson() {
        JsonObject checkInObject = new JsonObject();
        checkInObject.addProperty("userId", userId);
        checkInObject.addProperty("os", os);
        checkInObject.addProperty("ntype", ntype);
        checkInObject.addProperty("appVer", appVer);
        checkInObject.addProperty("lang", lang);
        checkInObject.addProperty("MCCMNC", MCCMNC);

        return BsonUtil.jsonObjectToBson(checkInObject);
    }

}
