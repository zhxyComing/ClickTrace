package com.dixon.clicktrace.utils;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by dixon.xu on 2018/12/26.
 */

public class ClickFileUtils {

    private static final String CTGENPATH = "/clicktrace/";
    private static final String CTAUTOPATH = "/clicktrace/auto/";

    //要修改的文件放到该目录
    public static void copyCTGen(File file, String buildPath) {
        try {
            FileUtils.copyFile(file, new File(buildPath + CTGENPATH + file.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //自生成的类放到该目录
    public static void copyCTAuto(File file, String buildPath) {
        try {
            FileUtils.copyFile(file, new File(buildPath + CTAUTOPATH + file.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //文件转byte[]
    public static byte[] getBytes(String filePath) {
        File file = new File(filePath);
        ByteArrayOutputStream out = null;
        try {
            FileInputStream in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {
                out.write(b, 0, i);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null != out ? out.toByteArray() : new byte[0];
    }
}
