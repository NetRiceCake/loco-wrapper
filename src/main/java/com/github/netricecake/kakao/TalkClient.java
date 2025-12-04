package com.github.netricecake.kakao;

import com.github.netricecake.kakao.exception.*;
import com.github.netricecake.kakao.structs.ChatRoom;
import com.github.netricecake.loco.LocoPacket;
import com.github.netricecake.loco.LocoSocektHandler;
import com.github.netricecake.loco.LocoSocket;
import com.github.netricecake.loco.packet.inbound.login.CheckInIn;
import com.github.netricecake.loco.packet.inbound.login.GetConfIn;
import com.github.netricecake.loco.packet.inbound.login.LoginListIn;
import com.github.netricecake.loco.packet.inbound.message.PostIn;
import com.github.netricecake.loco.packet.inbound.message.ShipIn;
import com.github.netricecake.loco.packet.inbound.message.WriteIn;
import com.github.netricecake.loco.packet.outbound.login.CheckInOut;
import com.github.netricecake.loco.packet.outbound.login.LoginListOut;
import com.github.netricecake.loco.packet.outbound.etc.PingOut;
import com.github.netricecake.loco.packet.outbound.message.PostOut;
import com.github.netricecake.loco.packet.outbound.message.ShipOut;
import com.github.netricecake.loco.packet.outbound.message.WriteOut;
import com.github.netricecake.loco.util.BsonUtil;
import com.github.netricecake.loco.util.ByteUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
    protected boolean connected;

    private KakaoApi.LoginData loginData;
    private GetConfIn bookingData;
    private CheckInIn checkInData;
    private LoginListIn loginListData;

    private ExecutorService locoHandlerPool;

    @Getter
    private TalkHandler talkHandler;

    @Getter
    private LocoSocket socket;

    public TalkClient(String email, String password, String deviceName, String deviceUuid, TalkHandler talkHandler) throws IOException {
        this.email = email;
        this.password = password;
        this.deviceName = deviceName;
        this.deviceUuid = deviceUuid;
        this.sessionDir = System.getProperty("user.dir") + "/" + email + "_" + deviceName + "/";
        this.talkHandler = talkHandler;
        talkHandler.setTalkClient(this);

        new File(sessionDir).mkdirs();
        File loginDataFile = new File(sessionDir + "loginData.json");
        if (!loginDataFile.exists()) return;
        String loginDataJson = Files.readString(Paths.get(loginDataFile.getAbsolutePath()));
        loginData = new KakaoApi.LoginData(loginDataJson);
    }

    public void connect() throws IOException, InvalidDeviceNameException, InvalidDeviceUUIDException, BadCredentialsException, UnregisteredDeviceException, BookingFailedException, LoginFailedException {
        if (this.connected) throw new IOException("Already connected.");
        if (loginData == null) { // 저장된 로그인 데이터가 없는 경우 로그인 시도
            loginData = KakaoApi.loginRequest(email, password, deviceName, deviceUuid);
            File loginDataFile = new File(sessionDir + "loginData.json");
            if (!loginDataFile.exists()) loginDataFile.createNewFile();
            Files.write(Paths.get(loginDataFile.getAbsolutePath()), loginData.toJson().getBytes());
        }

        bookingData = KakaoApi.getBookingData(loginData.userId);
        if (bookingData == null || bookingData.getStatus() != 0) throw new BookingFailedException();

        LocoSocket checkInSocket = new LocoSocket(bookingData.getAddr(), bookingData.getPort(), new LocoSocektHandler() {
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }, Executors.newFixedThreadPool(1));
        byte[] body = new CheckInOut(loginData.userId).toBson();
        checkInSocket.connect();
        LocoPacket checkinResponse = checkInSocket.writeAndRead(new LocoPacket(1000, "CHECKIN", body));
        checkInData = new CheckInIn(checkinResponse.getBody());
        checkInSocket.close();

        long lastTokenId = 0;
        long lbk = 0;
        byte[] rp = ByteUtil.hexStringToByteArray("0000ffff0000");

        File loginListDataFile = new File(sessionDir + "loginListData.json");
        if (loginListDataFile.exists()) {
            String loginDataJson = Files.readString(Paths.get(loginListDataFile.getAbsolutePath()));
            JsonObject loginListData = JsonParser.parseString(loginDataJson).getAsJsonObject();
            lastTokenId = loginListData.getAsJsonPrimitive("lastTokenId").getAsLong();
            lbk = loginListData.getAsJsonPrimitive("lbk").getAsLong();
            rp = ByteUtil.hexStringToByteArray("0100ffff0100"); // 이게 도대체 뭐임
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
            throw new LoginFailedException();
        }

        if (!loginListDataFile.exists()) loginListDataFile.createNewFile();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lastTokenId", loginListData.getLastTokenId());
        jsonObject.addProperty("lbk", loginListData.getLbk());
        Files.write(Paths.get(loginListDataFile.getAbsolutePath()), new Gson().toJson(jsonObject).getBytes());

        connected = true;

        new Thread(() -> {
            try {
                while (socket.isAlive()) {
                    Thread.sleep(5 * 60 * 1000);
                    PingOut pingOut = new PingOut();
                    LocoPacket pingPacket = new LocoPacket("PING", pingOut.toBson());
                    socket.write(pingPacket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean sendMessage(long chatId, int type, String message, String extra) {
        WriteOut wo = new WriteOut();
        wo.setChatId(chatId);
        wo.setType(type);
        wo.setMessage(message);
        wo.setExtra(extra);
        WriteIn wi = new WriteIn();
        wi.fromBson(socket.writeAndRead(new LocoPacket("WRITE", wo.toBson())).getBody());
        return wi.getStatus() == 0;
    }

    public boolean sendMessage(long chatId, String message) {
        return sendMessage(chatId, 1, message, "{}");
    }

    public boolean sendJpg(long chatId, byte[] image, String format, int width, int height) {
        LocoSocket postSocket = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            ShipOut so = new ShipOut();
            so.setChatId(chatId);
            so.setSize(image.length);
            so.setCheckSum(ByteUtil.byteArrayToHexString(md.digest(image)));
            ShipIn si = new ShipIn();
            si.fromBson(socket.writeAndRead(new LocoPacket("SHIP", so.toBson())).getBody());
            if (si.getStatus() != 0) return false;

            final CompletableFuture<Integer> future = new CompletableFuture<>();
            postSocket = new LocoSocket(si.getVhost(), si.getPort(), new LocoSocektHandler() {
                @Override
                public void onPacket(LocoPacket packet) {
                    JsonObject jsonObject = BsonUtil.bsonToJsonObject(packet.getBody());
                    int status = jsonObject.get("status").getAsInt();
                    future.complete(status);
                }
            }, Executors.newFixedThreadPool(1));
            postSocket.connect();

            PostOut po =  new PostOut();
            po.setUserId(loginData.userId);
            po.setKey(si.getKey());
            po.setSize(image.length);
            po.setChatId(chatId);
            po.setWidth(width);
            po.setHeight(height);

            PostIn pi = new PostIn();
            pi.fromBson(postSocket.writeAndRead(new LocoPacket("POST", po.toBson())).getBody());
            if (pi.getStatus() != 0) {
                postSocket.close();
                return false;
            }

            LocoPacket packet = new LocoPacket("", image);
            packet.setRaw(true);
            postSocket.write(packet);
            int status = future.get();
            postSocket.close();
            return status == 0;
        } catch (Exception e) {
        } finally {
            if (postSocket != null) postSocket.close();
        }
        return false;
    }

    public long getUserId() {
        return loginData.userId;
    }

}
