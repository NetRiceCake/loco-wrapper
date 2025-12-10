package com.github.netricecake.kakao;

import com.github.netricecake.kakao.packet.inbound.member.*;
import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.kakao.structs.Member;
import com.github.netricecake.kakao.structs.MemberType;
import com.github.netricecake.kakao.structs.Message;
import com.github.netricecake.kakao.loco.LocoPacket;
import com.github.netricecake.kakao.loco.LocoSocketHandler;
import com.github.netricecake.kakao.packet.inbound.message.MessageIn;
import com.github.netricecake.kakao.packet.outbound.member.MemberOut;
import com.github.netricecake.kakao.packet.outbound.message.MessageOut;

public class LocoSocketHandlerImpl extends LocoSocketHandler {

    private final TalkClient client;

    public LocoSocketHandlerImpl(TalkClient client) {
        this.client = client;
    }

    @Override
    public void onPacket(LocoPacket packet) {
        if (packet.getMethod().equals("MSG")) {
            client.getSocket().write(new LocoPacket(packet.getPacketId(), "MSG", new MessageOut().toBson()));

            MessageIn in = new MessageIn(packet.getBody());
            ChatRoom chatRoom = client.getChatRoom(in.getChatId());
            if (chatRoom == null) return;
            Member member = chatRoom.getMembers().get(in.getAuthorId());

            Message msg = new Message(client, chatRoom, in.getLogId(), member, in.getType(), in.getSendAt(), in.getMessage(), in.getAttachment());
            Thread.ofVirtual().start(() -> {
                client.getTalkHandler().onMessage(msg);
            });
        } else if (packet.getMethod().equals("NEWMEM")) {
            NewMemIn res = new NewMemIn(packet.getBody());
            ChatRoom chatRoom = client.getChatRoom(res.getChatId());
            if (chatRoom == null) return;
            MemberOut mo = new MemberOut(res.getChatId(), res.getUserId());
            MemberIn mi = new MemberIn(client.getSocket().writeAndRead(new LocoPacket("MEMBER", mo.toBson())).getBody());
            chatRoom.getMembers().put(res.getUserId(), new Member(client, chatRoom, res.getUserId(), mi.getType(), mi.getNickName(), mi.getProfileImageUrl(), mi.getFullProfileImageUrl(), mi.getOriginalProfileImageUrl(), mi.getProfileLinkId(), mi.getMemberType(), mi.getProfileType()));

            Thread.ofVirtual().start(() -> {
                client.getTalkHandler().onNewMember(chatRoom, chatRoom.getMembers().get(res.getUserId()));
            });
        } else if (packet.getMethod().equals("DELMEM")) {
            DelMemIn in = new DelMemIn(packet.getBody());
            ChatRoom chatRoom = client.getChatRoom(in.getChatId());
            if (chatRoom == null) return;
            chatRoom.getMembers().remove(in.getUserId());

            Thread.ofVirtual().start(() -> {
                client.getTalkHandler().onDelMember(chatRoom, in.getUserId(), in.getNickname());
            });
        } else if (packet.getMethod().equals("SYNCLINKPF")) {
            SyncLinkPfIn res = new SyncLinkPfIn(packet.getBody());
            ChatRoom chatRoom = client.getChatRoom(res.getChatId());
            if (chatRoom == null) return;
            chatRoom.getMembers().remove(res.getUserId());
            Member member = new Member(client, chatRoom, res.getUserId(), 1000, res.getNickName(), res.getProfileImageUrl(), res.getFullProfileImageUrl(), res.getOriginalProfileImageUrl(), res.getProfileLinkId(), MemberType.MEMBER, res.getProfileType());
            chatRoom.getMembers().put(res.getUserId(), member);
        }
    }

    @Override
    public void onConnect() {
        System.out.println("연결 성공");
    }

    @Override
    public void onDisconnect() {
        client.connected = false;
        System.out.println("연결 끊김");
    }

    @Override
    public void onError(Exception e) {
        System.out.println(e.getMessage());
    }
}
