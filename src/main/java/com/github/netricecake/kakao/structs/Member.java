package com.github.netricecake.kakao.structs;

import lombok.Getter;

@Getter
public class Member {

    private long id;

    private String name;

    public Member(long id, String name) {
        this.id = id;
        this.name = name;
    }

}
