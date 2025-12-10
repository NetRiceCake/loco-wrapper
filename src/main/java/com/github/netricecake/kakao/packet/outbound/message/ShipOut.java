package com.github.netricecake.kakao.packet.outbound.message;

import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipOut {

    private long chatId;

    private long size;

    private int type = 2;

    private String checkSum;

    private String extension = "jpg";

    private String extensions = "{}";

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("c", chatId);
        jsonObject.addProperty("s", size);
        jsonObject.addProperty("t", type);
        jsonObject.addProperty("cs", checkSum);
        jsonObject.addProperty("e", extension);
        jsonObject.addProperty("ex", extensions);
        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
