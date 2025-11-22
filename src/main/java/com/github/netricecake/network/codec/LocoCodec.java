package com.github.netricecake.network.codec;

import com.github.netricecake.network.LocoPacket;
import com.github.netricecake.util.ByteUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LocoCodec extends MessageToMessageCodec<byte[], LocoPacket> {

    private LocoPacket currentLocoPacket = null;
    private byte[] buffer = new byte[0];

    private BlockingQueue<LocoPacket> locoPacketQueue;

    public LocoCodec(BlockingQueue<LocoPacket> locoPacketHandler) {
        this.locoPacketQueue = locoPacketHandler;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LocoPacket packet, List<Object> list) throws Exception {
        byte[] packetId = ByteUtil.intToByteArrayLE(packet.getPacketId());
        byte[] statusCode = ByteUtil.shortToByteArrayLE(packet.getStatusCode());
        byte[] method = new byte[11];
        System.arraycopy(packet.getMethod().getBytes(), 0, method, 0, packet.getMethod().length());
        byte[] bodyType = { packet.getBodyType() };
        byte[] body = packet.getBody();
        byte[] bodyLength = ByteUtil.intToByteArrayLE(body.length);
        list.add(ByteUtil.concatBytes(packetId, statusCode, method, bodyType, bodyLength, body));
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> list) throws Exception {
        if (bytes == null) return;
        buffer = ByteUtil.concatBytes(buffer, bytes);
        do {
            if (currentLocoPacket == null) {
                if (buffer.length < 22) return;
                int id = ByteUtil.byteArrayToIntLE(ByteUtil.sliceBytes(buffer, 0, 4));
                short statusCode = ByteUtil.byteArrayToShortLE(ByteUtil.sliceBytes(buffer, 4, 2));
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < 11; i++) {
                    if ((buffer[6 + i] & 0xFF) == 0) break;
                    sb.append((char) buffer[6 + i]);
                }
                String method = sb.toString();
                byte bodyType = buffer[17];
                int bodyLength = ByteUtil.byteArrayToIntLE(ByteUtil.sliceBytes(buffer, 18, 4));
                currentLocoPacket = new LocoPacket(id, statusCode, method, bodyType, bodyLength, null);
                buffer = ByteUtil.sliceBytes(buffer, 22, buffer.length - 22);
            }
            if (currentLocoPacket.getBodyLength() > buffer.length) break;
            byte[] body = ByteUtil.sliceBytes(buffer, 0, currentLocoPacket.getBodyLength());
            buffer = ByteUtil.sliceBytes(buffer, currentLocoPacket.getBodyLength(), buffer.length - currentLocoPacket.getBodyLength());
            currentLocoPacket.setBody(body);
            locoPacketQueue.put(currentLocoPacket);
            currentLocoPacket = null;
        } while (buffer.length > 0);

    }

}
