package com.github.netricecake.network;

import lombok.Getter;
import lombok.Setter;

public class LocoPacket {

    @Getter
    private final int packetId;

    @Getter
    private final short statusCode;

    @Getter
    private String method;

    @Getter
    private final byte bodyType;

    @Getter
    private int bodyLength;

    @Getter
    @Setter
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

}
