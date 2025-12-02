package com.github.netricecake.loco.packet.inbound.login;

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

    private long revision;

    private String revisionInfo;

    private byte[] rp;

    private long minLogId;

    private long sb;

    private JsonArray chatDatas;

    private JsonArray delChatIds;

    private JsonArray kc;

    private long mcmRevision;

    private long lastTokenId;

    private long lastChatId;

    private long ltk;

    private long lbk;

    private boolean eof;

    public void fromBson(byte[] bson) {
        JsonObject json = BsonUtil.bsonToJsonObject(bson);
        status = json.get("status").getAsInt();
        userId = getLongOrNull(json, "userId");
        revision = getLongOrNull(json, "revision");
        revisionInfo = getStringOrNull(json, "revisionInfo");
        minLogId = getLongOrNull(json, "minLogId");
        sb = getLongOrNull(json, "sb");
        mcmRevision = getLongOrNull(json, "mcmRevision");
        lastTokenId = getLongOrNull(json, "lastTokenId");
        lastChatId = getLongOrNull(json, "lastChatId");
        ltk = getLongOrNull(json, "ltk");
        lbk = getLongOrNull(json, "lbk");
        eof = getBoolOrNull(json, "eof");

        if (status == 0) {
            rp = Base64.getDecoder().decode(json.get("rp").getAsJsonObject().get("$binary").getAsJsonObject().get("base64").getAsString());
            chatDatas = json.get("chatDatas").getAsJsonArray();
            delChatIds = json.get("delChatIds").getAsJsonArray();
            kc = json.get("kc").getAsJsonArray();
        }
    }

}
