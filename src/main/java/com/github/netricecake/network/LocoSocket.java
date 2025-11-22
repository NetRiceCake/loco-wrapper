package com.github.netricecake.network;

import com.github.netricecake.crypto.CryptoManager;
import com.github.netricecake.network.codec.LocoCodec;
import com.github.netricecake.network.codec.SecureLayerCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LocoSocket {

    @Getter
    private String ip;
    @Getter
    private int port;

    private CryptoManager cryptoManager;

    private Channel channel;
    private EventLoopGroup eventLoopGroup;

    @Getter
    private boolean alive = false;

    private BlockingQueue<LocoPacket> locoPacketQueue = new LinkedBlockingQueue<>();

    public LocoSocket(String ip, int port) {
        this.ip = ip;
        this.port = port;
        cryptoManager = new CryptoManager();
    }

    public void connect() throws Exception {
        byte[] handshakePacket = cryptoManager.generateHandshakeMessage();
        eventLoopGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.remoteAddress(new InetSocketAddress(ip, port))
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new ByteArrayEncoder());
                        pipeline.addLast(new ByteArrayDecoder());
                    }
                });
        channel = bootstrap.connect().sync().channel();
        alive = true;
        channel.writeAndFlush(handshakePacket).sync();
        channel.pipeline().addLast(new SecureLayerCodec(cryptoManager));
        channel.pipeline().addLast(new LocoCodec(locoPacketQueue));
        new Thread() {
            @Override
            public void run() {
                try {
                    channel.closeFuture().sync();
                    eventLoopGroup.shutdownGracefully();
                    locoPacketQueue.offer(null);
                    alive = false;
                } catch (Exception e) {}
            }
        }.start();
    }

    public void write(LocoPacket packet) {
        if (!alive) return;
        channel.writeAndFlush(packet);
    }

    public LocoPacket read() throws Exception {
        if (!alive) return null;
        return locoPacketQueue.poll(100000, TimeUnit.SECONDS);
    }

    public void close() {
        channel.close();
        eventLoopGroup.shutdownGracefully();
        alive = false;
    }

}
