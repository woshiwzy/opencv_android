package org.opencv.samples.tutorial1;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import org.opencv.android.*;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FrontCameraRotate90PreviewActivity extends Activity implements CvCameraViewListener2 {

    private static final String TAG = App.tag;
    private JavaCameraView mOpenCvCameraView;
    private ImageView imageViewPreview;
    private ImageView imageViewPreview2;

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

    public FrontCameraRotate90PreviewActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_preview1);

        mOpenCvCameraView = findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mOpenCvCameraView.setUseFrontCamera(true);
        mOpenCvCameraView.setDrawSource(false);


        imageViewPreview = findViewById(R.id.imageViewPreview);
        imageViewPreview2 = findViewById(R.id.imageViewPreview2);


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

    }

    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Log.e(App.tag, "camera preview:----");

        Mat src = inputFrame.rgba();

//       Mat gray = inputFrame.gray();

//        float centerPercent=1/3.0f;
//
//        Rect centerRect = new Rect();
//        centerRect.x=0;
//        centerRect.y=(int)(src.height()*centerPercent);
//        centerRect.width=src.width();
//        centerRect.height=(int)(src.height()*centerPercent);
//
//
//        Mat centerCrop = new Mat(src, centerRect);

//        Mat dst = new Mat(src.cols(), src.rows(), src.type());
//        Mat rotateMat = Imgproc.getRotationMatrix2D(new Point(src.cols() / 2, src.rows() / 2), 90, 1);
//        Imgproc.warpAffine(src, dst, rotateMat, dst.size());


//        final Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.RGB_565);
//        Imgproc.rectangle(dst, new Point(0, 0), new Point(bitmap.getWidth() - 5, bitmap.getHeight() - 5), new Scalar(255, 55, 55));
//        Utils.matToBitmap(dst, bitmap);
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                imageViewPreview.setImageBitmap(bitmap);
//            }
//        });
//
//        return dst;

//        Imgproc.line(src, new Point(0, 0), new Point(src.cols(), src.rows()), new Scalar(255, 55, 55));
//        Imgproc.circle(src, new Point(0, 0), 20, new Scalar(255, 55, 55));

        Core.flip(src, src, 1);//翻转左右，不然旋转90度之后会有问题


        final Bitmap bitmapfull = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(src, bitmapfull);
        imageViewPreview2.post(new Runnable() {
            @Override
            public void run() {
                imageViewPreview2.setImageBitmap(adjustPhotoRotation(bitmapfull, 90));
            }
        });
        return src;
    }


    /**
     * 图片旋转90度
     *
     * @param bitmap
     * @param orientationDegree
     * @return
     */
    Bitmap adjustPhotoRotation(Bitmap bitmap, int orientationDegree) {

        Matrix matrix = new Matrix();
        matrix.setRotate(orientationDegree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bitmap.getHeight();
            targetY = 0;
        } else {
            targetX = bitmap.getHeight();
            targetY = bitmap.getWidth();
        }

        final float[] values = new float[9];
        matrix.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        matrix.postTranslate(targetX - x1, targetY - y1);
        Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawBitmap(bitmap, matrix, paint);

        return canvasBitmap;
    }
}
