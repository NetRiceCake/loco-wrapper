package com.github.netricecake;

import com.github.netricecake.util.ByteUtil;

import java.security.SecureRandom;

public class Main {

    static String EMAIL = "";
    static String PASSWORD = "";
    static String DEVICE_NAME = "SM-X930"; // 갤럭시 탭 s11 울트라

    public static void main(String[] args) throws Exception {
        byte[] uuid = new byte[32];
        new SecureRandom().nextBytes(uuid);
        String deviceUuid = ByteUtil.byteArrayToHexString(uuid);
        KakaoTalkClient client = new KakaoTalkClient();
        client.login(EMAIL, PASSWORD, DEVICE_NAME, deviceUuid);
        client.test();
    }

}
