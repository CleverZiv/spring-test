package com.leng.io.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Classname AioClient
 * @Date 2020/11/21 15:54
 * @Autor lengxuezhang
 */
public class AioClient {
    private static final Logger logger = LoggerFactory.getLogger(AioClient.class);

    private AsynchronousSocketChannel clientChannel;
    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int DEFAULT_PORT = 8321;


    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        try {
            clientChannel =  AsynchronousSocketChannel.open();
            // 在客户端中我们尝试使用 Future 而不是回调函数来处理异步调用的额结果
            Future<Void> future = clientChannel.connect(new InetSocketAddress(DEFAULT_SERVER, DEFAULT_PORT));
            future.get();
            // 等待用户的输入
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();

                ByteBuffer buffer = ByteBuffer.wrap(input.getBytes());
                buffer.clear();
                Future<Integer> writeRes = clientChannel.write(buffer);
                writeRes.get();
                // 从服务器端读取响应
                buffer.clear();
                Future<Integer> readRes = clientChannel.read(buffer);
                readRes.get();
                String echo = new String(buffer.array());
                logger.info(echo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            close(clientChannel);
        }
    }

    public static void main(String[] args) {
        AioClient client = new AioClient();
        client.start();
    }

}
