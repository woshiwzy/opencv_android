package org.opencv.samples.tutorial1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class StartActivity extends Activity {


    private int REQUEST_PERMISSION_CAMERA_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        findViewById(R.id.buttonOpen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //当前系统大于等于6.0
                    if (ContextCompat.checkSelfPermission(StartActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        //具有拍照权限，直接调用相机
                        //具体调用代码
                        startCampera();
                    } else {
                        //不具有拍照权限，需要进行权限申请
                        ActivityCompat.requestPermissions(StartActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA_CODE);
                    }
                } else {
                    //当前系统小于6.0，直接调用拍照
                    startCampera();
                }

            }
        });
    }

    private void startCampera() {
        Intent intent = new Intent(this, FrontCameraRotate90PreviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSION_CAMERA_CODE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            startCampera();
        }

    }
}
