package com.github.netricecake.kakao.structs;

import com.github.netricecake.kakao.TalkClient;
import com.github.netricecake.kakao.loco.LocoPacket;
import com.github.netricecake.kakao.packet.inbound.message.WriteIn;
import com.github.netricecake.kakao.packet.outbound.message.WriteOut;
import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class Message {

    private final TalkClient client;

    private final ChatRoom chatRoom;

    private final long logId;

    private final Member author;

    private final int type;

    private final long sendTime;

    private final String message;

    private final String attachment;

    public Message(TalkClient client, ChatRoom chatRoom, long logId, Member author, int type, long sendTime, String message, String attachment) {
        this.client = client;
        this.logId = logId;
        this.chatRoom = chatRoom;
        this.author = author;
        this.type = type;
        this.sendTime = sendTime;
        this.message = message;
        this.attachment = attachment;
    }

    public boolean reply(String message) {
        return client.reply(this, message);
    }

    public boolean blind() {
        return false;
    }

}
