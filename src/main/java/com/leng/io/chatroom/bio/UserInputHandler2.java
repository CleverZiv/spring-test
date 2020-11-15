package com.leng.io.chatroom.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Classname UserInputHandler
 * @Date 2020/11/15 12:28
 * @Autor lengxuezhang
 */
public class UserInputHandler2 implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(UserInputHandler2.class);
    // 客户端连接信息
    private ChatClient2 chatClient;

    public UserInputHandler2(ChatClient2 chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run() {
        try {
            BufferedReader cosoleReader = new BufferedReader(new InputStreamReader(System.in));
            String msg = null;
            while ((msg = cosoleReader.readLine()) != null) {
                chatClient.send(msg);
                if(chatClient.readyToQuit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.info("UserInputHandler 异常");
        }
    }
}
