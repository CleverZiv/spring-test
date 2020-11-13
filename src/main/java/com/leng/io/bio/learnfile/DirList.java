package com.leng.io.bio.learnfile;

import java.io.File;
import java.util.Arrays;

/**
 * @Classname DirList
 * @Date 2020/11/13 19:38
 * @Autor lengxuezhang
 */
public class DirList {
    public static void main(String[] args) {
        File path = new File(".");
        String[] list;
        if(args.length == 0) {
            list = path.list();
        }else {
            list = path.list(new DirFilter(args[0]));
        }
        // String.CASE_INSENSITIVE_ORDER 将字符串通过首字母的asill码进行排序
        Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
        for (String dirItem : list) {
            System.out.println(dirItem);
        }
    }
}
