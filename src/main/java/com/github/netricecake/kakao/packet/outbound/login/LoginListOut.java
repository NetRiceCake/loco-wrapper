package com.github.netricecake.kakao.packet.outbound.login;

import com.github.netricecake.kakao.KakaoApi;
import com.github.netricecake.kakao.util.BsonUtil;
import com.github.netricecake.kakao.util.ByteUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
public class LoginListOut {

    private String appVer = KakaoApi.VERSION;

    private String os = KakaoApi.AGENT;

    private String lang = KakaoApi.LANGUAGE;

    private String duuid;

    private int ntype = KakaoApi.NETWORK_TYPE;

    private String MCCMNC =  KakaoApi.MCCMNC;

    private int revision = 0;

    private int dtype = 2;

    private int pcst = 1;

    private JsonArray chatIds = new JsonArray();

    private JsonArray maxIds = new JsonArray();

    private long lastTokenId = 0;

    private long lbk = 0;

    private byte[] rp = ByteUtil.hexStringToByteArray("0000ffff0000");; // 이거 뭐임

    private boolean bg = true;

    private String oauthToken;

    public byte[] toBson() {
        JsonObject rpObject  = new JsonObject();
        JsonObject binary = new JsonObject();
        binary.addProperty("base64", new String(Base64.getEncoder().encode(rp)));
        binary.addProperty("subType", "00");
        rpObject.add("$binary", binary);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("appVer", appVer);
        resultObject.addProperty("os", os);
        resultObject.addProperty("lang", lang);
        resultObject.addProperty("duuid", duuid);
        resultObject.add("sKey", JsonNull.INSTANCE);
        resultObject.addProperty("oauthToken", oauthToken);
        resultObject.addProperty("ntype", ntype);
        resultObject.addProperty("MCCMNC", MCCMNC);
        resultObject.addProperty("revision", revision);
        resultObject.addProperty("dtype", dtype);
        resultObject.addProperty("pcst", pcst);
        resultObject.add("rp", rpObject);
        resultObject.addProperty("bg", bg);
        resultObject.add("chatIds", chatIds);
        resultObject.add("maxIds", maxIds);
        resultObject.addProperty("lastTokenId", lastTokenId);
        resultObject.addProperty("lbk", lbk);

        return BsonUtil.jsonObjectToBson(resultObject);
    }


}
