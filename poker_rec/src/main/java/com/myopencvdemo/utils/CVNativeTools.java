package com.myopencvdemo.utils;

import java.util.TreeMap;

public class CVNativeTools {


    static {
        System.loadLibrary("cv_tools");
    }

    public static void test(){

    }

    /**
     * @param matAddr
     * @return
     */
    public static native TreeMap<String, Integer> caculatePixNumberFromNative(long matAddr);

}
