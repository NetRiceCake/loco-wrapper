package com.github.netricecake.loco.packet.outbound.login;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;

public class GetConfOut {

    private String MCCMNC;

    private String os;

    private long userId;

    public GetConfOut(String MCCMNC, String os, long userId) {
        this.MCCMNC = MCCMNC;
        this.os = os;
        this.userId = userId;
    }

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("MCCMNC", MCCMNC);
        jsonObject.addProperty("os", os);
        jsonObject.addProperty("userId", userId);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
