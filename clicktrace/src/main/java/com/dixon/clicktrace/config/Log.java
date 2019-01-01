package com.dixon.clicktrace.config;

/**
 * Created by dixon.xu on 2018/12/26.
 */

public class Log {

    public static void o(String key, String value) {
        System.out.println(Params.LOGO + key + " : " + value);
    }

    public static void e(String error) {
        System.out.println(Params.LOGE + " Error : " + error);
    }
}
