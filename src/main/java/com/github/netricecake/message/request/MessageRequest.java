package com.github.netricecake.message.request;

import com.github.netricecake.message.LocoRequest;
import com.github.netricecake.util.BsonUtil;

public class MessageRequest implements LocoRequest {
    @Override
    public String getMethod() {
        return "MSG";
    }

    @Override
    public byte[] toBson() {
        return BsonUtil.jsonToBson("{ notiRead: false }");
    }
}
