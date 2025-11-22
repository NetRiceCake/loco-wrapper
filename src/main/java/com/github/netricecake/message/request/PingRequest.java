package com.github.netricecake.message.request;

import com.github.netricecake.message.LocoRequest;
import com.github.netricecake.util.BsonUtil;

public class PingRequest implements LocoRequest {


    @Override
    public String getMethod() {
        return "PING";
    }

    @Override
    public byte[] toBson() {
        return BsonUtil.jsonToBson("{}");
    }
}
