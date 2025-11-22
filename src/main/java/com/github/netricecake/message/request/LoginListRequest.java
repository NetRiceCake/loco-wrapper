package com.github.netricecake.message.request;

import com.github.netricecake.KakaoApi;
import com.github.netricecake.message.LocoRequest;
import com.github.netricecake.util.BsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginListRequest implements LocoRequest {

    private String appVer = KakaoApi.VERSION;

    private String prtVer = "1";

    private String os = KakaoApi.AGENT;

    private String lang = "ko";

    private String duuid;

    private int ntype = 0; // 0 : WIFI, 3: Cellular

    private String MCCMNC = "45006"; // 앞자리 세자리(한국) 450 고정, 뒤에 두자리 SKT: 05 KT: 08 LGU+: 06   ex) 45006

    private int revision = 0; // TODO 이거뭐임

    private JsonArray chatIds = new JsonArray();

    private JsonArray maxIds = new JsonArray();

    private int lastTokenId = 0;

    private int lbk = 0; // TODO 이거 뭐임 2

    private JsonObject rp = new JsonObject(); // TODO 이거 뭐임 3

    private boolean bg = true; // TODO 이거 뭐임 4

    private String oauthToken;

    public LoginListRequest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("base64", "AAD//wAA");
        jsonObject.addProperty("subType", "00");
        rp.add("$binary", jsonObject);
    }

    @Override
    public String getMethod() {
        return "LOGINLIST";
    }

    @Override
    public byte[] toBson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("appVer", appVer);
        jsonObject.addProperty("prtVer", prtVer);
        jsonObject.addProperty("os", os);
        jsonObject.addProperty("lang", lang);
        jsonObject.addProperty("duuid", duuid);
        jsonObject.addProperty("ntype", ntype);
        jsonObject.addProperty("MCCMNC", MCCMNC);
        jsonObject.addProperty("revision", revision);
        jsonObject.add("chatIds", chatIds);
        jsonObject.add("maxIds", maxIds);
        jsonObject.addProperty("lastTokenId", lastTokenId);
        jsonObject.addProperty("lbk", lbk);
        jsonObject.add("rp", rp);
        jsonObject.addProperty("bg", bg);
        jsonObject.addProperty("oauthToken", oauthToken);

        return BsonUtil.jsonObjectToBson(jsonObject);
    }

}
