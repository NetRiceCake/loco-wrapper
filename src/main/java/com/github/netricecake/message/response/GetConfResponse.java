package com.github.netricecake.message.response;

import com.github.netricecake.message.LocoResponse;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class GetConfResponse implements LocoResponse {

    private String addr;

    private int port;

    @Override
    public String getMethod() {
        return "GetConf";
    }

    @Override
    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        this.addr = jsonObject.get("ticket").getAsJsonObject().get("lsl").getAsJsonArray().get(0).getAsString();
        this.port = jsonObject.get("wifi").getAsJsonObject().get("ports").getAsJsonArray().get(0).getAsInt();
    }
}
