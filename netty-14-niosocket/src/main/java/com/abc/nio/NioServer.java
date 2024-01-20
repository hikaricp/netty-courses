package com.abc.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class NioServer {
    public static void main(String[] args) throws Exception {
        // 创建一个服务端 Channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 指定 Channel 采用非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 指定要监听的端口
        serverSocketChannel.bind(new InetSocketAddress(8888));
        // 创建一个多路复用器
        Selector selector = Selector.open();
        // 将 Channel 注册到 Selector，并告诉 Selector 让其监听“接收 Client 连接事件”
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // select() 是一个阻塞方法，若阻塞 1 秒的时间到了，或在阻塞期间有 Channel 就绪了，都会打破阻塞
            if (selector.select(1000) == 0) {
                System.out.println("暂时没有就绪的 Channel");
                continue;
            }

            // 代码能执行到这里，说明已经有 Channel 就绪了
            // 获取所有就绪的 Channel 的 key
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey key : selectionKeys) {
                // 若当前 key 为 OP_ACCEPT，则说明当前 channel 是可以接收客户端连接的
                // 那么，这里的代码就是用于接收客户端连接的
                if (key.isAcceptable()) {
                    System.out.println("接收到Client的连接");
                    // 获取连接到 Server 的客户端 Channel，其是客户端 Channel 在 Server 端的代表（驻京办）
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    clientChannel.configureBlocking(false);
                    // 将客户端 Channel 注册到 Selector，并告诉 Selector 让其监听这个 Channel 中是否发生了读事件
                    clientChannel.register(selector, SelectionKey.OP_READ);
                }

                // 若当前 key 为 OP_READ，则说明当前 Channel 中有客户端发送来的数据
                // 那么，这里的代码就是用于读取 Channel 中的数据的
                if (key.isReadable()) {
                    try {// 创建 buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // 根据 key 获取其对应的 Channel
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        // 把 Channel 中的数据读取到 buffer
                        clientChannel.read(buffer);
                    } catch (IOException e) {
                        // 若在读取过程中发生异常，则直接取消该 key，即放弃该 Channel
                        key.cancel();
                    }

                }

                // 删除当前处理过的 key，以免重复处理
                selectionKeys.remove(key);
            } // end-for
        }
    }
}
