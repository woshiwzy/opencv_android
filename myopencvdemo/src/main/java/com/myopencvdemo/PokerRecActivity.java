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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;

public class PokerRecActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "cv";

    private JavaCameraView mOpenCvCameraView;
    private SeekBar seekBar;
    private CheckBox checkBoxCaerma;
    private CheckBox checkBoxStudy;
    private View viewContent;

    private ArrayList<String> cardTypes = new ArrayList<>();


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

    public PokerRecActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    int screen_width;
    int screen_height;


    ArrayList<Rect> rects;

    Bitmap bitmap = null;

    ImageView imageViewRightTarget;

    private boolean needRec = true;
    private int huseCount = 13;//13 poker card 花色的总数，代表这么多张卡片

    private TextView textViewResult;


    private float cropContentPercent = 1;
    private float coverContentPercennt = 1;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_preview_layout);

        imageViewRightTarget = findViewById(R.id.imageViewRightTarget);
        viewContent = findViewById(R.id.viewContent);
        viewContent.post(new Runnable() {
            @Override
            public void run() {
                cropContentPercent = (((LinearLayout.LayoutParams) viewContent.getLayoutParams()).weight);
                coverContentPercennt = (((LinearLayout.LayoutParams) findViewById(R.id.converView).getLayoutParams()).weight);
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

        cardTypes.add("梅花");
        cardTypes.add("红桃");
        cardTypes.add("方块");
        cardTypes.add("黑桃");


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
                boolean test = false;

                if ((test) || (rect.height > (targetHeight*1.0f / 5)) && (rect.height < 0.8f * targetHeight) && (rect.width < (targetMat.width() / 8)) && (wh < 2)) {
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
//                          Log.e(App.tag, "test col:" + nmatf.cols());

                            float response = DataPool.getkNearest().predict(nmatf, result);
//
//                          Log.e(App.tag, "percent:" + DataPool.getPredicatedResult(response) + " predicated:" + nmatf.toString());

                            String resultStr = result.toString();
                            String resultLabel = DataPool.getPredicatedResult(response);
                            File file = new File(resultLabel);
                            String responnse = file.getParentFile().getName();
                            if (!"X".equalsIgnoreCase(responnse)) {
//                                Log.e(App.tag, "识别结果：" + responnse);
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

            Collections.sort(predicateds, new Comparator<RecResult>() {

                @Override
                public int compare(RecResult o1, RecResult o2) {

                    if (o1.getStartX() <= o2.getStartX()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            ArrayList<RecResult> huase = new ArrayList<>();//花色，
            ArrayList<RecResult> cardNo = new ArrayList<>();//对应花色的值

//          Log.e(App.tag, "------------------------------------------------------------------------------------------");
            for (int i = 0, isize = predicateds.size(); i < isize; i++) {
                RecResult card = predicateds.get(i);
                if (cardTypes.contains(card.getResultLabel())) {
                    huase.add(card);
                } else {
                    cardNo.add(card);
                }
            }
            if (huase.size() == huseCount) {//识别出指定数量的卡片数量


                ArrayList<Poker> pokers = new ArrayList<>();//存放识别结果

                StringBuffer sbfCardNo = new StringBuffer();

                //正确的处理10的识别结果，如果只出现了1或者0，识别结果都是不对的
                for (int i = 0, isize = cardNo.size(); i < isize; i++) {
                    if ("1".equalsIgnoreCase(cardNo.get(i).getResultLabel())) {
                        if ((i + 1) < isize) {
                            String nextZero = cardNo.get(i + 1).getResultLabel();
                            if ("0".equalsIgnoreCase(nextZero)) {
                                //1和0 不能分开，而且只有10这种情况
                                sbfCardNo.append(cardNo.get(i).getResultLabel() + cardNo.get(i + 1).getResultLabel() + ",");
                                i++;
                            } else {
                                Log.e(App.tag, "识别1但是紧接着不是0【识别失败】");
                                break;
                            }
                        } else {
                            Log.e(App.tag, "识别到最后一个数字是1本次【识别失败】");
                            break;
                        }
                    } else {
                        sbfCardNo.append(cardNo.get(i).getResultLabel() + ",");
                    }
                }


                String[] cards = sbfCardNo.toString().split(",");
                ArrayList<String> rightCardNo = new ArrayList<>();
                for (int i = 0, isize = cards.length; i < isize; i++) {
                    if (!"0".equals(cards[i]) && !"1".equals(cards[i])) {//如果识别结果单独出现了1，或者 0 是不对的
                        rightCardNo.add(cards[i]);
                    }
                }

                //经过处理后，如果和花色的数量是一样的，就算识别正确了
                if (rightCardNo.size() == huseCount) {//筛选出正确的识别结果，到这里为止，算是识别成功

                    Log.e(App.tag, "----------------------------Nice-------------------");

                    final StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0, isize = rightCardNo.size(); i < isize; i++) {
                        Poker poker = new Poker(huase.get(i).getResultLabel(), rightCardNo.get(i));
                        pokers.add(poker);
                        stringBuffer.append(poker.toString());
                    }

                    Log.e(App.tag, "识别结果:" + stringBuffer);
                    Log.e(App.tag, "----------------------------Nice End-------------------");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewResult.setText("识别结果:" + stringBuffer.toString() + "点击重新识别");
                        }
                    });

                    needRec = false;
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
            targetMat.release();

            return gray;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(App.tag, "error onpreview:" + e.getLocalizedMessage());

        }

        return null;
    }


    class Poker {

        String pokertype;
        String cardNo;

        public Poker(String pokertype, String cardNo) {
            this.pokertype = pokertype;
            this.cardNo = cardNo;
        }

        public String getPokertype() {
            return pokertype;
        }

        public void setPokertype(String pokertype) {
            this.pokertype = pokertype;
        }

        public String getCardNo() {
            return cardNo;
        }

        public void setCardNo(String cardNo) {
            this.cardNo = cardNo;
        }

        @Override
        public String toString() {
            return "(" + pokertype + ":" + cardNo + ")";
        }
    }

}
