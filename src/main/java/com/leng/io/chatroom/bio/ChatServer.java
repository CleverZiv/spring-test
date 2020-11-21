package com.leng.io.chatroom.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Classname AioServer
 * @Date 2020/11/15 12:27
 * @Autor lengxuezhang
 */
public class ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static final int DEFAULT_SERVER_PORT = 9898;
    private static final String QUIT = "quit";

    private ServerSocket serverSocket;
    // 与服务端建立连接的客户端列表，key 为客户端端口号，value 为客户端的 输出流
    private Map<Integer, Writer> connectedClients;
    private ExecutorService executorService;

    public ChatServer() {
        executorService = Executors.newFixedThreadPool(10);
        connectedClients = new HashMap<>();
    }

    /**
     * 添加 socket 到客户端列表
     *
     * @param socket
     */
    public synchronized void addClient(Socket socket) throws IOException {
        if(socket != null) {
            connectedClients.put(socket.getPort(), new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            logger.info("客户端[{}]已连接到服务端", socket.getPort());
        }
    }

    /**
     * 清除 客户端连接
     * @param socket
     * @throws IOException
     */
    public synchronized void removeClient(Socket socket) throws IOException {
        if(socket != null) {
            int port = socket.getPort();
            if(connectedClients.containsKey(port)) {
                // 首先把客户端的socket关闭
                connectedClients.get(port).close();

            }
            connectedClients.remove(port);
            logger.info("客户端[{}]已断开连接", socket.getPort());
        }
    }

    /**
     * 转发消息
     * @param socket
     * @param msg
     * @throws IOException
     */
    public synchronized void forwardMsg(Socket socket, String msg) throws IOException {
        for(Integer port : connectedClients.keySet()) {
            if(!port.equals(socket.getPort())) {
                // 将消息转发给除自己以外的其他客户端
                Writer writer = connectedClients.get(port);
                writer.write(msg + "\n");
                writer.flush();
            }
        }

    }

    public boolean readyToQuit(String msg) {
       return QUIT.equals(msg);
    }

    /**
     * 关闭 服务端
     */
    public synchronized void close() {
        if(serverSocket != null) {
            try {
                serverSocket.close();
                logger.info("服务端已关闭");
            } catch (IOException e) {
                logger.info("关闭服务器异常", e);
            }
        }
    }

    public void start() {
        // 创建ServerSocket
        try {
            serverSocket = new ServerSocket(DEFAULT_SERVER_PORT);
            logger.info("启动服务器，监听端口：{}", DEFAULT_SERVER_PORT);
            // 监听端口获取客户端连接
            while (true) {
                Socket socket = serverSocket.accept();
                // 为每个客户端连接创建一个 Handler 线程处理
                // 1.方法一：来一个创建一个
                // new Thread(new ChatHandler(this, socket)).start();
                // 2.方法二：线程池
                executorService.submit(new ChatHandler(this, socket));

            }
        } catch (IOException e) {
            logger.info("服务端异常", e);
        } finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
