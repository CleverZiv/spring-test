package com.leng.io.chatroom.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Classname ChatServer
 * @Date 2020/11/21 22:26
 * @Autor lengxuezhang
 */
public class ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private static final  int BUFFER = 1024;
    private static final int THRADPOOL_SIZE = 8;
    private static final String Quit = "quit";

    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private int port;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;
    private List<ClientHanlder> connenctedClients;
    private Charset charset = Charset.forName("UTF-8");

    public ChatServer() {
        this(DEFAULT_PORT);
    }
    public ChatServer(int port) {
        this.port = port;
        this.connenctedClients = new ArrayList<>();
    }

    private boolean readyToQuit(String msg) {
        return Quit.equals(msg);
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.info("{}关闭异常", closeable.toString());
        }
    }

    private String receive(ByteBuffer buffer) {
        return String.valueOf(charset.decode(buffer));
    }

    private void removeClient(ClientHanlder hanlder) {
        AsynchronousSocketChannel channel = hanlder.getClientChannel();
        connenctedClients.remove(hanlder);
        logger.info("客户端：{}已断开连接", channel.toString());
        close(channel);
    }
    private void addClient(ClientHanlder hanlder) {
        connenctedClients.add(hanlder);
        logger.info("客户端:{}已经连接", hanlder.getClientChannel().toString());
    }

    private void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(THRADPOOL_SIZE);
        try {
            // 将自定义的线程池放入到 AsynchronousChannelGroup 中，交由它管理
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            // 将 channelGroup 传入，会使得 serverChannel 不再使用默认的 channelGroup，而使用自定义的channelGroup
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            serverChannel.bind(new InetSocketAddress(DEFAULT_SERVER, DEFAULT_PORT));
            logger.info("启动服务器，监听端口：{}", DEFAULT_PORT);

            while (true) {
                serverChannel.accept(null, new AcceptHandler());
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
            // 是不是可以注释掉?  不可以，注释掉之后只能与一个客户端连接
            if(serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }
            if(clientChannel != null && clientChannel.isOpen()) {
                // clientChannel 的读写也都是异步的，同样要实现一个 Handler
                ClientHanlder handler = new ClientHanlder(clientChannel);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
                // 第一个buffer：接收clientChannle 读到的数据 ；第二个 buffer：Attachment 我们希望在回调函数中知道读进来的数据是什么，而这个数据存储在 buffer中
                // 第二个参数，在read回调的时候，会作为 Attachment 传入到 handler 中
                // todo  需要把这个新用户加入到在线用户列表中
                addClient(handler);
                clientChannel.read(buffer, buffer, handler);
                String msg = new String(buffer.array());
                forwardMsg(clientChannel, msg);

            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            logger.info("客户端连接失败" + exc);
        }
    }

    private void forwardMsg(AsynchronousSocketChannel clientChannel, String msg) {
        for(ClientHanlder connectedHandler : connenctedClients) {
            AsynchronousSocketChannel client = connectedHandler.getClientChannel();
            if(! client.equals(clientChannel)) {
                // 将消息写入各个客户端的通道
                ByteBuffer buffer = charset.encode(client.toString() + msg);
                client.write(buffer, null, connectedHandler);
            }
        }
    }

    private class ClientHanlder implements CompletionHandler<Integer, Object> {

        private AsynchronousSocketChannel clientChannel;
        public ClientHanlder(AsynchronousSocketChannel clientChannel) {
            this.clientChannel= clientChannel;
        }
        public AsynchronousSocketChannel getClientChannel() {
            return clientChannel;
        }

        /**
         *
         * @param result 原函数执行结果
         * @param attachment
         */
        @Override
        public void completed(Integer result, Object attachment) {
            ByteBuffer buffer = (ByteBuffer) attachment;
            // read 操作的回调:打印出信息在服务端，并且转发出去
            // 在具体实现读写逻辑时，由于我们服务端的写逻辑不会需要用到 attachment，可以通过区分 attachment（也就是buffer） 是否为null，来判断是 read操作还是write操作
            if(buffer != null) {
                // 读操作的回调
                if(result <= 0) {
                    // 读到的数据的字节长度 <=0，说明客户端异常
                    // TODO 将客户端移除在线客户列表
                    removeClient(this);
                } else {
                    buffer.flip();
                    String fwdMsg = receive(buffer);
                    logger.info("来自客户端:{}的消息：{}", clientChannel, fwdMsg);
                    // 转发消息
                    forwardMsg(clientChannel , fwdMsg);
                    buffer.clear();
                    if(readyToQuit(fwdMsg)) {
                        removeClient(this);
                    } else {
                        clientChannel.read(buffer, buffer, this);
                    }
                }
            }

            // write
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            logger.info("服务端读写客户端通道失败，{}", exc);
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
