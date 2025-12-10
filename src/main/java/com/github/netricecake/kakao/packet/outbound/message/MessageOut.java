package com.github.netricecake.kakao.packet.outbound.message;

import com.github.netricecake.kakao.util.BsonUtil;

public class MessageOut {

    public byte[] toBson() {
        return BsonUtil.jsonToBson("{ \"notiRead\": false }");
    }

}
