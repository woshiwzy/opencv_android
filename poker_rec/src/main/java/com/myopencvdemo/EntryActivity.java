package com.myopencvdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.myopencvdemo.datapool.DataPool;
import com.myopencvdemo.domain.MlData;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 入口Activity
 */
public class EntryActivity extends AppCompatActivity {


    public static String targetpath = "pork";//学习模式收集的图片存放的位置
    public static String dataPath = Environment.getExternalStorageDirectory() + File.separator + targetpath;//学习模式收集的图片存放的位置

    public static String mldata = "good_data";//采集好的分类图片存放的位置
    public static String mldataPath = Environment.getExternalStorageDirectory() + File.separator + mldata;


    TextView textViewStatus;

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


    private SweetAlertDialog pDialog;

    private void showProgressBar() {
        if (null != pDialog && pDialog.isShowing()) {
            pDialog.dismiss();
        }
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void hideProgressBar() {
        if (null != pDialog && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textViewStatus = findViewById(R.id.textViewStatus);
        findViewById(R.id.buttonPreview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(PokerRecActivity.class);
            }
        });

//        findViewById(R.id.buttonAna).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startNewActivity(SingImageAnaActivity.class);
//            }
//        });

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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewStatus.setText("稍后");
                    showProgressBar();
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewStatus.setText("拷贝样本数据到sdcard");
                    showProgressBar();
                }
            });

            copyAssets(EntryActivity.this, "good_data.zip", "/sdcard/good_data.zip");

            File file = new File(path);
            File[] fs = file.listFiles();
            DataPool.clear();

            for (File t : fs) {
                if (t.isFile()) {
                    continue;
                }

                MlData mlData = MlData.createMlDataFromDirectory(t);
                if (null == mlData) {
                    Log.i(App.tag, "ret:" + mlData.toString());
                    continue;
                }
                DataPool.addMlData(mlData.getLabel(), mlData);
            }

            DataPool.init();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressBar();

                    textViewStatus.setText("结束");

                    Intent intent = new Intent(EntryActivity.this, PokerRecActivity.class);
                    startActivity(intent);
                }
            });

        }
    }


    private void startNewActivity(Class claz) {
        Intent intent = new Intent(this, claz);
        startActivity(intent);
    }


    /**
     * 复制asset文件到指定目录
     *
     * @param oldPath asset下的路径
     * @param newPath SD卡下保存路径
     */
    public static void copyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(App.tag, "拷贝出错：" + e.getLocalizedMessage());
        }
    }
}
