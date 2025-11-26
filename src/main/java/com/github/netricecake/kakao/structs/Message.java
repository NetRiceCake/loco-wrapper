package com.github.netricecake.kakao.structs;

import lombok.Getter;

@Getter
public class Message {

    private long logId;

    private ChatRoom chatRoom;

    private Member author;

    private int type;

    private String message;

    private String attachment;

    public Message(long logId, ChatRoom chatRoom, Member author, int type, String message, String attachment) {
        this.logId = logId;
        this.chatRoom = chatRoom;
        this.author = author;
        this.type = type;
        this.message = message;
        this.attachment = attachment;
    }

}
