package com.leng.io.bio.stream;


import org.apache.log4j.Logger;

import java.io.*;

/**
 * @Classname ByteStreamDemo1
 * @Date 2020/11/13 20:53
 * @Autor lengxuezhang
 */
public class ByteStreamDemo1 {
    private static final Logger logger = Logger.getLogger(ByteStreamDemo1.class);
    public static void main(String[] args) {
        // 1、创建源和目标
        File file1 = new File("G:"+File.separator+"生活"+File.separator+"狼人杀入门篇"+File.separator+"狼人杀角色整理.docx");
        File file2 = new File("G:"+File.separator+"生活"+File.separator+"狼人杀入门篇"+File.separator+"狼人杀角色整理2.docx");
        InputStream in;
        OutputStream out;
        try {
            //2、创建输入输出流对象
            in = new FileInputStream(file1);
            out = new FileOutputStream(file2);

            //3、读取和写入操作
            byte[] buffer = new byte[10];
            // 本次读取的长度
            int len = -1;
            while((len = in.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, len));
                // 将读取出来的内容写入
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            logger.info("文件读取异常");
        }
    }
}
