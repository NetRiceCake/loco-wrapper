package com.github.netricecake.message.request;

import com.github.netricecake.message.LocoRequest;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetStatusRequest implements LocoRequest {

    /*
        카톡 켜고 있는지 안켜고 있는지 알리는 패킷인듯
     */

    private int status; // 1 : 본다, 2 : 안본다

    public SetStatusRequest(int atatus) {
        this.status = atatus;
    }

    @Override
    public String getMethod() {
        return "SETST";
    }

    @Override
    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("st", status);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }
}
