package com.github.netricecake.kakao.packet.outbound.room;

import com.github.netricecake.kakao.util.BsonUtil;
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
