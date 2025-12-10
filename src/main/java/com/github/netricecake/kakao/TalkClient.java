package com.github.netricecake.kakao;

import com.github.netricecake.kakao.exception.*;
import com.github.netricecake.kakao.packet.inbound.member.GetMemberIn;
import com.github.netricecake.kakao.packet.inbound.message.PostIn;
import com.github.netricecake.kakao.packet.inbound.message.ShipIn;
import com.github.netricecake.kakao.packet.inbound.room.ChatInfoIn;
import com.github.netricecake.kakao.packet.inbound.room.InfoLinkIn;
import com.github.netricecake.kakao.packet.outbound.member.GetMemberOut;
import com.github.netricecake.kakao.packet.outbound.message.PostOut;
import com.github.netricecake.kakao.packet.outbound.message.ShipOut;
import com.github.netricecake.kakao.packet.outbound.room.ChatInfoOut;
import com.github.netricecake.kakao.packet.outbound.room.InfoLinkOut;
import com.github.netricecake.kakao.structs.*;
import com.github.netricecake.kakao.loco.LocoPacket;
import com.github.netricecake.kakao.loco.LocoSocketHandler;
import com.github.netricecake.kakao.loco.LocoSocket;
import com.github.netricecake.kakao.packet.inbound.login.CheckInIn;
import com.github.netricecake.kakao.packet.inbound.login.GetConfIn;
import com.github.netricecake.kakao.packet.inbound.login.LoginListIn;
import com.github.netricecake.kakao.packet.inbound.message.WriteIn;
import com.github.netricecake.kakao.packet.outbound.login.CheckInOut;
import com.github.netricecake.kakao.packet.outbound.login.LoginListOut;
import com.github.netricecake.kakao.packet.outbound.etc.PingOut;
import com.github.netricecake.kakao.packet.outbound.member.KickMemberOut;
import com.github.netricecake.kakao.packet.outbound.message.WriteOut;
import com.github.netricecake.kakao.util.BsonUtil;
import com.github.netricecake.kakao.util.ByteUtil;
import com.github.netricecake.kakao.util.ImageUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.concurrent.Executors;

public class TalkClient {

    private final String email;
    private final String password;
    private final String deviceName;
    private final String deviceUuid;
    private final String sessionDir;

    private final Map<Long, ChatRoom> chatRooms = new HashMap<>();

    @Getter
    protected boolean connected;

    private KakaoApi.LoginData loginData;
    private GetConfIn bookingData;
    private CheckInIn checkInData;
    private LoginListIn loginListData;

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

        LocoSocket checkInSocket = new LocoSocket(bookingData.getAddr(), bookingData.getPort(), new LocoSocketHandler() {
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

        socket = new LocoSocket(checkInData.getHost(), checkInData.getPort(), new LocoSocketHandlerImpl(this), Executors.newFixedThreadPool(1));
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

        Thread.ofVirtual().start(() -> {
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
        });
    }

    public ChatRoom getChatRoom(long chatId) {
        if (chatRooms.containsKey(chatId)) return chatRooms.get(chatId);
        ChatInfoOut req = new ChatInfoOut(chatId);

        ChatInfoIn res = new ChatInfoIn(socket.writeAndRead(new LocoPacket("CHATINFO", req.toBson())).getBody());
        if (res.getStatus() != 0) return null;

        GetMemberOut memberReq = new GetMemberOut(chatId);
        GetMemberIn memberRes = new GetMemberIn(socket.writeAndRead(new LocoPacket("GETMEM", memberReq.toBson())).getBody());

        ChatRoom chatRoom = null;
        if (res.getType().equals(ChatRoomType.OPEN_CHAT) || res.getType().equals(ChatRoomType.OPEN_DIRECT)) {
            InfoLinkOut linkReq = new InfoLinkOut(res.getLinkId());
            InfoLinkIn linkRes = new InfoLinkIn(socket.writeAndRead(new LocoPacket("INFOLINK", linkReq.toBson())).getBody());

            chatRoom = new ChatRoom(this, chatId, res.getType(), linkRes.getName(), res.getLinkId());
            for (int i = 0; i < memberRes.getMembers().size(); i++) {
                JsonObject json = memberRes.getMembers().get(i).getAsJsonObject();
                Member member = new Member(this, chatRoom, json.get("userId").getAsLong(), json.get("type").getAsInt(), json.get("nickName").getAsString(), json.get("pi").getAsString(), json.get("fpi").getAsString(), json.get("opi").getAsString(), json.get("ptp").getAsInt() == 16 ? json.get("pli").getAsLong() : 0, json.get("mt").getAsInt(), json.get("ptp").getAsInt());
                chatRoom.getMembers().put(member.getUserId(), member);
            }
        } else if (res.getType().equals(ChatRoomType.DIRECT_CHAT)) {
            chatRoom = new ChatRoom(this, chatId, res.getType(), res.getDisplayMembers().get(0).getAsJsonObject().get("nickName").getAsString(), 0);
            for (int i = 0; i < memberRes.getMembers().size(); i++) {
                JsonObject json = memberRes.getMembers().get(i).getAsJsonObject();
                Member member = new Member(this, chatRoom, json.get("userId").getAsLong(), json.get("type").getAsInt(), json.get("nickName").getAsString(), json.get("profileImageUrl").getAsString(), json.get("fullProfileImageUrl").getAsString(), json.get("originalProfileImageUrl").getAsString(), 0, MemberType.MEMBER, 0);
                chatRoom.getMembers().put(member.getUserId(), member);
            }
        } else if (res.getType().equals(ChatRoomType.GROUP_CHAT)) {
            if (!res.getChatMetas().isEmpty()) {
                JsonArray chatMetas = res.getChatMetas();
                chatRoom = new ChatRoom(this, chatId, res.getType(), chatMetas.get(0).getAsJsonObject().get("content").getAsString(), 0);
            } else {
                JsonArray displayMembers = res.getDisplayMembers();
                String name = "";
                for (int i = 0; i < displayMembers.size(); i++) {
                    name += displayMembers.get(i).getAsJsonObject().get("nickName").getAsString() + ", ";
                }
                chatRoom = new ChatRoom(this, chatId, res.getType(), name, 0);
            }

            for (int i = 0; i < memberRes.getMembers().size(); i++) {
                JsonObject json = memberRes.getMembers().get(i).getAsJsonObject();
                Member member = new Member(this, chatRoom, json.get("userId").getAsLong(), json.get("type").getAsInt(), json.get("nickName").getAsString(), json.get("profileImageUrl").getAsString(), json.get("fullProfileImageUrl").getAsString(), json.get("originalProfileImageUrl").getAsString(), 0, MemberType.MEMBER, 0);
                chatRoom.getMembers().put(member.getUserId(), member);
            }
        }

        return chatRoom;
    }

    public boolean sendMessage(long chatId, String message, String extra) {
        WriteOut req = new WriteOut();
        req.setChatId(chatId);
        req.setType(MessageType.TEXT);
        req.setMessage(message);
        req.setExtra(extra);

        WriteIn res = new WriteIn(socket.writeAndRead(new LocoPacket("WRITE", req.toBson())).getBody());
        return res.getStatus() == 0;
    }

    public boolean sendMessage(long chatId, String message) {
        return sendMessage(chatId, message, "{}");
    }

    public boolean sendJpg(long chatId, byte[] image) {
        ImageUtil.ImageMeta meta = ImageUtil.getImageMeta(image);
        if (!meta.isValidJpeg()) return false;
        LocoSocket postSocket = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            ShipOut so = new ShipOut();
            so.setChatId(chatId);
            so.setSize(image.length);
            so.setCheckSum(ByteUtil.byteArrayToHexString(md.digest(image)));
            ShipIn si = new ShipIn(socket.writeAndRead(new LocoPacket("SHIP", so.toBson())).getBody());
            if (si.getStatus() != 0) return false;

            final CompletableFuture<Integer> future = new CompletableFuture<>();
            postSocket = new LocoSocket(si.getVhost(), si.getPort(), new LocoSocketHandler() {
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
            po.setWidth(meta.getWidth());
            po.setHeight(meta.getHeight());

            PostIn pi = new PostIn(postSocket.writeAndRead(new LocoPacket("POST", po.toBson())).getBody());
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

    public boolean reply(Message target, String message) {
        JsonObject extraObject = new JsonObject();
        extraObject.addProperty("src_logId", target.getLogId());
        extraObject.addProperty("src_userId", target.getAuthor().getUserId());
        extraObject.addProperty("src_message", target.getMessage());
        extraObject.addProperty("src_type", target.getType());
        extraObject.addProperty("src_linkId", target.getChatRoom().getLinkId());

        WriteOut req = new WriteOut();
        req.setChatId(target.getChatRoom().getChatId());
        req.setMessage(message);
        req.setType(MessageType.REPLY);
        req.setExtra(extraObject.toString());

        JsonObject res = socket.writeAndRead(new LocoPacket("WRITE", req.toBson())).getBodyJson();
        return res.get("status").getAsInt() == 0;
    }

    public boolean kickMember(long chatId, long linkId, long memberId) {
        KickMemberOut req = new KickMemberOut(chatId, linkId, memberId);

        JsonObject res = socket.writeAndRead(new LocoPacket("KICKMEM", req.toBson())).getBodyJson();
        // 채팅 로그 저장할거면 이거도 처리
        return res.get("status").getAsInt() == 0;
    }

    public long getMyUserId() {
        return loginData.userId;
    }

}
