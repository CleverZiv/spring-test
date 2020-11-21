package com.leng.io.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;


/**
 * @Classname AioServer
 * @Date 2020/11/21 14:55
 * @Autor lengxuezhang
 */
public class AioServer {
    private static final Logger logger = LoggerFactory.getLogger(AioServer.class);

    private AsynchronousServerSocketChannel serverChannel;
    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int DEFAULT_PORT = 8321;


    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void start() {
        // 获取 AsynchronousServerSocketChannel
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            // 绑定端口
            serverChannel.bind(new InetSocketAddress(DEFAULT_SERVER, DEFAULT_PORT));
            logger.info("启动服务器，开始监听端口：{}", DEFAULT_PORT);
            // 服务端通过 accept()来等待接收客户端的连接请求，该方法是非阻塞的。
            // 第一个参数是“附件”的意思，可以放处理时需要的额外的信息；第二个参数是回调函数，用来处理 accept 方法结束时的结果
            while (true) {
                // accept() 是异步调用，也就是说主线程调用 accept 方法后，会立即返回执行下一行代码。所以这里需要加一个循环，使服务器循环等待
                // 但由于加了循环之后，服务端会不停地调用 accpet 函数，浪费系统资源
                serverChannel.accept(null, new AcceptHandler());
                // System.in.read()是一个阻塞的函数，会将服务端阻塞在这里，使得主线程不会过早的返回（实际应用时是不可能这样做的）
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }

        //

    }

    // 回调接口 CompletionHandler 使用了泛型，可以限制 attachment的类型和返回值的类型，在这里返回值的类型是 与客户端建立连接的AsynchronousSocketChannel
    private class AcceptHandler implements java.nio.channels.CompletionHandler<java.nio.channels.AsynchronousSocketChannel, Object> {
        /**
         * 表明 accept 方法正常返回
         * @param result
         * @param attachment
         */
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            // 首先保证 服务端 继续等待监听客户端的连接请求。这里虽然有了递归调用的存在，但系统会对递归调用做限制。
            if(serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }
            AsynchronousSocketChannel clientChannel = result;
            // 服务器需要读取来自客户端的消息，读取完成之后的操作，放在回调函数 ClientHandler 的逻辑中
            if(clientChannel != null && clientChannel.isOpen()) {
                // 读写的 IO 操作同样都是异步的，也需要回调函数
                ClientHandler handler = new ClientHandler(clientChannel);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // 尝试使用一下 attachment
                Map<String, Object> info = new HashMap<>();
                info.put("type", "read");
                info.put("buffer",  buffer);
                clientChannel.read(buffer, info, handler);

            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            // 处理错误的逻辑
            logger.info("accept 方法执行错误");
        }
    }

    /**
     * 回调函数是用来进行 IO 操作的，IO 操作的返回值都是操作的字节数量，因此返回值的类型是 Integer
     */
    private class ClientHandler implements CompletionHandler<Integer, Object> {

        private AsynchronousSocketChannel clientChannel;
        public ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }
        @Override
        public void completed(Integer result, Object attachment) {
            Map<String, Object> info = (Map<String, Object>) attachment;
            String type = (String) info.get("type");
            if(type.equals("read")) {
                // 服务端已经调用完 read 方法，此时的 buffer中已经有了从客户端通道写入的信息
                ByteBuffer buffer = (ByteBuffer) info.get("buffer");
                buffer.flip(); // 转换为读模式
                info.put("type", "write");
                // 将buffer中的值写入到客户端通道，实现“回声”
                logger.info(new String(buffer.array()));
                clientChannel.write(buffer, info, this);
                buffer.clear();
            } else if(type.equals("write")) {
                // 服务端完成写操作之后，需要继续监听客户端
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                info.put("type", "read");
                info.put("buffer",  buffer);
                clientChannel.read(buffer, info, this);

            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            // 处理错误的逻辑
            logger.info("ClientHandler 方法执行错误");
        }
    }

    public static void main(String[] args) {
        AioServer server = new AioServer();
        server.start();
    }
}
