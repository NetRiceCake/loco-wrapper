package com.github.netricecake.kakao.structs;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ChatRoom {

    private long chatId;

    private String type;

    private String name;

    private long linkId;

    private Map<Long, Member> members = new HashMap<>();

    public int getMemberCount() {
        return members.size();
    }

    public Member getMember(long id) {
        return members.get(id);
    }

}
