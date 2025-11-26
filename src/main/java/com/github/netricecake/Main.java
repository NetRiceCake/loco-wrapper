package com.github.netricecake;

import com.github.netricecake.kakao.TalkClient;
import com.github.netricecake.kakao.TalkHandler;
import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.kakao.structs.Member;
import com.github.netricecake.kakao.structs.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
public class Main {

    static String email = "invalid@example.com";
    static String password = "example";
    static String deviceName = "SM-X930"; // 갤럭시 탭 s11 울트라,   지원되는 태블릿 모델명 넣으세요
    static String deviceUuid = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"; // 64자 랜덤 hex-string

    public static void main(String[] args) throws Exception {
        TalkClient client = new TalkClient(email, password, deviceName, deviceUuid, new TalkHandler() {
            @Override
            public void onMessage(Message msg) {
                if (msg.getType() != 1) return; // 1이 그냥 채팅, 그냥 채팅만 받기
                if (msg.getMessage().equals("!send")) {
                    getTalkClient().sendMessage(msg.getChatRoom(), "test");
                }
                else if (msg.getMessage().equals("!reply")) { // 답장
                    int replyType = 26; // 답장 타입
                    JsonObject extraObject = new JsonObject();
                    extraObject.addProperty("src_logId", msg.getLogId());
                    extraObject.addProperty("src_userId", msg.getAuthor().getId());
                    extraObject.addProperty("src_message", msg.getMessage());
                    extraObject.addProperty("src_type", msg.getType());
                    extraObject.addProperty("src_linkId", msg.getChatRoom().getLinkId());
                    getTalkClient().sendMessage(msg.getChatRoom(), replyType, "reply test", extraObject.toString());
                }
                else if (msg.getMessage().equals("!mention")) { // 멘션
                    JsonObject extraObject = new JsonObject();
                    JsonArray mentionArray = new JsonArray();
                    JsonObject mentionObject = new JsonObject();
                    mentionObject.addProperty("user_id", msg.getAuthor().getId());
                    JsonArray pos = new JsonArray();
                    pos.add(1);
                    mentionObject.add("at", pos);
                    mentionObject.addProperty("len", msg.getAuthor().getName().length());
                    mentionArray.add(mentionObject);
                    extraObject.add("mentions", mentionArray);
                    getTalkClient().sendMessage(msg.getChatRoom(), 1, "@" + msg.getAuthor().getName(), extraObject.toString());
                }
            }

            @Override
            public void onNewMember(ChatRoom room, Member member) {
                getTalkClient().sendMessage(room, member.getName() + "님이 들어왔습니다.");
            }

            @Override
            public void onDelMember(ChatRoom room, Member member) {
                getTalkClient().sendMessage(room, member.getName() + "님이 나갔습니다.");
            }
        });
        client.connect();
    }
}
