package com.github.netricecake.kakao.loco;

import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocoPacket {

    private boolean raw = false;

    private int packetId;

    private short statusCode;

    private String method;

    private byte bodyType;

    private int bodyLength;

    private byte[] body;

    public LocoPacket(int packetId, short statusCode, String method, byte bodyType, int bodyLength, byte[] body) {
        this.packetId = packetId;
        this.statusCode = statusCode;
        this.method = method;
        this.bodyType = bodyType;
        this.bodyLength = bodyLength;
        this.body = body;
    }

    public LocoPacket(int packetId, String method, byte[] body) {
        this(packetId, (short) 0, method, (byte) 0, body.length, body);
    }

    public LocoPacket(String method, byte[] body) {
        this(-1, method, body);
    }

    public LocoPacket(int packetId, String method, JsonObject body) {
        this(packetId, method, BsonUtil.jsonObjectToBson(body));
    }

    public LocoPacket(String method, JsonObject body) {
        this(-1, method, BsonUtil.jsonObjectToBson(body));
    }

    public JsonObject getBodyJson() {
        return BsonUtil.bsonToJsonObject(body);
    }

}
