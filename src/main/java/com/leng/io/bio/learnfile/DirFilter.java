package com.leng.io.bio.learnfile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * @Classname DirFilter
 * @Date 2020/11/13 19:35
 * @Autor lengxuezhang
 */
public class DirFilter implements FilenameFilter {
    private Pattern pattern;

    public DirFilter(String regex) {
        pattern = Pattern.compile(regex);
    }
    @Override
    public boolean accept(File dir, String name) {
        return pattern.matcher(name).matches();
    }
}
