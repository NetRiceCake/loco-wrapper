package com.github.netricecake.kakao;

import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.kakao.structs.Member;
import com.github.netricecake.kakao.structs.Message;
import lombok.Getter;
import lombok.Setter;

public class TalkHandler {

    @Getter
    @Setter
    private TalkClient talkClient;

    public void onConnect() {

    }

    public void onDisconnect() {

    }

    public void onMessage(Message message) {

    }

    public void onNewMember(ChatRoom chaRoom, Member member) {

    }

    public void onDelMember(ChatRoom chatRoom, long userId, String nickName) {

    }

}
