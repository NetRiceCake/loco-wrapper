package com.github.netricecake.kakao;

import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.kakao.structs.Member;
import com.github.netricecake.kakao.structs.Message;
import com.github.netricecake.loco.LocoPacket;
import com.github.netricecake.loco.LocoSocektHandler;
import com.github.netricecake.loco.packet.inbound.*;
import com.github.netricecake.loco.packet.outbound.ChatInfoOut;
import com.github.netricecake.loco.packet.outbound.InfoLinkOut;
import com.github.netricecake.loco.packet.outbound.MessageOut;
import lombok.Getter;

public class LocoSocketHandlerImpl extends LocoSocektHandler {

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

            ChatRoom room = client.getChatRooms().get(in.getChatId());
            if (!room.getType().equals("OM")) return;
            Message msg = new Message(in.getLogId(), room, new Member(in.getAuthorId(), in.getAuthorNickname()), in.getType(), in.getMessage(), in.getAttachment());
            client.getTalkHandler().onMessage(msg);
        } else if (packet.getMethod().equals("NEWMEM")) {
            NewMemIn in = new NewMemIn();
            in.fromBson(packet.getBody());
            checkRoom(in.getChatId());

            ChatRoom room = client.getChatRooms().get(in.getChatId());
            if (!room.getType().equals("OM")) return;
            client.getTalkHandler().onNewMember(room, new Member(in.getUserId(), in.getNickname()));
        } else if (packet.getMethod().equals("DELMEM")) {
            DelMemIn in = new DelMemIn();
            in.fromBson(packet.getBody());
            checkRoom(in.getChatId());

            ChatRoom room = client.getChatRooms().get(in.getChatId());
            if (!room.getType().equals("OM")) return;
            client.getTalkHandler().onDelMember(room, new Member(in.getUserId(), in.getNickname()));
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
            if (ci.getType().equals("OM")) {
                InfoLinkOut lo =  new InfoLinkOut(ci.getLinkId());
                InfoLinkIn li = new InfoLinkIn();
                li.fromBson(client.getSocket().writeAndRead(new LocoPacket("INFOLINK", lo.toBson())).getBody());
                room.setName(li.getName());
            }

            client.getChatRooms().put(chatId, room);
        }
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {
        client.connected = false;
        System.out.println("연결 끊어짐");
    }

    @Override
    public void onError(Exception e) {

    }
}
