package com.abc.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioClient {
    public static void main(String[] args) throws Exception {
        final SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        SocketAddress address = new InetSocketAddress("localhost", 8888);
        if (!clientChannel.connect(address)) {
            while (!clientChannel.finishConnect()) {
                System.out.println("进行重试连接");
                continue;
            }
        }
        clientChannel.write(ByteBuffer.wrap("hello".getBytes()));
        System.out.println("客户端已发送数据");
        System.in.read();
    }
}
