package com.myopencvdemo.domain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.myopencvdemo.App;
import com.myopencvdemo.datapool.DataPool;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class MlData {

    public static final int UNITWIDTH = 40, UNITHEIGHT = 60;


    public String label;
    public ArrayList<File> files;


    public MlData(String label) {
        this.label = label;
        files = new ArrayList<>();
    }

    public void addFiles(File file) {
        this.files.add(file);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }


    public static void createMlDataFromFile(File file) {

        try {

//            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));

//            Mat::Mat(rows, cols, type)
//            Mat mat = new Mat(bitmap.getWidth(),bitmap.getHeight(), CV_8UC3);

//            Utils.bitmapToMat(bitmap,mat);

            Mat mat = Imgcodecs.imread(file.getAbsolutePath());
            Mat dstMat = new Mat(UNITWIDTH, UNITHEIGHT, mat.type());
            Imgproc.resize(mat, dstMat, new Size(UNITWIDTH, UNITHEIGHT));
            MatOfFloat descriptors = new MatOfFloat();
            DataPool.getHogDescriptor().compute(dstMat, descriptors);

            List<Float> list = descriptors.toList();

            Log.i(App.tag,"create vector:"+file.getAbsolutePath());


        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
