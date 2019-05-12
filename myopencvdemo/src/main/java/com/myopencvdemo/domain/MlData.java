package com.myopencvdemo.domain;

import android.util.Log;
import com.myopencvdemo.App;
import com.myopencvdemo.datapool.DataPool;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MlData {

    public static final int UNITWIDTH = 40, UNITHEIGHT = 60;

    public String label;
    public HashMap<File, MatOfFloat> datas;

    public ArrayList<MatOfFloat> listDatas;

    public MlData(String label) {
        this.label = label;
        this.datas = new HashMap<>();
        this.listDatas=new ArrayList<>();
    }

    public void putMlData(File file, MatOfFloat matOfFloat) {
        this.datas.put(file, matOfFloat);
        this.listDatas.add(matOfFloat);
    }

    public ArrayList<MatOfFloat> getAllMatdata(){
        return this.listDatas;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public HashMap<File, MatOfFloat> getDatas() {
        return datas;
    }

    public static MlData createMlDataFromDirectory(File dir) {

        MlData mlData = new MlData(dir.getName());

        if (null != dir && dir.exists() && dir.isDirectory()) {
            File[] stdfiles = dir.listFiles();
            for (int i = 0, isize = stdfiles.length; i < isize; i++) {
                File df = stdfiles[i];

                if (null != df && df.exists() && (df.getAbsolutePath().endsWith(".png") || df.getAbsolutePath().endsWith(".PNG"))) {
                    MatOfFloat matOfFloat = createMlDataFromFile(df);
                    if (null != matOfFloat) {
                        mlData.putMlData(df, matOfFloat);
                    }
                }
            }
        } else {
            Log.e(App.tag, "study data must be a Directory");
        }

        return mlData;
    }


    private static MatOfFloat createMlDataFromFile(File file) {

        try {

            Mat mat = Imgcodecs.imread(file.getAbsolutePath());
            Mat dstMat = new Mat(UNITWIDTH, UNITHEIGHT, mat.type());
            Imgproc.resize(mat, dstMat, new Size(UNITWIDTH, UNITHEIGHT));

            MatOfFloat descriptors = new MatOfFloat();
            DataPool.getHogDescriptor().compute(dstMat, descriptors);
//          List<Float> list = descriptors.toList();
//          Log.i(App.tag, "create vector:" + file.getAbsolutePath());

            return descriptors;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String toString() {
        return "label:"+label+" datasize:"+datas.size();
    }
}
