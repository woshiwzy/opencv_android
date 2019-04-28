package com.myopencvdemo;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import org.opencv.android.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;

public class CameraPreviewActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "cv";

    private JavaCameraView mOpenCvCameraView;
    private SeekBar seekBar;
    private CheckBox checkBoxCaerma;
    private CheckBox checkBoxStudy;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public CameraPreviewActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    int width;
    int height;


    ArrayList<Rect> rects;

    Bitmap bitmap = null;

    ImageView imageViewRightTarget;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.preview_layout);

        imageViewRightTarget = findViewById(R.id.imageViewRightTarget);

        rects = new ArrayList<>();

        checkBoxStudy = findViewById(R.id.checkBoxStudy);

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        width = dm.widthPixels;
        height = dm.heightPixels;


        seekBar = findViewById(R.id.seekBar);
        checkBoxCaerma = findViewById(R.id.checkBoxCaerma);
        checkBoxCaerma.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                mOpenCvCameraView.setUseFrontCamera(isChecked);
                mOpenCvCameraView.setCameraIndex(isChecked ? 1 : 0);
                initCamera();
            }
        });

        mOpenCvCameraView = findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        initCamera();
    }

    public void initCamera() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        Log.d(TAG, "width:" + width + " height:" + height);

    }

    public void onCameraViewStopped() {

    }

    ArrayList<MatOfPoint> targetAreas = new ArrayList<>();

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        targetAreas.clear();
        rects.clear();

        Mat gray = inputFrame.rgba();

        Mat srcMat = gray.clone();

//        Mat rotateMat = Imgproc.getRotationMatrix2D(new Point(gray.rows() / 2, gray.cols() / 2), -90, 1);
//        Mat dst = new Mat(gray.rows(), gray.cols(), CV_8UC1);
//        Imgproc.warpAffine(gray, dst, rotateMat, dst.size());

        Imgproc.cvtColor(gray, gray, Imgproc.COLOR_RGBA2GRAY);
        Mat dst = new Mat(gray.rows(), gray.cols(), CV_8UC1);
        int bValue = seekBar.getProgress();

        Imgproc.threshold(gray, dst, bValue, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(dst, contours, new Mat(gray.rows(), gray.cols(), gray.type()), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);//CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE

        int minWidth = width / 2, minHeight = height / 3;

        for (int i = 0, isize = contours.size(); i < isize; i++) {
            MatOfPoint temp = contours.get(i);
            Rect rect = Imgproc.boundingRect(temp);
            int rh = (rect.width / rect.height);
            if ((rect.width > minWidth || rect.height > minHeight) && (rh > 4 && rh < 6)) {
                Imgproc.rectangle(srcMat, rect.tl(), rect.br(), new Scalar(255, 100, 100), 10);
                targetAreas.add(temp);
                rects.add(rect);
            }
        }


        if (rects.size() == 1) {
//            Log.i(App.tag, "whs:" + rects.get(0).width / rects.get(0).height+" threadname:"+Thread.currentThread().getName());

            Rect r = rects.get(0);
            r.height = r.height / 2;

            Mat t = new Mat(srcMat, r);

//            Mat srct=t.clone();
            //find target  area

            Imgproc.cvtColor(t, t, Imgproc.COLOR_RGBA2GRAY);
            List<MatOfPoint> contours2 = new ArrayList<>();
            Imgproc.threshold(t, t, 50, 255, Imgproc.THRESH_BINARY);


            Imgproc.findContours(t, contours2, new Mat(t.rows(), t.cols(), t.type()), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);//CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE

            for (int j = 0, jsize = contours2.size(); j < jsize; j++) {

                MatOfPoint temp2 = contours2.get(j);
                Rect rect2 = Imgproc.boundingRect(temp2);
                float rh2 = (rect2.width * 1.0f / rect2.height);
//                if ( (rh2>=0.4 && rh2 <=1.2) && (rect2.height> t.height()/6)) {
                if ((rect2.height > t.height() / 7) && rect2.width < t.width() / 12) {
                    Imgproc.rectangle(t, rect2.tl(), rect2.br(), new Scalar(44, 255, 100, 100), 1);
                    if (checkBoxStudy.isChecked()) {
                        Mat matArea = new Mat(t, rect2);

                        File dirFile = new File(MainActivity.dataPath);

                        if (!dirFile.exists()) {
                            dirFile.mkdir();
                            Log.i(App.tag, "mkdir:" + dirFile.getAbsolutePath());
                        }
                        String path = MainActivity.dataPath + File.separator + String.valueOf(System.currentTimeMillis()) + ".png";

                        Log.i(App.tag, "save path:" + path);

                        Bitmap bitmapMat = Bitmap.createBitmap(matArea.cols(), matArea.rows(), Bitmap.Config.RGB_565);

                        Utils.matToBitmap(matArea, bitmapMat);


                        try {

                            File tf=new File(path);
                            if(!tf.exists()){
                                tf.createNewFile();
                            }
                            bitmapMat.compress(Bitmap.CompressFormat.PNG, 75, new FileOutputStream(tf));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i(App.tag, "保存失败" + path+" "+e.getLocalizedMessage());
                        }

//                        Imgcodecs.imwrite(path,matArea);

                    }
                }
            }

//            if (null != bitmap) {
//                bitmap.recycle();
//            }

            bitmap = Bitmap.createBitmap(t.cols(), t.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(t, bitmap);
            t.release();
//            srct.release();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageViewRightTarget.setImageBitmap(bitmap);
                }
            });
        }

        Imgproc.drawContours(dst, targetAreas, -1, new Scalar(0, 255, 255), 3);


        dst.release();
        gray.release();


        return srcMat;
    }

}
