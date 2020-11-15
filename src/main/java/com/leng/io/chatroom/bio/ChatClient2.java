package com.leng.io.chatroom.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * @Classname ChatClient
 * @Date 2020/11/15 12:28
 * @Autor lengxuezhang
 */
public class ChatClient2 {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    // 需要知道目标服务器的ip地址和端口，通过这两者确定 serverSocket
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 9898;
    private static final String QUIT = "quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * 发送消息到客户端
     * @param msg
     */
    public void send(String msg) throws IOException {
        if(!socket.isOutputShutdown()) {
            writer.write(msg + "\n");
            writer.flush();
        }
    }

    /**
     * 从服务端接收消息
     * @return
     * @throws IOException
     */
    public String receive() throws IOException {
        String msg = null;
        if(!socket.isInputShutdown()) {
            msg = reader.readLine();
        }
        return msg;
    }

    /**
     * 验证用户是否要退出
     * @param msg
     * @return
     */
    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public void start() {
        // 创建socket
        try {
            socket = new Socket(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
            //创建 IO 流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //处理用户的输入
            new Thread(new UserInputHandler2(this)).start();
            //监听服务端的消息
            String msg = null;
            while((msg = receive()) != null) {
                // 将接收到的值直接显示出来
                logger.info(msg);
            }
        } catch (IOException e) {
            logger.info("客户端出现异常", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.info("关闭客户端异常");
            }
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
