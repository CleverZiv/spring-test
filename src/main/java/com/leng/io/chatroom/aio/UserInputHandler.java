package com.leng.io.chatroom.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Classname UserInputHandler
 * @Date 2020/11/21 23:48
 * @Autor lengxuezhang
 */
public class UserInputHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UserInputHandler.class);
    private ChatClient client;

    public UserInputHandler(ChatClient client) {
        this.client = client;
    }
    @Override
    public void run() {
        BufferedReader cosoleReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String msg = cosoleReader.readLine();
                client.sendMsg(msg);
                if(client.readyToQuit(msg)){
                    logger.info("成功返回聊天室");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
