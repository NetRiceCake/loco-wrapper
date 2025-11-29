package com.github.netricecake.loco.packet.inbound;

import com.github.netricecake.loco.packet.InboundPacket;
import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.Base64;

@Getter
public class LoginListIn extends InboundPacket {

    private int status;

    private long userId;

    private int revision;

    private String revisionInfo;

    private byte[] rp;

    private long minLogId;

    private int sb;

    private JsonArray chatDatas;

    private JsonArray delChatIds;

    private JsonArray kc;

    private int mcmRevision;

    private long lastTokenId;

    private long lastChatId;

    private long ltk;

    private long lbk;

    private boolean eof;

    public void fromBson(byte[] bson) {
        JsonObject json = BsonUtil.bsonToJsonObject(bson);
        status = json.get("status").getAsInt();
        try {
            userId = json.get("userId").getAsLong();
            revision = json.get("revision").getAsInt();
            revisionInfo = json.get("revisionInfo").getAsString();
            rp = Base64.getDecoder().decode(json.get("rp").getAsJsonObject().get("$binary").getAsJsonObject().get("base64").getAsString());
            minLogId = json.get("minLogId").getAsLong();
            sb = json.get("sb").getAsInt();
            chatDatas = json.get("chatDatas").getAsJsonArray();
            delChatIds = json.get("delChatIds").getAsJsonArray();
            kc = json.get("kc").getAsJsonArray();
            mcmRevision = json.get("mcmRevision").getAsInt();
            lastTokenId = json.get("lastTokenId").getAsLong();
            lastChatId = json.get("lastChatId").getAsLong();
            ltk = json.get("ltk").getAsLong();
            lbk = json.get("lbk").getAsLong();
            eof = json.get("eof").getAsBoolean();
        } catch(Exception e) {}
    }

}
