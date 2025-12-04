package com.github.netricecake.kakao.structs;

import lombok.Getter;

@Getter
public class Member {

    private long id;

    private String name;

    private int memberType;

    public Member(long id, String name, int memberType) {
        this.id = id;
        this.name = name;
        this.memberType = memberType;
    }

}
