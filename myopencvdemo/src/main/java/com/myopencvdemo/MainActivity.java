package com.myopencvdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.myopencvdemo.datapool.DataPool;
import com.myopencvdemo.domain.MlData;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    public static String targetpath = "pork";

    public static String dataPath = Environment.getExternalStorageDirectory() + File.separator + targetpath;

    public static String mldata = "stuy_data";

    public static String mldataPath = Environment.getExternalStorageDirectory() + File.separator + mldata;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(App.tag, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.buttonPreview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(CameraPreviewActivity.class);
            }
        });

        findViewById(R.id.buttonAna).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(SingImageAnaActivity.class);
            }
        });


        findViewById(R.id.createMlData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MlThread(mldataPath).start();

            }
        });



        if (!OpenCVLoader.initDebug()) {
            Log.d(App.tag, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(App.tag, "OpenCV library found inside package. Using it!");
        }
    }


    class MlThread extends Thread {

        String path;

        public MlThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {

            File file = new File(path);
            File[] fs = file.listFiles();

            for (File t : fs) {

                if (t.getName().equalsIgnoreCase(".DS_Store")) {
                    continue;
                }

                String label=t.getName();

                Log.i(App.tag, "path:" + t.getName());
                File[] lfs = t.listFiles();

                MlData mlData=new MlData(label);

                for (File lf : lfs) {

                    if (lf.getName().equalsIgnoreCase(".DS_Store")) {
                        continue;
                    }

                    MlData.createMlDataFromFile(lf);

                    Log.i(App.tag, "abspath:" + lf.getAbsolutePath());
                }
            }

        }
    }


    private void startNewActivity(Class claz) {
        Intent intent = new Intent(this, claz);
        startActivity(intent);
    }
}
