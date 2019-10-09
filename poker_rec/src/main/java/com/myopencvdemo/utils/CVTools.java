package com.myopencvdemo.utils;

import org.opencv.core.Mat;

import java.util.TreeMap;

/**
 * Created by wangzy on 2018/11/21
 * description:
 */
public class CVTools {


    /**
     * rgb转换成16进制颜色
     *
     * @param red
     * @param green
     * @param blue
     * @return
     */
    public static String rgb2HexColor(int red, int green, int blue) {
        String hr = Integer.toHexString(red);
        String hg = Integer.toHexString(green);
        String hb = Integer.toHexString(blue);

        if (hr.length() == 1) {
            hr = "0" + hr;
        }
        if (hg.length() == 1) {
            hg = "0" + hg;
        }
        if (hb.length() == 1) {
            hb = "0" + hb;
        }
        String ret = "#" + hr + hg + hb;
        return ret;
    }


    /**
     * 获取指定点RGB
     *
     * @param mat
     * @param row
     * @param col
     * @return
     */
    public static String getPixHexColorAt(Mat mat, int row, int col) {
        double data[] = mat.get(row, col);
        if (null == data) {
            return null;
        }
        return rgb2HexColor((int) data[0], (int) data[1], (int) data[2]);
    }

    /**
     * 统计像素的个数
     *
     * @param mat
     * @return
     */
    public static TreeMap<String, Integer> caculatePixNumber(Mat mat) {

        int colTotal = mat.cols();
        int rowTotal = mat.rows();
        TreeMap<String, Integer> pixNumbersMap = new TreeMap<>();
        for (int col = 0; col < colTotal; col++) {
            for (int row = 0; row < rowTotal; row++) {
                String pxi = getPixHexColorAt(mat, row, col);
                if (pixNumbersMap.containsKey(pxi)) {
                    pixNumbersMap.put(pxi, pixNumbersMap.get(pxi) + 1);
                } else {
                    pixNumbersMap.put(pxi, 1);
//                    pixNumbersMap.pu
//                    pixNumbersMap.containsKey();
                }
            }
        }
        return pixNumbersMap;
    }




}
