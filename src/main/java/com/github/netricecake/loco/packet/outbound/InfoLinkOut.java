package com.github.netricecake.loco.packet.outbound;

import com.github.netricecake.loco.util.BsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class InfoLinkOut {

    private long linkId;

    public InfoLinkOut(long linkId) {
        this.linkId = linkId;
    }

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(linkId);
        jsonObject.add("lis", jsonArray);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
