package com.github.netricecake.network;

@FunctionalInterface
public interface LocoPacketHandler {
    void onPacket(LocoPacket packet);
}
