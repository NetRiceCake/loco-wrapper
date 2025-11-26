package com.github.netricecake.loco.codec;

import com.github.netricecake.loco.crypto.CryptoManager;
import com.github.netricecake.loco.util.ByteUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class SecureLayerCodec extends MessageToMessageCodec<byte[], byte[]> {

    private CryptoManager cryptoManager;

    private int currentLength = -1;
    private byte[] buffer = new byte[0];

    public SecureLayerCodec(CryptoManager cryptoManager) {
        this.cryptoManager = cryptoManager;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> list) throws Exception {
        byte[] encryptedBody = cryptoManager.encryptMessage(bytes);
        byte[] packet = ByteUtil.concatBytes(ByteUtil.intToByteArrayLE(encryptedBody.length), encryptedBody);
        list.add(packet);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> list) throws Exception {
        buffer = ByteUtil.concatBytes(buffer, bytes);
        do {
            if (currentLength == -1) {
                if (buffer.length < 4) break;
                currentLength = ByteUtil.byteArrayToIntLE(ByteUtil.sliceBytes(buffer, 0, 4));
                buffer = ByteUtil.sliceBytes(buffer, 4, buffer.length - 4);
            }
            if (currentLength > buffer.length) break;
            byte[] packet = ByteUtil.sliceBytes(buffer, 0, currentLength);
            buffer = ByteUtil.sliceBytes(buffer, currentLength, buffer.length - currentLength);
            currentLength = -1;
            list.add(cryptoManager.decryptMessage(packet));
        } while (buffer.length > 0);
    }

}
