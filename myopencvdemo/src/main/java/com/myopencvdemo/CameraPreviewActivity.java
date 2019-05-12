package com.myopencvdemo;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.myopencvdemo.datapool.DataPool;
import com.myopencvdemo.domain.MlData;
import org.opencv.android.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
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

    private boolean needRec = true;
    private int cardNo = 13;//13 poker card

    TextView textViewResult;

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

        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needRec = true;
                textViewResult.setText("等待重新识别:");
            }
        });

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

        try {


            targetAreas.clear();
            rects.clear();

            Mat gray = inputFrame.rgba();//可能出现这个错误:(-215:Assertion failed) u != 0 in function 'void cv::Mat::create(int, const int*, int)'

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


            //找到目标区域之后进处理
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


                ArrayList<String> predicateds = new ArrayList<>();

                for (int j = 0, jsize = contours2.size(); j < jsize; j++) {

                    MatOfPoint temp2 = contours2.get(j);
                    Rect rect2 = Imgproc.boundingRect(temp2);
                    float rh2 = (rect2.width * 1.0f / rect2.height);
//                if ( (rh2>=0.4 && rh2 <=1.2) && (rect2.height> t.height()/6)) {
                    if ((rect2.height > t.height() / 7) && rect2.width < t.width() / 12) {

                        Imgproc.rectangle(t, rect2.tl(), rect2.br(), new Scalar(44, 255, 100, 100), 1);

                        if (checkBoxStudy.isChecked()) {//学习模式

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
                                File tf = new File(path);
                                if (!tf.exists()) {
                                    tf.createNewFile();
                                }
                                bitmapMat.compress(Bitmap.CompressFormat.PNG, 75, new FileOutputStream(tf));
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i(App.tag, "保存失败" + path + " " + e.getLocalizedMessage());
                            }
                        } else {

                            if (needRec) {
                                //识别模式
                                Mat matArea = new Mat(t, rect2);
                                Mat dstMat = new Mat(MlData.UNITWIDTH, MlData.UNITHEIGHT, matArea.type());
                                Imgproc.resize(matArea, dstMat, new Size(MlData.UNITWIDTH, MlData.UNITHEIGHT));//归一化

                                MatOfFloat matf = new MatOfFloat();//计算特征
                                DataPool.getHogDescriptor().compute(dstMat, matf);

//                        Log.e(App.tag, "matf row:" + matf.rows());
                                Mat nmatf = matf.reshape(0, 1);

                                Mat result = new Mat();
//                        Log.e(App.tag, "test col:" + nmatf.cols());

                                float response = DataPool.getkNearest().predict(nmatf, result);
//
//                          Log.e(App.tag, "percent:" + DataPool.getPredicatedResult(response) + " predicated:" + nmatf.toString());

                                predicateds.add(DataPool.getPredicatedResult(response));

                                matArea.release();
                                dstMat.release();
                                matf.release();
                                nmatf.release();
                                result.release();

                            }

                        }
                    }
                }

//                Log.e(App.tag, "识别出：" + predicateds.size() + " 个数据");

                ArrayList<String> goodResult = new ArrayList<>();

                for (String pred : predicateds) {
//                  /storage/emulated/0/good_data/9/1556351332955.png
                    File file = new File(pred);
                    String result = file.getParentFile().getName();
                    if (!result.equalsIgnoreCase("X")) {
                        goodResult.add(result);
//                        Log.e(App.tag, "识别结果:" + result);
                    }
                }
                Log.e(App.tag, "--->>共识别:" + goodResult.size() + "有用数据");

                if ((goodResult.contains("1") && goodResult.size() == cardNo * 2 + 1) || (goodResult.size() == cardNo * 2)) {
                    needRec = false;
                    final StringBuffer sbf = new StringBuffer();
                    for (String re : goodResult) {
                        Log.e(App.tag, "-->:" + re);
                        sbf.append(re + ",");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewResult.setText("识别结果:" + sbf.toString().replace("BH", "黑桃️") + "点击重新识别");
                        }
                    });
                }

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
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(App.tag, "error onpreview:" + e.getLocalizedMessage());

        }

        return null;
    }

}
