package com.github.netricecake.message.request;

import com.github.netricecake.KakaoApi;
import com.github.netricecake.kakao.KakaoDefaultValues;
import com.github.netricecake.message.LocoRequest;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequest implements LocoRequest {

    private int userId;

    private String os = KakaoDefaultValues.os;

    private int ntype = KakaoDefaultValues.ntype;

    private String appVer = KakaoApi.VERSION;

    private String lang = KakaoApi.LANGUAGE;

    private String MCCMNC = KakaoDefaultValues.MCCMNC;

    @Override
    public String getMethod() {
        return "CHECKIN";
    }

    @Override
    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("os", os);
        jsonObject.addProperty("ntype", ntype);
        jsonObject.addProperty("appVer", appVer);
        jsonObject.addProperty("lang", lang);
        jsonObject.addProperty("MCCMNC", MCCMNC);

        return BsonUtil.jsonObjectToBson(jsonObject);
    }
}
