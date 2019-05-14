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
import com.myopencvdemo.domain.RecResult;
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
    private View viewContent;

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


    int screen_width;
    int screen_height;


    ArrayList<Rect> rects;

    Bitmap bitmap = null;

    ImageView imageViewRightTarget;

    private boolean needRec = true;
    private int cardNo = 13;//13 poker card

    private TextView textViewResult;


    private int cropContentPercent = 1;
    private int coverContentPercennt = 1;


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
        viewContent = findViewById(R.id.viewContent);
        viewContent.post(new Runnable() {
            @Override
            public void run() {
                cropContentPercent = (int) (((LinearLayout.LayoutParams) viewContent.getLayoutParams()).weight);
                coverContentPercennt = (int) (((LinearLayout.LayoutParams) findViewById(R.id.converView).getLayoutParams()).weight);
                Log.e(App.tag, "weight is :" + cropContentPercent + " cover:" + coverContentPercennt * 2);

            }
        });


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
        screen_width = dm.widthPixels;
        screen_height = dm.heightPixels;


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

//        Log.d(TAG, "---->>>>>>>screen_width:" + width + " screen_height:" + height);

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

            int twidth = srcMat.width();
            int theight = srcMat.height();

//          Log.e(App.tag, "target wh:" + twidth + "," + theight);

            Rect targetCropRect = new Rect();

            targetCropRect.width = twidth;
            int targetHeight = (int) (theight * (cropContentPercent * 1.0 / (cropContentPercent + coverContentPercennt * 2)));
            targetCropRect.height = targetHeight;

//            Log.e(App.tag, "target screen_width*screen_height:" + targetCropRect.width + "," + targetCropRect.height);

            targetCropRect.x = 0;
            targetCropRect.y = theight / 2 - targetCropRect.height / 2;

            Mat targetMat = new Mat(srcMat, targetCropRect);

            Mat copyMat = targetMat.clone();


            Imgproc.cvtColor(targetMat, targetMat, Imgproc.COLOR_RGBA2GRAY);
            Mat dst = new Mat(gray.rows(), gray.cols(), CV_8UC1);
            int bValue = seekBar.getProgress();
            Imgproc.threshold(targetMat, dst, bValue, 255, Imgproc.THRESH_BINARY);

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(dst, contours, new Mat(dst.rows(), dst.cols(), dst.type()), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);//CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE


            ArrayList<RecResult> predicateds = new ArrayList<>();

            for (int i = 0, isize = contours.size(); i < isize; i++) {
                MatOfPoint temp = contours.get(i);
                Rect rect = Imgproc.boundingRect(temp);

                float wh = (rect.width * 1.0f / rect.height);

                if ((rect.height > (targetHeight / 4)) && (rect.height < 0.8f * targetHeight) && (rect.width < (targetMat.width() / 8)) && (wh < 2)) {
//                   Log.e(App.tag, "------>>>rhfactor:" + wh + " startX:" + rect.x);
                    Imgproc.rectangle(copyMat, rect.tl(), rect.br(), new Scalar(0, 0, 255, 255), 2);
                    targetAreas.add(temp);
                    rects.add(rect);

                    if (checkBoxStudy.isChecked()) {//学习模式

                        Mat matArea = new Mat(dst, rect);
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
                            Mat matArea = new Mat(dst, rect);
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

                            String resultLabel = DataPool.getPredicatedResult(response);
                            File file = new File(resultLabel);
                            String responnse = file.getParentFile().getName();
                            if (!"X".equalsIgnoreCase(responnse)) {
                                Log.e(App.tag, "识别结果：" + responnse);
                                predicateds.add(new RecResult(responnse, rect.x, resultLabel));
                            }

//                            matArea.release();
//                            dstMat.release();
//                            matf.release();
//                            nmatf.release();
//                            result.release();
                        }
                    }
                }
            }

            bitmap = Bitmap.createBitmap(targetCropRect.width, targetCropRect.height, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(copyMat, bitmap);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageViewRightTarget.setImageBitmap(bitmap);
                }
            });


            return gray;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(App.tag, "error onpreview:" + e.getLocalizedMessage());

        }

        return null;
    }

}
