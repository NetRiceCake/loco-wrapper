package com.github.netricecake;

import com.github.netricecake.kakao.KakaoApi;
import com.github.netricecake.kakao.TalkClient;
import com.github.netricecake.kakao.TalkHandler;
import com.github.netricecake.kakao.exception.*;
import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.kakao.structs.Member;
import com.github.netricecake.kakao.structs.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;

public class Main {

    static String email = "invalid@example.com"; // 이메일 말고 전화번호도 가능
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
        
        try {
            client.connect();
        } catch (InvalidDeviceNameException e) {
            System.out.println("서브 디바이스 로그인을 지원하지 않는 디바이스입니다.");
        } catch (InvalidDeviceUUIDException e) {
            System.out.println("Device UUID는 64자리 hex string이어야 합니다.");
        } catch (BadCredentialsException e) {
            System.out.println("이메일(전화번호)이나 비밀번호가 틀렸습니다.");
        } catch (BookingFailedException e) {
            System.out.println("Booking 서버와의 통신을 실패했습니다.");
        } catch (LoginFailedException e) {
            System.out.println("카카오톡 서버와 연결을 실패했습니다. 로그인 정보 파일을 삭제 후 다시 시도해보세요.");
        } catch (UnregisteredDeviceException e) {
            System.out.println("디바이스 등록이 필요합니다.");
            Map.Entry<String, Integer> registerInfo = KakaoApi.generatePasscode(email, password, deviceName, deviceUuid);
            System.out.println("카카오톡 앱에서 " + registerInfo.getValue() + "초 안에 코드를 입력해주세요. 코드 : " + registerInfo.getKey());
            boolean registerResult = KakaoApi.registerDevice(email, password, deviceUuid);
            if (!registerResult) {
                System.out.println("디바이스 등록 실패");
            }
            System.out.println("디바이스 등록 성공, 다시 실행하세요.");
        }
    }
}
