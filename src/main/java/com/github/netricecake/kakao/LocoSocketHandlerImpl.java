package com.github.netricecake.kakao;

import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.kakao.structs.Member;
import com.github.netricecake.kakao.structs.Message;
import com.github.netricecake.loco.LocoPacket;
import com.github.netricecake.loco.LocoSocketHandler;
import com.github.netricecake.loco.packet.inbound.member.*;
import com.github.netricecake.loco.packet.inbound.message.MessageIn;
import com.github.netricecake.loco.packet.inbound.room.ChatInfoIn;
import com.github.netricecake.loco.packet.inbound.room.InfoLinkIn;
import com.github.netricecake.loco.packet.outbound.member.MemberOut;
import com.github.netricecake.loco.packet.outbound.room.ChatInfoOut;
import com.github.netricecake.loco.packet.outbound.member.GetMemberOut;
import com.github.netricecake.loco.packet.outbound.room.InfoLinkOut;
import com.github.netricecake.loco.packet.outbound.message.MessageOut;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

public class LocoSocketHandlerImpl extends LocoSocketHandler {

    @Getter
    private TalkClient client;

    public LocoSocketHandlerImpl(TalkClient client) {
        this.client = client;
    }

    @Override
    public void onPacket(LocoPacket packet) {
        if (packet.getMethod().equals("MSG")) {
            client.getSocket().write(new LocoPacket(packet.getPacketId(), "MSG", new MessageOut().toBson()));

            MessageIn in = new MessageIn();
            in.fromBson(packet.getBody());
            checkRoom(in.getChatId());
            checkMember(in.getChatId(), in.getAuthorId());

            ChatRoom room = client.getChatRooms().get(in.getChatId());
            Member member = room.getMembers().get(in.getAuthorId());

            Message msg = new Message(in.getLogId(), room, member, in.getType(), in.getMessage(), in.getAttachment());
            client.getTalkHandler().onMessage(msg);
        } else if (packet.getMethod().equals("NEWMEM")) {
            NewMemIn in = new NewMemIn();
            in.fromBson(packet.getBody());
            checkRoom(in.getChatId());
            checkMember(in.getChatId(), in.getUserId());

            ChatRoom room = client.getChatRooms().get(in.getChatId());
            if (!room.getType().equals("OM")) return;
            client.getTalkHandler().onNewMember(room, room.getMembers().get(in.getUserId()));
        } else if (packet.getMethod().equals("DELMEM")) {
            DelMemIn in = new DelMemIn();
            in.fromBson(packet.getBody());
            checkRoom(in.getChatId());

            ChatRoom room = client.getChatRooms().get(in.getChatId());
            if (!room.getType().equals("OM")) return;
            client.getTalkHandler().onDelMember(room, new Member(in.getUserId(), in.getNickname(), 2));
            room.getMembers().remove(in.getUserId());
        } else if (packet.getMethod().equals("SYNCLINKPF")) {
            SyncLinkPfIn si = new SyncLinkPfIn();
            si.fromBson(packet.getBody());
            ChatRoom room = client.getChatRooms().get(si.getChatId());
            room.getMembers().remove(si.getUserId());
            Member member = new Member(si.getUserId(), si.getNickName(), 2);
            room.getMembers().put(si.getUserId(), member);
        }
    }

    private void checkRoom(long chatId) {
        if (!client.getChatRooms().containsKey(chatId)) {
            ChatInfoOut co = new ChatInfoOut(chatId);
            ChatInfoIn ci = new ChatInfoIn();
            ci.fromBson(client.getSocket().writeAndRead(new LocoPacket("CHATINFO", co.toBson())).getBody());
            ChatRoom room = new ChatRoom();
            room.setChatId(chatId);
            room.setType(ci.getType());
            room.setLinkId(ci.getLinkId());
            GetMemberOut gmo = new GetMemberOut(chatId);
            GetMemberIn gmi = new GetMemberIn();
            gmi.fromBson(client.getSocket().writeAndRead(new LocoPacket("GETMEM", gmo.toBson())).getBody());
            for (int i = 0; i < gmi.getMembers().size(); i++) {
                JsonObject json = gmi.getMembers().get(i).getAsJsonObject();
                int type = json.get("mt") != null ? json.get("mt").getAsInt() : 2;
                Member member = new Member(json.get("userId").getAsLong(), json.get("nickName").getAsString(), type);
                room.getMembers().put(member.getId(), member);
            }

            if (ci.getType().equals("OM")) {
                InfoLinkOut lo =  new InfoLinkOut(ci.getLinkId());
                InfoLinkIn li = new InfoLinkIn();
                li.fromBson(client.getSocket().writeAndRead(new LocoPacket("INFOLINK", lo.toBson())).getBody());
                room.setName(li.getName());
            } else if (ci.getType().equals("MultiChat")) {
                if (!ci.getChatMetas().isEmpty()) {
                    JsonArray chatMetas = ci.getChatMetas();
                    room.setName(chatMetas.get(0).getAsJsonObject().get("content").getAsString());
                } else {
                    JsonArray displayMembers = ci.getDisplayMembers();
                    String name = "";
                    for (int i = 0; i < displayMembers.size(); i++) {
                        name += displayMembers.get(i).getAsJsonObject().get("nickName").getAsString() + ", ";
                    }
                    room.setName(name);
                }
            } else if (ci.getType().equals("DirectChat")) {
                room.setName(ci.getDisplayMembers().get(0).getAsJsonObject().get("nickName").getAsString());
            }
            client.getChatRooms().put(chatId, room);
        }
    }

    private void checkMember(long chatId, long memberId) {
        ChatRoom room = client.getChatRooms().get(chatId);
        if (!room.getMembers().containsKey(memberId)) {
            MemberOut mo = new MemberOut(chatId, memberId);
            MemberIn mi = new MemberIn();
            mi.fromBson(client.getSocket().writeAndRead(new LocoPacket("MEMBER", mo.toBson())).getBody());
            room.getMembers().put(memberId, new Member(memberId, mi.getNickName(), mi.getMemberType()));
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
