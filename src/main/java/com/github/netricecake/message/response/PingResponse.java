package com.github.netricecake.message.response;

import com.github.netricecake.message.LocoResponse;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class PingResponse implements LocoResponse {

    private int status;

    @Override
    public String getMethod() {
        return "PING";
    }

    @Override
    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        this.status = jsonObject.get("status").getAsInt();
    }
}
