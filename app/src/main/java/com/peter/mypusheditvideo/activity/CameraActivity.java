package com.peter.mypusheditvideo.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.peter.mypusheditvideo.R;
import com.peter.mypusheditvideo.camera.CameraView;

public class CameraActivity extends AppCompatActivity {

    private CameraView myCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        myCameraView = (CameraView)findViewById(R.id.cameraview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myCameraView.onDestory();
    }

    public void switchCamera(View view) {
    }
}
