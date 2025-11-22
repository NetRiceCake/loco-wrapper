package com.github.netricecake.message.response;

import com.github.netricecake.message.LocoResponse;
import com.github.netricecake.util.BsonUtil;
import com.github.netricecake.util.ByteUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class CheckInResponse implements LocoResponse {

    private String host;

    private String host6;

    private int port;

    private String cshost;

    private String cshost6;

    private int csport;

    private String vshost;

    private String vshost6;

    private int vsport;

    private int cacheExpire;

    private String MCCMNC;

    @Override
    public String getMethod() {
        return "CHECKIN";
    }

    @Override
    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        this.host = jsonObject.get("host").getAsString();
        this.host6 = jsonObject.get("host6").getAsString();
        this.port = jsonObject.get("port").getAsInt();
        this.cshost = jsonObject.get("cshost").getAsString();
        this.cshost6 = jsonObject.get("cshost6").getAsString();
        this.csport = jsonObject.get("csport").getAsInt();
        this.vshost = jsonObject.get("vsshost").getAsString();
        this.vshost6 = jsonObject.get("vsshost6").getAsString();
        this.vsport = jsonObject.get("vssport").getAsInt();
        this.cacheExpire = jsonObject.get("cacheExpire").getAsInt();
        this.MCCMNC = jsonObject.get("MCCMNC").getAsString();
    }
}
