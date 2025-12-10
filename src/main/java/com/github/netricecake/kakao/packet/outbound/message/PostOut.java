package com.github.netricecake.kakao.packet.outbound.message;

import com.github.netricecake.kakao.KakaoApi;
import com.github.netricecake.kakao.util.BsonUtil;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;

@Getter
@Setter
public class PostOut {

    private long userId;

    private String key;

    private int type = 2;

    private long size;

    private long chatId;

    private long msgId = new SecureRandom().nextLong();

    private long width;

    private long height;

    private String MCCMNC = KakaoApi.MCCMNC;

    private int nType = KakaoApi.NETWORK_TYPE;

    private String os = KakaoApi.AGENT;

    private String version = KakaoApi.VERSION;

    private String ex = "{\"cmt\":\"\"}";

    private boolean ns = false;

    private long dt = 4;

    private long scp = 1;

    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("u", userId);
        jsonObject.addProperty("k", key);
        jsonObject.addProperty("t", type);
        jsonObject.addProperty("s", size);
        jsonObject.addProperty("c", chatId);
        jsonObject.addProperty("mid", msgId);
        jsonObject.addProperty("w", width);
        jsonObject.addProperty("h", height);
        jsonObject.addProperty("mm", MCCMNC);
        jsonObject.addProperty("nt", nType);
        jsonObject.addProperty("os", os);
        jsonObject.addProperty("av", version);
        jsonObject.addProperty("ex", ex);
        jsonObject.add("f", null);
        jsonObject.add("sp", null);
        jsonObject.addProperty("ns", ns);
        jsonObject.addProperty("dt", dt);
        jsonObject.addProperty("scp", scp);

        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
