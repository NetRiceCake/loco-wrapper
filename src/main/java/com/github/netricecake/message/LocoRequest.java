package com.github.netricecake.message;

public interface LocoRequest {

    String getMethod();

    byte[] toBson();

}
