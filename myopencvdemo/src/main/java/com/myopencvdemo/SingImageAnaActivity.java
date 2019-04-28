package com.myopencvdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.myopencvdemo.utils.CVNativeTools;
import com.myopencvdemo.utils.CVTools;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class SingImageAnaActivity extends AppCompatActivity {
    String tag = "cv";
    SeekBar seekBar;
    SeekBar seekBarDiff;
    ImageView imageView;

    Button buttonSetSrouceimg;
    Button buttonSumPix;

    Bitmap bitmap;
    Mat mat;
    Mat dst;

    TextView textViewDiffValue;

    CheckBox checkboxCVT;
    CheckBox checkBoxFlooadFill;
    View viewBgGround;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_image_ana);
        checkboxCVT = findViewById(R.id.checkboxCVT);
        checkBoxFlooadFill = findViewById(R.id.checkBoxFlooadFill);
        viewBgGround = findViewById(R.id.viewBgGround);
        seekBarDiff = findViewById(R.id.seekBarDiff);


        buttonSumPix = findViewById(R.id.buttonSumPix);
        buttonSumPix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                System.loadLibrary("cv_tools");
                TreeMap<String, Integer> tremap = CVNativeTools.caculatePixNumberFromNative(mat.nativeObj);
                Log.d(tag, "tremap:" + tremap.size());
            }
        });


        seekBarDiff.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewDiffValue.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        textViewDiffValue = findViewById(R.id.textViewDiffValue);


        buttonSetSrouceimg = findViewById(R.id.buttonSetSrouceimg);
        buttonSetSrouceimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmap);
            }
        });

        checkboxCVT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Mat dst = new Mat();
                    Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);

                    Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(dst, bitmap);
                    imageView.setImageBitmap(bitmap);
                    dst.release();
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });

//        findViewById(R.id.imageViewTarget).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                imageView.setImageBitmap(bitmap);
//            }
//        });

        seekBar = findViewById(R.id.seekBar);
        imageView = findViewById(R.id.imageViewTarget);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (checkBoxFlooadFill.isChecked()) {

                    float scale = imageView.getHeight() * 1.0f / bitmap.getHeight();
                    float imageWidthInImageView = scale * bitmap.getWidth();

                    int scaledWidth = (int) imageWidthInImageView;
                    int scaledHeight = imageView.getHeight();

                    int touchInSourceX = (int) ((event.getX() - (imageView.getWidth() / 2 - scaledWidth / 2)) / scale);
                    int touchInSourceY = (int) (event.getY() / scale);

                    Log.i(tag, "touch in source:" + touchInSourceX + " " + touchInSourceY);
                    int matCol = mat.cols(), matrow = mat.rows();
                    Log.i(tag, "matCol:" + matCol + " " + matrow);

                    double[] touchData = mat.get(touchInSourceY, touchInSourceX);

                    if (null == touchData || null == mat) {
                        return true;
                    }

                    long start = System.currentTimeMillis();
                    TreeMap<String, Integer> map = CVTools.caculatePixNumber(mat);
                    long end = System.currentTimeMillis();
                    Log.i(tag, "统计像素耗时:" + (end - start));


                    Log.i(tag, "touch data1:(" + CVTools.getPixHexColorAt(mat, touchInSourceX, touchInSourceY) + ")");
                    Log.d(tag, "touch data2:(" + touchData[0] + "," + touchData[1] + "," + touchData[2] + "," + touchData[3] + ")");
                    if (null != dst) {
                        dst.release();
                    }

                    dst = new Mat(mat.rows(), mat.cols(), CV_8UC3);

                    Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGRA2BGR);

                    Mat maskMat = Mat.zeros(mat.rows() + 2, mat.cols() + 2, CV_8UC1);

                    int detal = seekBarDiff.getProgress();
                    Rect r = new Rect();
                    Imgproc.floodFill(dst, maskMat, new Point(touchInSourceX, touchInSourceY), new Scalar(0, 0, 0), r, new Scalar(detal, detal, detal), new Scalar(detal, detal, detal));
                    Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(dst, bitmap);
                    imageView.setImageBitmap(bitmap);
                    dst.release();
                    try {
                        viewBgGround.setBackgroundColor(Color.parseColor(CVTools.rgb2HexColor((int) touchData[0], (int) touchData[1], (int) touchData[2])));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    imageView.setImageBitmap(bitmap);
                }
                return true;
            }
        });
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2);

        Log.d(tag, "原图宽高:" + bitmap.getWidth() + "," + bitmap.getHeight());


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                ArrayList<MatOfPoint> targetAreas = new ArrayList<>();
                int bValue = seekBar.getProgress();

                Mat dst = new Mat();

                ArrayList<Mat> mats = new ArrayList<>();
                mats.add(mat);

//              Imgproc.calcHist(mats, new MatOfInt(1), new Mat(), new Mat(), new MatOfInt(10), new MatOfFloat(10));
                Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(dst, dst, bValue, 255, Imgproc.THRESH_BINARY);

//                if (checkboxCVT.isChecked()) {
//                    Imgproc.threshold(dst, dst, bValue, 255, Imgproc.THRESH_BINARY);
//                } else {
//                    Imgproc.threshold(mat, dst, bValue, 255, Imgproc.THRESH_BINARY);
//                }

                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(dst, contours, new Mat(dst.rows(), dst.cols(), dst.type()), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);//CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE

                int minWidth = 30, minHeight = 30;

                for (int i = 0, isize = contours.size(); i < isize; i++) {
                    MatOfPoint temp = contours.get(i);
                    Rect rect = Imgproc.boundingRect(temp);

                    if (rect.width > minWidth || rect.height > minHeight) {
                        Imgproc.rectangle(dst, rect.tl(), rect.br(), new Scalar(255, 100, 0), 5);
                        targetAreas.add(temp);
                    }
                }

                Imgproc.drawContours(dst, targetAreas, -1, new Scalar(255, 255, 255), 5);

                Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dst, bitmap);
                imageView.setImageBitmap(bitmap);

                dst.release();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mat = new Mat();
                    Utils.bitmapToMat(bitmap, mat);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public void initCamera() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(tag, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(tag, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }


    public void onResume() {
        super.onResume();
        initCamera();
    }
}
