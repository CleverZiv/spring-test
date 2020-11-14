package com.leng.io.bio.socket;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Classname ServerDemo1
 * @Date 2020/11/14 19:57
 * @Autor lengxuezhang
 */
public class ServerDemo1 {
    private static final Logger logger = LoggerFactory.getLogger(ServerDemo1.class);
    // 服务器的默认端口
    private static final int DEFAULT_SERVER_PORT = 9898;
    private static final String QUIT = "q";

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            // 1.获取服务器的socket，需要绑定特定的端口号
            serverSocket = new ServerSocket(DEFAULT_SERVER_PORT);
            // 2. 监听端口
            while (true) {
                logger.info("服务器启动，监听端口中");
                // accept 方法是阻塞的方法，会一直监听，当有客户端调用 serverSocket时，可以获取到客户端的 socket 信息
                socket = serverSocket.accept();
                logger.info("客户端[{}]已连接", socket.getPort());
                // 3. 准备服务器端的 IO 流，我们先使用 BIO
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String msg = null;
                while ((msg = reader.readLine()) != null) {
                    //4. 读取客户端socket传递过来的信息
                    logger.info("客户端[{}]传递的消息是：{}", socket.getPort(), msg);
                    //5. 回复客户端的消息
                    writer.write("客户端["+socket.getPort()+"的消息我已经收到，内容是:"+ msg + "\n");
                    // 清空缓存，并同时将缓存中的数据写入到目标文件（在这里是socket）
                    writer.flush();
                    if(msg.equals(QUIT)) {
                        logger.info("检测到客户端已经退出");
                        break;
                    }
                }
            }

        } catch (IOException e) {
            logger.info("服务器出现异常");
        } finally {
            // 6. 关闭socket
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.info("关闭serverSocket时异常");
                }
            }
        }

    }
}
