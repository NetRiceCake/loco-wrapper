package com.github.netricecake.kakao.structs;

import com.github.netricecake.kakao.TalkClient;
import lombok.Getter;

@Getter
public class Member {

    private final TalkClient client;

    private final ChatRoom chatRoom;

    private final long userId;

    private final int type;

    private final String nickName;

    private final String profileImageUrl;

    private final String fullProfileImageUrl;

    private final String originalProfileImageUrl;

    private final long profileLinkId;

    private final int memberType;

    private final int profileType; // 1 실제 프로필, 2 카카오 프로필, 16 오픈 프로필

    public Member(TalkClient client, ChatRoom chatRoom, long userId, int type, String nickName, String profileImageUrl, String fullProfileImageUrl, String originalProfileImageUrl, long profileLinkId, int memberType, int profileType) {
        this.client = client;
        this.chatRoom = chatRoom;
        this.userId = userId;
        this.type = type;
        this.nickName = nickName;
        this.profileImageUrl = profileImageUrl;
        this.fullProfileImageUrl = fullProfileImageUrl;
        this.originalProfileImageUrl = originalProfileImageUrl;
        this.profileLinkId = profileLinkId;
        this.memberType = memberType;
        this.profileType = profileType;
    }

    public boolean kick() {
        return chatRoom.kickMember(this);
    }
}
