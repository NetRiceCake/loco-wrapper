package com.github.netricecake.kakao.structs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoom {

    private long chatId;

    private String type;

    private String name;

    private long linkId;

    //private Map<Long, Member> members = new HashMap<>();

}
