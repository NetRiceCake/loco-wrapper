package com.github.netricecake.kakao.structs;

import com.github.netricecake.kakao.TalkClient;
import com.github.netricecake.kakao.util.ImageUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom {

    @Getter
    private final TalkClient client;

    @Getter
    private final long chatId;

    @Getter
    private final String type;

    @Getter
    private final String name;

    @Getter
    private final long linkId;

    @Getter
    private final Map<Long, Member> members = new HashMap<>();

    public ChatRoom(TalkClient client, long chatId, String type, String name, long linkId) {
        this.client = client;
        this.chatId = chatId;
        this.type = type;
        this.name = name;
        this.linkId = linkId;
    }

    public int getMemberCount() {
        return members.size();
    }

    public Member getMember(long id) {
        return members.get(id);
    }

    public boolean kickMember(long userId) {
        if (!(type.equals(ChatRoomType.OPEN_CHAT) || type.equals(ChatRoomType.OPEN_DIRECT))) return false;
        if (!(getMember(client.getMyUserId()).getMemberType() == MemberType.OWNER || getMember(client.getMyUserId()).getMemberType() == MemberType.ADMIN)) return false;
        return client.kickMember(chatId, linkId, userId);
    }

    public boolean kickMember(Member member) {
        return kickMember(member.getUserId());
    }

    public boolean sendMessage(String message) {
        return client.sendMessage(chatId, message);
    }

    public boolean sendMessage(String message, String extra) {
        return client.sendMessage(chatId, message, extra);
    }

    public boolean sendJpg(byte[] image) {
        return client.sendJpg(chatId, image); //client.sendImage(chatId, image, extension);
    }

    public boolean sendVideo(String extension, byte[] video) {
        return false;
    }

    public boolean sendFile(String extension, byte[] file) {
        return false;
    }

}
