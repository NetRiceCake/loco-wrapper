package com.github.netricecake.kakao.packet.outbound.etc;

import com.github.netricecake.kakao.util.BsonUtil;

public class PingOut {

    public byte[] toBson() {
        return BsonUtil.jsonToBson("{}");
    }

}
