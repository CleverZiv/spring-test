package com.leng.io.bio.learnfile;

import java.io.File;

/**
 * @Classname FileDemo1
 * @Date 2020/11/13 20:29
 * @Autor lengxuezhang
 */
public class FileDemo1 {
    public static void main(String[] args) {
        File file = new File("G:"+File.separator+"生活"+File.separator+"狼人杀入门篇"+File.separator+"狼人杀角色整理.docx");
        System.out.println(file.canRead());
    }

}
