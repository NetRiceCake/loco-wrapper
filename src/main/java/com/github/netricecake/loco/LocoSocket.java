package com.github.netricecake.loco;

import com.github.netricecake.loco.crypto.CryptoManager;
import com.github.netricecake.loco.codec.LocoCodec;
import com.github.netricecake.loco.codec.SecureLayerCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class LocoSocket {

    @Getter
    private final String ip;
    @Getter
    private final int port;

    @Getter
    private final CryptoManager cryptoManager = new CryptoManager();;

    private Channel channel;
    private EventLoopGroup eventLoopGroup;

    @Getter
    private boolean alive = false;

    private final LocoSocketHandler locoSocektHandler;

    private final ExecutorService handlerPool;

    private final Map<Integer, Future<LocoPacket>> waitList = new HashMap<>();

    private int packetIdCounter = 1000;

    public LocoSocket(String ip, int port, LocoSocketHandler locoSocektHandler, ExecutorService handlerPool) {
        this.ip = ip;
        this.port = port;
        this.locoSocektHandler = locoSocektHandler;
        this.handlerPool = handlerPool;
    }

    public void connect() throws IOException {
        if (alive) throw new IOException("Already connected");
        try {
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
            channel.writeAndFlush(cryptoManager.generateHandshakeMessage()).sync();
            channel.pipeline().addLast(new SecureLayerCodec(cryptoManager));
            channel.pipeline().addLast(new LocoCodec(locoSocektHandler, handlerPool, waitList));
            handlerPool.execute(locoSocektHandler::onConnect);
            new Thread() {
                @Override
                public void run() {
                    try {
                        channel.closeFuture().sync();
                        eventLoopGroup.shutdownGracefully();
                        handlerPool.execute(locoSocektHandler::onDisconnect);
                        alive = false;
                    } catch (Exception e) {
                        handlerPool.execute(() -> {
                            locoSocektHandler.onError(e);
                        });
                    }
                }
            }.start();
        } catch (InterruptedException e) {
            handlerPool.execute(() -> {
                locoSocektHandler.onError(e);
            });
        }
    }

    public void write(LocoPacket packet) {
        if (!alive) return;
        synchronized (this) {
            if (packet.getPacketId() == -1) packet.setPacketId(++packetIdCounter);
        }
        channel.writeAndFlush(packet);
    }

    public LocoPacket writeAndRead(LocoPacket packet) {
        if (!alive) return null;
        int packetId = packet.getPacketId();
        synchronized (this) {
            if (packet.getPacketId() == -1) packetId = ++packetIdCounter;
        }
        packet.setPacketId(packetId);
        Future<LocoPacket> future = new CompletableFuture<>();
        waitList.put(packetId, future);
        channel.writeAndFlush(packet);
        try {
            LocoPacket result = future.get(5, TimeUnit.SECONDS);
            waitList.remove(packetId);
            return result;
        } catch (Exception e) {
            handlerPool.execute(() -> {
                locoSocektHandler.onError(e);
            });
        }
        return null;
    }

    public void close() {
        if (!alive) return;
        handlerPool.execute(() -> {
            locoSocektHandler.onDisconnect();
        });
        eventLoopGroup.shutdownGracefully();
        channel.close();
        handlerPool.execute(locoSocektHandler::onDisconnect);
        alive = false;
    }

}
