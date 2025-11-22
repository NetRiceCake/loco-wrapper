package com.github.netricecake.message;

public interface LocoResponse {

    String getMethod();

    void fromBson(byte[] bson);

}
