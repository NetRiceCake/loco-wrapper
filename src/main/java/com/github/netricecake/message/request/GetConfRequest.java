package com.github.netricecake.message.request;

import com.github.netricecake.message.LocoRequest;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;

public class GetConfRequest implements LocoRequest {

    private String MCCMNC;

    private String os;

    private int userId;

    public GetConfRequest(String MCCMNC, String os, int userId) {
        this.MCCMNC = MCCMNC;
        this.os = os;
        this.userId = userId;
    }

    @Override
    public String getMethod() {
        return "GETCONF";
    }

    @Override
    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("MCCMNC", MCCMNC);
        jsonObject.addProperty("os", os);
        jsonObject.addProperty("userId", userId);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }
}
