package com.leng.io.chatroom.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Classname ChatClient
 * @Date 2020/11/19 22:40
 * @Autor lengxuezhang
 */
public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 7787;
    private static final  int BUFFER = 1024;
    private static final String Quit = "quit";

    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private SocketChannel client;
    private Selector selector;
    private String host;
    private int port;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ChatClient() {
        this(DEFAULT_SERVER_HOST, DEFAULT_PORT);
    }

    public boolean readyToQuit(String msg) {
        return Quit.equals(msg);
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.info("{}关闭异常", closeable.toString());
        }
    }

    private String receive(SocketChannel client) throws IOException {
        // 缓冲区切换成写模式
        rBuffer.clear();
        while(client.read(rBuffer) > 0);
        rBuffer.flip(); // 切换读模式
        return String.valueOf(rBuffer);
    }

    public void send(String msg) throws IOException {
        if(msg.isEmpty()) {
            return;
        }
        wBuffer.clear();
        wBuffer.put(msg.getBytes());
        wBuffer.flip();
        while (wBuffer.hasRemaining()) {
            client.write(wBuffer);
        }
        // 检查用户是否退出
        if(readyToQuit(msg)) {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        // 连接就绪事件 CONNECT 事件
        if(key.isConnectable()) {
            SocketChannel client = (SocketChannel) key.channel();
            // isConnectionPending() 返回 true 则建立连接已经就绪，只需再调用finish()方法；如果是false，则还需等待
            if(client.isConnectionPending()) {
                // 正式建立连接
                logger.info("=====Connection====");
                client.finishConnect();
                // 处理用户输入的信息
                new Thread(new UserInputHandler(this)).start();
            }
            // 注册 读 事件，监控是否有消息转发过来
            client.register(selector, SelectionKey.OP_READ);
        } else if(key.isReadable()) {
            logger.info("=====read====");
            SocketChannel client = (SocketChannel) key.channel();
            String msg = receive(client);
            if(msg.isEmpty()) {
                // 服务器异常，客户端自行关闭
                close(selector);
                logger.info("服务端可能出现异常");
            } else {
                logger.info(msg);
            }
        }

        // 服务器转发消息 READ 事件


    }
    private void start() {
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);
            selector = Selector.open();
            // 客户端有了 SocketChannel 之后，会发送连接请求给对应的服务器端，一旦服务器端接收了链接请求，客户端的通道上就会出发 CONNECT 事件
            client.register(selector, SelectionKey.OP_CONNECT);
            // 正式向服务器发送连接请求
            client.connect(new InetSocketAddress(host, port));

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    handles(iterator.next());
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            logger.info("客户端强制退出");

        } finally {
            close(selector);
        }

    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient(DEFAULT_SERVER_HOST, DEFAULT_PORT);
        client.start();
    }
}
