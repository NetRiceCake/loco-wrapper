package com.github.netricecake.message.response;

import com.github.netricecake.message.LocoResponse;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SetStatusResponse implements LocoResponse {

    private int status;

    @Override
    public String getMethod() {
        return "SETST";
    }

    @Override
    public void fromBson(byte[] bson) {
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        status = jsonObject.get("status").getAsInt();
    }
}
