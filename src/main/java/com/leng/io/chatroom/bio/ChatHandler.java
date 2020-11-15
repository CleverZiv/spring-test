package com.leng.io.chatroom.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * @Classname ChatHandler
 * @Date 2020/11/15 12:28
 * @Autor lengxuezhang
 */
public class ChatHandler implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ChatHandler.class);
    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 注册到服务端的客户端列表
            server.addClient(socket);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String msg = null;
            while((msg = reader.readLine()) != null) {
                // 转发到别的客户端
                String fwdMsg = "来自客户端["+socket.getPort()+"]的消息" + msg;
                server.forwardMsg(socket, fwdMsg);
                if(server.readyToQuit(msg)) {
                    break;
                }
            }

        } catch (IOException e) {
            logger.info("服务端Handler异常");
        } finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                logger.info("服务端删除客户端节点异常");
            }
        }
    }
}
