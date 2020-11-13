package com.leng.io.bio.stream;

import java.io.*;

/**
 * @Classname CharStreamDemo1
 * @Date 2020/11/13 22:29
 * @Autor lengxuezhang
 */
public class CharStreamDemo1 {
    public static void main(String[] args) throws IOException {
        // 1、创建源和目标
        File file1 = new File("G:"+File.separator+"生活"+File.separator+"狼人杀入门篇"+File.separator+"狼人杀角色整理.docx");
        File file2 = new File("G:"+File.separator+"生活"+File.separator+"狼人杀入门篇"+File.separator+"狼人杀角色整理3.docx");

        Reader reader;
        Writer writer;

        reader = new FileReader(file1);
        writer = new FileWriter(file2);

        // 与上个例子不同的是，这次用字符数组接收
        char[] buffer = new char[10];
        int len = -1;
        while ((len = reader.read(buffer)) != -1) {
            System.out.println(new String(buffer, 0, len));
            writer.write(buffer, 0, len);
        }
        reader.close();
        writer.close();

    }
}
