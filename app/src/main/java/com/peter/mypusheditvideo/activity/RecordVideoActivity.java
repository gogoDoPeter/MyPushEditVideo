package com.peter.mypusheditvideo.activity;

import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.peter.mypusheditvideo.R;
import com.peter.mypusheditvideo.camera.CameraView;
import com.peter.mypusheditvideo.encodec.BaseMediaEncoder;
import com.peter.mypusheditvideo.encodec.MediaEncodec;

public class RecordVideoActivity extends AppCompatActivity {

    private CameraView cameraView;
    private Button btnRecord;
    private MediaEncodec mediaEncodec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        cameraView = ((CameraView) findViewById(R.id.cameraview));
        btnRecord = ((Button) findViewById(R.id.btn_record));
    }

    public void record(View view) {
        if(mediaEncodec == null)
        {
            Log.d("my_tag", "textureId:" + cameraView.getTextureId());
            mediaEncodec = new MediaEncodec(this, cameraView.getTextureId());
            mediaEncodec.initEncodec(cameraView.getEglContext(),
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_camera_record.mp4",
                    MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280);

            mediaEncodec.setOnMediaInfoListener(new BaseMediaEncoder.OnMediaInfoListener() {
                @Override
                public void onMediaTime(int times) {
                    Log.d("my_tag", "time is: " + times);
                }
            });

            mediaEncodec.startRecord();
            btnRecord.setText("正在录制");
        }
        else
        {
            mediaEncodec.stopRecord();
            btnRecord.setText("开始录制");
            mediaEncodec = null;
        }
    }
}
