package com.github.netricecake.loco;

public interface LocoSocektHandler {

    void onPacket(LocoPacket packet);

    void onConnect();

    void onDisconnect();

    void onError(Exception e);

}
