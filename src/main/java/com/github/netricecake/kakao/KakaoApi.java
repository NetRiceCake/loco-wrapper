package com.github.netricecake.kakao;

import com.github.netricecake.kakao.exception.BadCredentialsException;
import com.github.netricecake.kakao.exception.InvalidDeviceNameException;
import com.github.netricecake.kakao.exception.InvalidDeviceUUIDException;
import com.github.netricecake.kakao.exception.UnregisteredDeviceException;
import com.github.netricecake.loco.packet.inbound.GetConfIn;
import com.github.netricecake.loco.packet.outbound.GetConfOut;
import com.github.netricecake.loco.util.ByteUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class KakaoApi {


    public final static String AGENT = "android";
    public final static String VERSION = "25.9.2";
    public final static String OS_VERSION = "13";
    public final static String API_LEVEL = "33";
    public final static String LANGUAGE = "ko";

    public final static String PROTOCOL_VERSION = "1";
    public final static int NETWORK_TYPE = 0; // 0 : WIFI, 3: Cellular
    public final static String MCCMNC = "45006"; // 앞자리 세자리(한국) 450 고정, 뒤에 두자리 SKT: 05 KT: 08 LGU+: 06

    public final static String ALLOW_LIST_URL = "https://katalk.kakao.com/android/account/allowlist.json";
    public final static String LOGIN_URL = "https://katalk.kakao.com/android/account/login.json";
    public final static String PASSCODE_GENERATE_URL = "https://katalk.kakao.com/android/account/passcodeLogin/generate";
    public final static String REGISTER_DEVICE_URL = "https://katalk.kakao.com/android/account/passcodeLogin/registerDevice";
    public final static String CANCEL_REGISTER_URL = "https://katalk.kakao.com/android/account/passcodeLogin/cancel";
    public final static String BOOKING_URL = "booking-loco.kakao.com";
    public final static int BOOKING_PORT = 443;

    public final static String AUTH_USER_AGENT = String.format("KT/%s An/%s %s", VERSION, OS_VERSION, LANGUAGE);
    public final static String AUTH_HEADER_AGENT = String.format("%s/%s/%s", AGENT, VERSION, LANGUAGE);

    public final static int UUID_LENGTH = 64;

    private final static OkHttpClient client = new OkHttpClient();;

    private final static Gson gson = new Gson();

    public static LoginData loginRequest(String email, String password, String deviceName, String deviceUuid) throws IOException, InvalidDeviceNameException, InvalidDeviceUUIDException, BadCredentialsException, UnregisteredDeviceException {
        if (deviceUuid == null || deviceUuid.length() != UUID_LENGTH) throw new InvalidDeviceUUIDException();
        if (!checkAllowedDevice(deviceName)) throw new InvalidDeviceNameException();
        RequestBody body = new FormBody.Builder().add("password", password)
                .add("device_name", deviceName)
                .add("foced", "false")
                .add("permanent", "true")
                .add("email", email)
                .add("device_uuid", deviceUuid).build();
        Request.Builder builder = new Request.Builder().url(LOGIN_URL).post(body);
        builder.addHeader("X-VC", calculateXVC(email))
                .addHeader("Accept-Language", LANGUAGE)
                .addHeader("User-Agent", AUTH_USER_AGENT)
                .addHeader("A", AUTH_HEADER_AGENT);

        Response response = client.newCall(builder.build()).execute();
        JsonObject jsonObject = new JsonParser().parse(response.body().string()).getAsJsonObject();
        int status = jsonObject.get("status").getAsInt();

        // 12 비번 틀림 30 이메일 틀림
        if (status == 12 || status == 30) throw new BadCredentialsException();
        if (status == -100) throw new UnregisteredDeviceException();
        if (status == 0) {
            LoginData data = new LoginData();
            data.userId = jsonObject.get("userId").getAsLong();
            data.countryIso = jsonObject.get("countryIso").getAsString();
            data.countryCode = jsonObject.get("countryCode").getAsString();
            data.accountId = jsonObject.get("accountId").getAsLong();
            data.accessToken = jsonObject.get("access_token").getAsString();
            data.refreshToken = jsonObject.get("refresh_token").getAsString();
            data.tokenType = jsonObject.get("token_type").getAsString();
            data.autoLoginAccountId = jsonObject.get("autoLoginAccountId").getAsString();
            data.displayAccountId = jsonObject.get("displayAccountId").getAsString();
            data.mainDeviceAgentName = jsonObject.get("mainDeviceAgentName").getAsString();
            data.mainDeviceAppVersion = jsonObject.get("mainDeviceAppVersion").getAsString();
            data.recipe = jsonObject.get("recipe").getAsString();

            return data;
        }
        return null;
    }

    public static Map.Entry<String, Integer> generatePasscode(String email, String password, String device_name, String deviceUuid) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("permanent", true);
        JsonObject deviceObject = new JsonObject();
        deviceObject.addProperty("name", device_name);
        deviceObject.addProperty("uuid", deviceUuid);
        deviceObject.addProperty("model", device_name);
        deviceObject.addProperty("osVersion", API_LEVEL);
        jsonObject.add("device", deviceObject);

        Request.Builder builder = generateHeader(email).url(PASSCODE_GENERATE_URL).post(RequestBody.create(gson.toJson(jsonObject), MediaType.parse("application/json; charset=utf-8")));
        String json = client.newCall(builder.build()).execute().body().string();
        JsonObject body = JsonParser.parseString(json).getAsJsonObject();
        return Map.entry(body.get("passcode").getAsString(), body.get("remainingSeconds").getAsInt());
    }

    public static boolean registerDevice(String email, String password, String deviceUuid) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("password", password);
        JsonObject deviceObject = new JsonObject();
        deviceObject.addProperty("uuid", deviceUuid);
        jsonObject.add("device", deviceObject);

        Request.Builder builder = generateHeader(email).url(REGISTER_DEVICE_URL).post(RequestBody.create(gson.toJson(jsonObject), MediaType.parse("application/json; charset=utf-8")));

        Request request = builder.build();
        try {
            int remainTime = -7777;
            do {
                JsonObject resBody = JsonParser.parseString(client.newCall(request).execute().body().string()).getAsJsonObject();
                if (resBody.get("status").getAsInt() == 0) return true;
                if (resBody.get("status").getAsInt() != -100) return false;
                int remain = resBody.get("remainingSeconds").getAsInt();
                int interval = resBody.get("nextRequestIntervalInSeconds").getAsInt();
                remainTime = remain - interval;
                Thread.sleep((long) interval * 1000);
            } while(remainTime > 0);
            builder = generateHeader(email).url(CANCEL_REGISTER_URL).post(RequestBody.create(gson.toJson(jsonObject), MediaType.parse("application/json; charset=utf-8")));
            client.newCall(builder.build()).execute();
        } catch (Exception e) {}
        return false;
    }

    public static GetConfIn getBookingData(long userId) {
        byte[] id = ByteUtil.intToByteArrayLE(1000);
        byte[] method = new byte[11];
        System.arraycopy("GETCONF".getBytes(), 0, method, 0, 7);
        byte[] b = new GetConfOut(MCCMNC, AGENT, userId).toBson();
        byte[] m = ByteUtil.concatBytes(id, new byte[2], method, new byte[1], ByteUtil.intToByteArrayLE(b.length), b);
        try {
            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketFactory.createSocket(BOOKING_URL, BOOKING_PORT);
            socket.startHandshake();

            OutputStream writer = socket.getOutputStream();
            InputStream reader = socket.getInputStream();

            writer.write(m);
            writer.flush();

            byte[] res = new byte[4096];
            reader.read(res);

            int len = ByteUtil.byteArrayToIntLE(ByteUtil.sliceBytes(res, 18, 4));
            GetConfIn r = new GetConfIn();
            r.fromBson(ByteUtil.sliceBytes(res, 22, len));

            socket.close();

            return r;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkAllowedDevice(String deviceName) throws IOException {
        Request.Builder builder = new Request.Builder().url(String.format("%s?model_name=%s", ALLOW_LIST_URL, deviceName)).get();
        builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
        builder.addHeader("Accept-Language", LANGUAGE);
        builder.addHeader("User-Agent", AUTH_USER_AGENT);
        builder.addHeader("A", AUTH_HEADER_AGENT);
        builder.addHeader("Accept-Encoding", "gzip");

        Request request = builder.build();

        return JsonParser.parseString(client.newCall(request).execute().body().string()).getAsJsonObject().get("allowlisted").getAsBoolean();
    }

    public static Request.Builder generateHeader(String email) {
        Request.Builder builder = new Request.Builder();
        builder.addHeader("X-VC", calculateXVC(email))
            .addHeader("User-Agent", AUTH_USER_AGENT)
            .addHeader("A", AUTH_HEADER_AGENT);

        return builder;
    }

    public static String calculateXVC(String email) {
        try {
            String str = String.format("BARD|%s|DANTE|%s|SIAN", AUTH_USER_AGENT, email);
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(str.getBytes());

            return ByteUtil.byteArrayToHexString(digest.digest()).substring(0, 16).toLowerCase();
        } catch (NoSuchAlgorithmException e) {}
        return "";
    }

    public static class LoginData {
        public long userId;
        public String countryIso;
        public String countryCode;
        public long accountId;
        public String accessToken;
        public String refreshToken;
        public String tokenType;
        public String autoLoginAccountId;
        public String displayAccountId;
        public String mainDeviceAgentName;
        public String mainDeviceAppVersion;
        public String recipe;

        public LoginData(String json) {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            userId = jsonObject.get("userId").getAsLong();
            countryIso = jsonObject.get("countryIso").getAsString();
            countryCode = jsonObject.get("countryCode").getAsString();
            accountId = jsonObject.get("accountId").getAsLong();
            accessToken = jsonObject.get("access_token").getAsString();
            refreshToken = jsonObject.get("refresh_token").getAsString();
            tokenType = jsonObject.get("token_type").getAsString();
            autoLoginAccountId = jsonObject.get("autoLoginAccountId").getAsString();
            displayAccountId = jsonObject.get("displayAccountId").getAsString();
            mainDeviceAgentName = jsonObject.get("mainDeviceAgentName").getAsString();
            mainDeviceAppVersion = jsonObject.get("mainDeviceAppVersion").getAsString();
            recipe = jsonObject.get("recipe").getAsString();
        }

        public LoginData() {}

        public String toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", userId);
            jsonObject.addProperty("countryIso", countryIso);
            jsonObject.addProperty("countryCode", countryCode);
            jsonObject.addProperty("accountId", accountId);
            jsonObject.addProperty("access_token", accessToken);
            jsonObject.addProperty("refresh_token", refreshToken);
            jsonObject.addProperty("token_type", tokenType);
            jsonObject.addProperty("autoLoginAccountId", autoLoginAccountId);
            jsonObject.addProperty("displayAccountId", displayAccountId);
            jsonObject.addProperty("mainDeviceAgentName", mainDeviceAgentName);
            jsonObject.addProperty("mainDeviceAppVersion", mainDeviceAppVersion);
            jsonObject.addProperty("recipe", recipe);

            return gson.toJson(jsonObject);
        }
    }

}
