package com.myopencvdemo.domain;

/**
 * Created by wangzy on 2019/5/14
 * description:
 */
public class RecResult {

    private String resultLabel;
    private int startX;
    private String filePath;

    public RecResult(String resultLabel, int startX, String filePath) {
        this.resultLabel = resultLabel;
        this.startX = startX;
        this.filePath = filePath;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Override
    public String toString() {
        return "RecResult{" +
                "resultLabel='" + resultLabel + '\'' +
                ", startX=" + startX +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
