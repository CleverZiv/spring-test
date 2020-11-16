package com.leng.io.chatroom.bio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Classname FileCopyDemo
 * <p>
 * 通过使用四种不同的复制文件的方法，来熟悉 BIO 和 NIO 的常用 API
 * @Date 2020/11/16 23:01
 * @Autor lengxuezhang
 */

interface FileCopyRunner {
    /**
     * 将文件从source复制到target
     *
     * @param source
     * @param target
     */
    void copyFile(File source, File target);
}

public class FileCopyDemo {

    private static final int ROUNDS = 5;

    private static void benchMark(FileCopyRunner test, File source, File target) {
        long elapsed = 0;
        for(int i = 0; i < ROUNDS; i++) {
            long startTime = System.currentTimeMillis();
            test.copyFile(source, target);
            elapsed += System.currentTimeMillis() - startTime;
        }
        System.out.println(test.toString()+":"+elapsed/ROUNDS);
    }

    private static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 不使用任何的流和缓冲区的拷贝
        FileCopyRunner noBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new FileInputStream(source);
                    fout = new FileOutputStream(target);
                    int result;
                    while ((result = fin.read()) != -1) {
                        fout.write(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }

            }

            @Override
            public String toString() {
                return "noBufferStreamCopy";
            }
        };

        // 使用缓冲区的流的拷贝
        FileCopyRunner bufferedStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new BufferedInputStream(new FileInputStream(source));
                    fout = new BufferedOutputStream(new FileOutputStream(target));
                    byte[] buffer = new byte[1024];
                    int res;
                    while ((res = fin.read(buffer)) != -1) {
                        fout.write(buffer, 0, res);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }
            }
            @Override
            public String toString() {
                return "bufferedStreamCopy";
            }
        };

        // 使用buffer的NIO拷贝
        FileCopyRunner nioBufferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;

                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int res;
                    // 通道调用读方法，将读到的数据写入到buffer,此时buffer是写模式
                    while ((res = fin.read(buffer)) != -1) {
                        // 写模式切换到读模式
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            // write 方法并不保证一次就将数据全部写入，所以需要不停的判断
                            fout.write(buffer);
                        }
                        // 读模式切换到写模式
                        buffer.clear();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }

            }
            @Override
            public String toString() {
                return "nioBufferCopy";
            }

        };

        // 不使用buffer的NIO拷贝
        FileCopyRunner nioTransferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;

                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();
                    // 记录已经拷贝的字节数
                    long transferred = 0L;
                    long size = fin.size();
                    while (transferred != size) {
                        // transferTo 函数同样不保证一次就能将文件拷贝完
                        transferred += fin.transferTo(0, size, fout);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(fin);
                    close(fout);
                }

            }
            @Override
            public String toString() {
                return "nioTransferCopy";
            }
        };
    }

    // 调用上面写的 benchMark 方法，准备一个文件即可进行测试

}
