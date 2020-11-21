package com.leng.io.chatroom.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Classname ChatClient
 * @Date 2020/11/21 23:37
 * @Autor lengxuezhang
 */
public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    private AsynchronousSocketChannel clientChannel;
    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private Charset charset = Charset.forName("UTF-8");
    private static final String Quit = "quit";

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.info("{}关闭异常", closeable.toString());
        }
    }

    public boolean readyToQuit(String msg) {
        return Quit.equals(msg);
    }

    public void sendMsg(String msg) {
        if(msg.isEmpty()) {
            return;
        }else {
            ByteBuffer buffer = charset.encode(msg);
            Future<Integer> write = clientChannel.write(buffer);
            try {
                write.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
    private void start() {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> connect = clientChannel.connect(new InetSocketAddress(DEFAULT_SERVER, DEFAULT_PORT));
            connect.get();
            logger.info("已经与服务器建立连接");
            // 需要有一个用户线程监控用户输入
            new Thread(new UserInputHandler(this)).start();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (clientChannel.isOpen()) {
                Future<Integer> read = clientChannel.read(buffer);
                int res = read.get();
                if(res <= 0) {
                    close(clientChannel);
                } else {
                    buffer.flip();
                    String msg = charset.decode(buffer).toString();
                    logger.info(msg);
                    buffer.clear();
                }
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
        ChatClient client = new ChatClient();
        client.start();
    }
}
