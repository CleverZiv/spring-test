package com.leng.io.bio.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * @Classname ClientDemo1
 * @Date 2020/11/14 19:57
 * @Autor lengxuezhang
 */
public class ClientDemo2 {
    private static final Logger logger = LoggerFactory.getLogger(ClientDemo1.class);
    // 需要知道目标服务器的ip地址和端口，通过这两者确定 serverSocket
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 9898;
    private static final String QUIT = "q";

    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            // 1.创建 socket
            socket = new Socket(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
            // 2.创建客户端的 IO 流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 3.写入消息，采用控制台获取用户输入的方式
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String res = null;
            while (true) {
                String msg = consoleReader.readLine();
                if(QUIT.equals(msg)) {
                    logger.info("客户端已退出");
                }
                // 4.将消息放入socket的IO流
                writer.write(msg + "\n");
                writer.flush();
                // 5.读取服务器返回的消息，并打印
                res = reader.readLine();
                logger.info("服务端返回的消息为：{}", res);
            }
        } catch (IOException e) {
            logger.info("客户端出现异常");
        } finally {
            if(writer != null) {
                logger.info("关闭客户端socket");
                try {
                    // 关闭writer时即可关闭socket
                    writer.close();
                } catch (IOException e) {
                    logger.info("关闭socket异常");
                }
            }

        }

    }
}
