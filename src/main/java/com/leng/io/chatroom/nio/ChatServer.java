package com.leng.io.chatroom.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Classname AioServer
 * @Date 2020/11/18 0:03
 * @Autor lengxuezhang
 */
public class ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static final int DEFAULT_PORT = 8888;
    private static final  int BUFFER = 1024;
    private static final String Quit = "quit";

    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private ServerSocketChannel server;
    private Selector selector;
    private int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }
    public ChatServer(int port) {
        this.port = port;
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.info("{}关闭异常", closeable.toString());
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        // client.read(rBuffer)：将通道中的数据读入到 rBuffer，直至没有数据可读
        while(client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(rBuffer);
    }

    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        // 找到目前在线的所有客户端  selector.keys() -- 返回所有注册在selector上的通道
        for (SelectionKey key : selector.keys()) {
            Channel connectedClient = key.channel();
            if(connectedClient instanceof ServerSocketChannel) {
                continue;
            }
            if(key.isValid() && !client.equals(connectedClient)) {
                // 转发到其它客户端
                wBuffer.clear();
                // 往 ByteBuffer中写入数据用put
                wBuffer.put(("来自客户端："+client.socket().getPort() + "信息：" +fwdMsg).getBytes());
                // 切换为 读 状态
                wBuffer.flip();
                while (wBuffer.hasRemaining()) {
                    ((SocketChannel)connectedClient).write(wBuffer);
                }

            }
        }
    }

    private boolean readyToQuit(String msg) {
        return Quit.equals(msg);
    }

    private void handles(SelectionKey key) throws IOException {
        // accept 事件
        if(key.isAcceptable()) {
            // 获取服务端通道
            ServerSocketChannel server = (ServerSocketChannel)key.channel();
            // 获取客户端通道，非阻塞模式下，该方法不阻塞
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            // 将客户端的可读事件注册到 selector 上。意思就是，当客户端有消息发出时（客户端通道可读），触发该事件
            client.register(selector, SelectionKey.OP_READ);
            logger.info("客户端:{} 已连接", client.socket().getPort());
        } else if (key.isReadable()) {
            // 读取客户端消息
            SocketChannel client = (SocketChannel) key.channel();
            String fwdMsg = receive(client);
            if(fwdMsg.isEmpty()) {
                // cancel() 函数可以将该监听事件取消掉
                key.cancel();
                // selector的select()是阻塞式的，通过wakeup()，将selector唤醒
                selector.wakeup();
                logger.info("客户端：{} 出现异常", client.socket().getPort());
            } else {
                // 转发给其它客户端
                forwardMessage(client, fwdMsg);
                if(readyToQuit(fwdMsg)) {
                    key.cancel();
                    selector.wakeup();
                }
            }


        }
    }

    private void start(){
        try {
            // 创建 ServerSocketChannel
            server = ServerSocketChannel.open();
            // 将通道置为 非阻塞 模式
            server.configureBlocking(false);
            // 将通道关联的 ServerSocket 绑定到指定监听端口；socket()--获取通道关联的 ServerSocket；bind()--绑定指定端口
            // 所以这里实际上要明确一点：端口永远是与 Socket 绑定在一起，Socket是一个四元组对象（服务端ip+端口，客户端ip+端口），通道是依赖于Socket的；
            server.socket().bind(new InetSocketAddress(port));
            // 创建 Selector
            selector = Selector.open();
            // 将通道注册到 selector，选择 accept 模式。可以理解为：
            // 当客户端 socket 与服务端 socket建立连接时（该动作与 Channel、Selector 都无关，是 Socket 的行为），会改变 Channel 的状态为 accept，而 Selector 即监听通道的该状态
            // 注意：selector上可以注册多个通道
            server.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("服务端已启动，开始监听端口：{}", port);

            while (true) {
                // select() 方法是阻塞的，只有当返回值大于1时，会往下执行。返回值代表当前被触发的事件数量
                selector.select();
                // 获取当前被触发的事件。关于事件的所有信息（SocketChannel等）都被包装在 SelectionKey 中
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    handles(iterator.next());
                    // 不能从 selector.selectedKeys() 集合中直接 remove 掉，需要使用迭代器，为什么？
                    iterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 释放selector时，会首先把注册在selector上的事件解除注册，同时把对应的通道关闭。
            close(selector);
        }

    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(7787);
        chatServer.start();
    }
}
