package com.peter.mypusheditvideo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.peter.mypusheditvideo.R;
import com.peter.mypusheditvideo.yuv.YuvView;

import java.io.File;
import java.io.FileInputStream;

public class YuvActivity extends AppCompatActivity {
    private static final String TAG="my_tag_YuvActivity";
    private YuvView yuvView;
    private FileInputStream fis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_yuv);

        yuvView = ((YuvView) findViewById(R.id.yuvview));
    }

    public void startPlayYuv(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    int w = 640;
                    int h = 360;
//                    fis = new FileInputStream(new File("/mnt/sdcard/sintel_640_360.yuv"));
                    fis = new FileInputStream(new File("/sdcard/hzw_640_360.yuv"));
                    byte []y = new byte[w * h];
                    byte []u = new byte[w * h / 4];
                    byte []v = new byte[w * h / 4];

                    while (true)
                    {
                        int ry = fis.read(y);
                        int ru = fis.read(u);
                        int rv = fis.read(v);
                        if(ry > 0 && ru > 0 && rv > 0)
                        {
                            yuvView.setFrameData(w, h, y, u, v);
                            Thread.sleep(40);
                        }
                        else
                        {
                            Log.d(TAG, "播放YUV Video完成");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
