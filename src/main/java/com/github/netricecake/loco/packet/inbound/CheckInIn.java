package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CheckInIn extends InboundPacket {

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

    public CheckInIn(byte[] bson) {
        JsonObject json = BsonUtil.bsonToJsonObject(bson);
        status = getIntOrNull(json, "status");
        host = getStringOrNull(json, "host");
        host6 = getStringOrNull(json, "host6");
        port = getIntOrNull(json, "port");
        cshost = getStringOrNull(json, "cshost");
        cshost6 = getStringOrNull(json, "cshost6");
        csport = getIntOrNull(json, "csport");
        vsshost = getStringOrNull(json, "vsshost");
        vsshost6 = getStringOrNull(json, "vsshost6");
        vssport = getIntOrNull(json, "vssport");
        cacheExpire = getLongOrNull(json, "cacheExpire");
        MCCMNC = getStringOrNull(json, "MCCMNC");
    }

}
