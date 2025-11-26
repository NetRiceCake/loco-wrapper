package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CheckInIn {

    private int status;

    private String host;

    private String host6;

    private int port;

    private String cshost;

    private String cshost6;

    private int csport;

    private String vsshost;

    private String vsshost6;

    private int vssport;

    private long cacheExpire;

    private String MCCMNC;

    public void fromBson(byte[] bson)
    {
        JsonObject json = BsonUtil.bsonToJsonObject(bson);
        status = json.get("status").getAsInt();
        host = json.get("host").getAsString();
        host6 = json.get("host6").getAsString();
        port = json.get("port").getAsInt();
        cshost = json.get("cshost").getAsString();
        cshost6 = json.get("cshost6").getAsString();
        csport = json.get("csport").getAsInt();
        vsshost = json.get("vsshost").getAsString();
        vsshost6 = json.get("vsshost6").getAsString();
        vssport = json.get("vssport").getAsInt();
        cacheExpire = json.get("cacheExpire").getAsLong();
        MCCMNC = json.get("MCCMNC").getAsString();
    }

}
