package com.github.netricecake.kakao;

import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.loco.LocoPacket;
import com.github.netricecake.loco.LocoSocektHandler;
import com.github.netricecake.loco.LocoSocket;
import com.github.netricecake.loco.packet.inbound.CheckInIn;
import com.github.netricecake.loco.packet.inbound.GetConfIn;
import com.github.netricecake.loco.packet.inbound.LoginListIn;
import com.github.netricecake.loco.packet.inbound.WriteIn;
import com.github.netricecake.loco.packet.outbound.CheckInOut;
import com.github.netricecake.loco.packet.outbound.LoginListOut;
import com.github.netricecake.loco.packet.outbound.PingOut;
import com.github.netricecake.loco.packet.outbound.WriteOut;
import com.github.netricecake.loco.util.ByteUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TalkClient {

    private String email;
    private String password;
    private String deviceName;
    private String deviceUuid;
    private String sessionDir;

    @Getter
    private Map<Long, ChatRoom> chatRooms = new HashMap<>();

    @Getter
    private boolean connected;

    private KakaoApi.LoginData loginData;
    private GetConfIn bookingData;
    private CheckInIn checkInData;
    private LoginListIn loginListData;

    private ExecutorService locoHandlerPool;

    @Getter
    private TalkHandler talkHandler;

    @Getter
    private LocoSocket socket;

    public TalkClient(String email, String password, String deviceName, String deviceUuid, TalkHandler talkHandler) {
        this.email = email;
        this.password = password;
        this.deviceName = deviceName;
        this.deviceUuid = deviceUuid;
        this.sessionDir = System.getProperty("user.dir") + "/" + email + "_" + deviceName + "/";
        this.talkHandler = talkHandler;
        talkHandler.setTalkClient(this);

        new File(sessionDir).mkdir();
        loginData = readLoginData();
    }

    public void connect() throws Exception {
        if (this.connected) throw new Exception("이미 연결되어 있습니다.");
        if (loginData == null) {
            System.out.println("로그인 데이터가 없습니다. 새로 로그인을 시도합니다.");
            try {
                loginData = KakaoApi.loginRequest(email, password, deviceName, deviceUuid);
            } catch (IllegalStateException e) {
                Map.Entry<String, Integer> registerInfo = KakaoApi.generatePasscode(email, password, deviceName, deviceUuid);
                System.out.println("기기 등록이 필요합니다.");
                System.out.println("카카오톡 앱에서 " + registerInfo.getValue() + "초 안에 코드를 입력해주세요. 코드 : " + registerInfo.getKey());
                boolean registerResult = KakaoApi.registerDevice(email, password, deviceUuid);
                if (!registerResult) throw new IllegalStateException("기기등록 실패");
                System.out.println("기기 등록 성공");
                loginData = KakaoApi.loginRequest(email, password, deviceName, deviceUuid);
            }
            System.out.println("로그인이 완료되었습니다.");
            writeLoginData();
        }

        bookingData = KakaoApi.getBookingData(loginData.userId);

        LocoSocket checkInSocket = new LocoSocket(bookingData.getAddr(), bookingData.getPort(), new LocoSocektHandler() {
            @Override
            public void onPacket(LocoPacket packet) {

            }

            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }, Executors.newFixedThreadPool(1));
        CheckInOut checkInRequest = new CheckInOut(loginData.userId);
        byte[] body = checkInRequest.toBson();
        checkInSocket.connect();
        LocoPacket checkinResponse = checkInSocket.writeAndRead(new LocoPacket(1000, checkInRequest.getMethod(), body));
        checkInData = new CheckInIn();
        checkInData.fromBson(checkinResponse.getBody());
        checkInSocket.close();



        long lastTokenId = 0;
        long lbk = 0;
        byte[] rp = ByteUtil.hexStringToByteArray("0000ffff0000");

        try {
            File loginListDataFile = new File(sessionDir + "loginListData.json");
            if (loginListDataFile.exists()) {
                String loginDataJson = Files.readString(Paths.get(loginListDataFile.getAbsolutePath()));
                JsonObject loginListData = JsonParser.parseString(loginDataJson).getAsJsonObject();
                lastTokenId = loginListData.getAsJsonPrimitive("lastTokenId").getAsLong();
                lbk = loginListData.getAsJsonPrimitive("lbk").getAsLong();
                rp = ByteUtil.hexStringToByteArray("0100ffff0100"); // 이게 도당체 뭐임
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        locoHandlerPool = Executors.newFixedThreadPool(1);

        socket = new LocoSocket(checkInData.getHost(), checkInData.getPort(), new LocoSocketHandlerImpl(this), locoHandlerPool);
        socket.connect();
        LoginListOut req = new LoginListOut();
        req.setDuuid(deviceUuid);
        req.setOauthToken(loginData.accessToken);
        req.setLastTokenId(lastTokenId);
        req.setLbk(lbk);
        req.setRp(rp);
        loginListData = new LoginListIn();
        loginListData.fromBson(socket.writeAndRead(new LocoPacket("LOGINLIST", req.toBson())).getBody());
        if (loginListData.getStatus() != 0) {
            System.out.println("카카오톡 서버와 연결을 실패했습니다. 로그인 정보 파일을 삭제 후 다시 시도해보세요.");
            throw new Exception("카카오톡 서버와 연결을 실패했습니다. 로그인 정보 파일을 삭제 후 다시 시도해보세요.");
        }
        System.out.println("연결 성공");

        try {
            File loginListDataFile = new File(sessionDir + "loginListData.json");
            if (!loginListDataFile.exists()) loginListDataFile.createNewFile();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("lastTokenId", loginListData.getLastTokenId());
            jsonObject.addProperty("lbk", loginListData.getLbk());
            Files.write(Paths.get(loginListDataFile.getAbsolutePath()), new Gson().toJson(jsonObject).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                while (socket.isAlive()) {
                    Thread.sleep(10 * 60 * 1000);
                    PingOut pingOut = new PingOut();
                    LocoPacket pingPacket = new LocoPacket("PING", pingOut.toBson());
                    socket.write(pingPacket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private KakaoApi.LoginData readLoginData() {
        try {
            File loginDataFile = new File(sessionDir + "loginData.json");
            if (!loginDataFile.exists()) return null;
            String loginDataJson = Files.readString(Paths.get(loginDataFile.getAbsolutePath()));
            KakaoApi.LoginData loginData = new KakaoApi.LoginData();
            loginData.fromJson(loginDataJson);
            return loginData;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void writeLoginData() {
        try {
            File loginDataFile = new File(sessionDir + "loginData.json");
            if (!loginDataFile.exists()) loginDataFile.createNewFile();
            Files.write(Paths.get(loginDataFile.getAbsolutePath()), loginData.toJson().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(ChatRoom room, int type, String message, String extra) {
        WriteOut wo = new WriteOut();
        wo.setChatId(room.getChatId());
        wo.setType(type);
        wo.setMessage(message);
        wo.setExtra(extra);
        WriteIn wi = new WriteIn();
        wi.fromBson(socket.writeAndRead(new LocoPacket("WRITE", wo.toBson())).getBody());
        return wi.getStatus() == 0;
    }

    public boolean sendMessage(ChatRoom room, String message) {
        return sendMessage(room, 1, message, "{}");
    }

}
