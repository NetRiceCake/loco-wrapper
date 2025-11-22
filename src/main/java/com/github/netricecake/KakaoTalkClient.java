package com.github.netricecake;

import com.github.netricecake.kakao.KakaoDefaultValues;
import com.github.netricecake.message.request.*;
import com.github.netricecake.message.response.CheckInResponse;
import com.github.netricecake.message.response.GetConfResponse;
import com.github.netricecake.message.response.MessageResponse;
import com.github.netricecake.network.LocoPacket;
import com.github.netricecake.network.LocoSocket;
import com.github.netricecake.util.BsonUtil;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class KakaoTalkClient {

    private final static String DEFAULT_MCCMNC = KakaoDefaultValues.MCCMNC;

    private KakaoApi.LoginData loginData;
    private GetConfResponse bookingData;
    private CheckInResponse checkInData;

    LocoSocket socket;

    public void login(String email, String password, String deviceName, String deviceUuid) throws Exception, IOException, InvalidParameterException, IllegalStateException {
        try {
            loginData = KakaoApi.loginRequest(email, password, deviceName, deviceUuid);
        } catch (IllegalStateException e) {
            Map.Entry<String, Integer> registerInfo = KakaoApi.generatePasscode(email, password, deviceName, deviceUuid);
            System.out.println("디바이스 등록이 필요합니다.");
            System.out.println("카카오톡에서 " + registerInfo.getValue() + "초 안에 코드를 입력해주세요. 코드 : " + registerInfo.getKey());
            boolean registerResult = KakaoApi.registerDevice(email, password, deviceUuid);
            if (!registerResult) throw new IllegalStateException("기기 등록 실패");
            System.out.println("기기 등록 성공");
            loginData = KakaoApi.loginRequest(email, password, deviceName, deviceUuid);
        }
        System.out.println("로그인 성공");

        bookingData = KakaoApi.getBookingData(DEFAULT_MCCMNC, loginData.userId);

        LocoSocket checkInSocket = new LocoSocket(bookingData.getAddr(), bookingData.getPort());

        try {
            CheckInRequest checkInRequest = new CheckInRequest();
            checkInRequest.setUserId(loginData.userId);
            byte[] body = checkInRequest.toBson();
            checkInSocket.connect();
            checkInSocket.write(new LocoPacket(1001, checkInRequest.getMethod(), body));
            checkInData = new CheckInResponse();
            checkInData.fromBson(checkInSocket.read().getBody());
            checkInSocket.close();
        } catch (Exception e) {
            System.out.println("오류 : " + e.getMessage());
        }

        socket = new LocoSocket(checkInData.getHost(), checkInData.getPort());
        socket.connect();
        LoginListRequest req = new LoginListRequest();
        req.setDuuid(deviceUuid);
        req.setOauthToken(loginData.accessToken);
        socket.write(new LocoPacket(1002, "LOGINLIST", req.toBson()));
    }

    public void test() throws Exception {
        while(true) {
            LocoPacket packet = socket.read();
            if (packet == null) continue;
            if (!packet.getMethod().equals("MSG")) continue;

            socket.write(new LocoPacket(packet.getPacketId(), "MSG", new MessageRequest().toBson()));
            MessageResponse res = new MessageResponse();
            res.fromBson(packet.getBody());
            if (res.getType() != 1) return;
            if (res.getMessage().trim().equals("!!test")) {
                WriteRequest req = new WriteRequest();
                req.setChatId(res.getChatId());
                req.setMessage("test!!");
                socket.write(new LocoPacket(1003, "WRITE", req.toBson()));
                LocoPacket t = socket.read();
                System.out.println(BsonUtil.bsonToJson(t.getBody()));
                System.exit(0);
            }

        }
    }

}
