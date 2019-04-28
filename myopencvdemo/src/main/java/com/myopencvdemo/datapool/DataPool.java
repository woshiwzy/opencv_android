package com.myopencvdemo.datapool;

import com.myopencvdemo.domain.MlData;
import org.opencv.core.Size;
import org.opencv.objdetect.HOGDescriptor;

import java.util.HashMap;

/**
 * Created by wangzy on 2019/4/27
 * description:
 */
public class DataPool {


    static HashMap<String, MlData> mlDataHashMap = new HashMap<>();
    static HOGDescriptor hogDescriptor;

    static {

        //HOGDescriptor::HOGDescriptor(_winSize, _blockSize, _blockStride, _cellSize, _nbins)

        Size windowSize=new Size(MlData.UNITWIDTH,MlData.UNITHEIGHT);
        Size blockSize=new Size(MlData.UNITWIDTH/2,MlData.UNITHEIGHT);

        Size _blockStride=new Size(blockSize.width/2,blockSize.height/2);

        Size _cellSize=_blockStride;
        int _nbins=4;

        hogDescriptor=new HOGDescriptor(windowSize,blockSize,_blockStride,_cellSize,_nbins);


    }

    public static void addMlData(String label, MlData mlData) {
        mlDataHashMap.put(label, mlData);
    }

    public static void clear() {
        mlDataHashMap.clear();
    }

    public static MlData getDataByLabel(String label) {
        return mlDataHashMap.get(label);
    }

    public static HOGDescriptor getHogDescriptor(){
        return  hogDescriptor;
    }

}
