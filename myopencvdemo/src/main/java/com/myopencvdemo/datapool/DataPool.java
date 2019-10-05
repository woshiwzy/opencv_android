package com.myopencvdemo.datapool;

import android.util.Log;
import com.myopencvdemo.App;
import com.myopencvdemo.domain.MlData;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.ml.KNearest;
import org.opencv.ml.RTrees;
import org.opencv.ml.SVM;
import org.opencv.ml.TrainData;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangzy on 2019/4/27
 * description:
 */
public class DataPool {


    static HashMap<String, MlData> mlDataHashMap = new HashMap<>();
    static HOGDescriptor hogDescriptor;
    static KNearest kNearest = null;
    static HashMap<Integer, String> n2f = new HashMap();//数字label，和文件名映射

//    private static  SVM svm = null;


    static {
        //HOGDescriptor::HOGDescriptor(_winSize, _blockSize, _blockStride, _cellSize, _nbins)
        //配置特征采集器
        Size windowSize = new Size(MlData.UNITWIDTH, MlData.UNITHEIGHT);
        Size blockSize = new Size(MlData.UNITWIDTH / 2, MlData.UNITHEIGHT / 2);
        Size _blockStride = new Size(blockSize.width / 2, blockSize.height / 2);
        Size _cellSize = _blockStride;
        int _nbins = 4;
        hogDescriptor = new HOGDescriptor(windowSize, blockSize, _blockStride, _cellSize, _nbins);

        //配置及其学习方法
        kNearest = KNearest.create();
        kNearest.setDefaultK(3);
        kNearest.setIsClassifier(true);


//        svm=SVM.create();
    }


    public static void addMlData(String label, MlData mlData) {
        mlDataHashMap.put(label, mlData);
    }


    public static int str2Int(String labelFile) {
        int size = n2f.size();
        n2f.put(size, labelFile);
        return size;

    }

    /**
     * 获取计算结果
     * @param response
     * @return
     */
    public static String getPredicatedResult(float response){

        return n2f.get((int)response);
    }


    /**
     * when finish addMldata call this method init
     */
    public static void init() {
        Log.i(App.tag, "------init ml------");

        ArrayList<Integer> labelList = new ArrayList<>();

        int totalRow = 0;
        int col = 0;

        for (Map.Entry<String, MlData> entry : mlDataHashMap.entrySet()) {
            HashMap<File, MatOfFloat> da = entry.getValue().getDatas();
            totalRow += da.size();
            if (col == 0) {
                col = entry.getValue().getAllMatdata().get(0).toList().size();
            }
        }

        Mat samplesMat = new Mat(0, col, CvType.CV_32F);

        for (Map.Entry<String, MlData> entry : mlDataHashMap.entrySet()) {

            String labelText = entry.getKey();
            HashMap<File, MatOfFloat> da = entry.getValue().getDatas();


            for (Map.Entry<File, MatOfFloat> entry1 : da.entrySet()) {

                File dataFile = entry1.getKey();

//                List<Float> list = entry1.getValue().toList();

                int labelNumber = str2Int(dataFile.getAbsolutePath());

                labelList.add(labelNumber);

                MatOfFloat v = entry1.getValue();
                Mat nv = v.reshape(v.channels(), 1);
//                Log.i(App.tag, "matoffloat row:" + nv.rows() + " col:" + nv.cols());

                samplesMat.push_back(nv);

              Log.i(App.tag,"-->data size:"+totalRow+" samplw rows"+samplesMat.rows());
            }
        }
//
        Log.i(App.tag, "data size:" + totalRow + "  sample rows:" + samplesMat.rows());

        Mat responseMat = new Mat(samplesMat.rows(), 1, CvType.CV_32F);

        for (int i = 0, isize = samplesMat.rows(); i < isize; i++) {
            responseMat.put(i, 0, labelList.get(i));
        }

//        samplesMat.convertTo(samplesMat,CvType.CV_32F);

        TrainData trainData = TrainData.create(samplesMat, 0, responseMat);

        kNearest.train(trainData);

        Log.i(App.tag, "------finish init------");
    }

    public static KNearest getkNearest() {
        return kNearest;
    }

    public static void setkNearest(KNearest kNearest) {
        DataPool.kNearest = kNearest;
    }

    public static void clear() {
        mlDataHashMap.clear();
        n2f.clear();
    }

    public static MlData getDataByLabel(String label) {
        return mlDataHashMap.get(label);
    }

    public static HOGDescriptor getHogDescriptor() {
        return hogDescriptor;
    }

}
