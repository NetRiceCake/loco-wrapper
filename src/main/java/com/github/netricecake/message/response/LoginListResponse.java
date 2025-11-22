package com.github.netricecake.message.response;

import com.github.netricecake.message.LocoResponse;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class LoginListResponse implements LocoResponse {

    private int status;

    @Override
    public String getMethod() {
        return "LOGINLIST";
    }

    @Override
    public void fromBson(byte[] bson) {
        // 너무 많음;;
        JsonObject jsonObject = BsonUtil.bsonToJsonObject(bson);
        this.status = jsonObject.get("status").getAsInt();
    }

}
