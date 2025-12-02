package com.github.netricecake.loco.packet.outbound.message;

import com.github.netricecake.loco.util.BsonUtil;

public class MessageOut {

    public byte[] toBson() {
        return BsonUtil.jsonToBson("{ \"notiRead\": false }");
    }

}
