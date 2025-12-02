package com.github.netricecake.loco.packet.outbound.etc;

import com.github.netricecake.loco.util.BsonUtil;

public class PingOut {

    public byte[] toBson() {
        return BsonUtil.jsonToBson("{}");
    }

}
