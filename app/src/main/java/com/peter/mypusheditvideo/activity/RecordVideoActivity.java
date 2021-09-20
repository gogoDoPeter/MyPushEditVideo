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
import com.peter.videolibrary.listener.MyOnCompleteListener;
import com.peter.videolibrary.listener.OnCutPcmDataListener;
import com.peter.videolibrary.listener.OnPreparedListener;
import com.peter.videolibrary.player.AudioPlayer;

public class RecordVideoActivity extends AppCompatActivity {

    private CameraView cameraView;
    private Button btnRecord;
    private MediaEncodec mediaEncodec;

    private AudioPlayer audioPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        cameraView = ((CameraView) findViewById(R.id.cameraview));
        btnRecord = ((Button) findViewById(R.id.btn_record));

        audioPlayer=AudioPlayer.getInstance();

        audioPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                audioPlayer.cutAudioPlayer(39, 50, true);//ShowPcm表示是否把裁剪的pcm数据回调给App
            }
        });

        audioPlayer.setOnCompleteListener(new MyOnCompleteListener() {
            @Override
            public void onComplete() {
                if(mediaEncodec != null)
                {
                    mediaEncodec.stopRecord();
                    mediaEncodec = null;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRecord.setText("开始录制");
                        }
                    });
                }
            }
        });

        audioPlayer.setOnCutPcmDataListener(new OnCutPcmDataListener() {
            @Override
            public void onPcmCutData(byte[] buffer, int size) {
                if(mediaEncodec != null)
                {
                    mediaEncodec.putPCMData(buffer, size);
                }
            }

/*                    Log.d("my_tag", "textureId:" + cameraView.getTextureId());
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
        mediaEncodec.startRecord();*/

            @Override
            public void onPcmDataInfo(int sample_rate, int bit, int channels) {
                mediaEncodec = new MediaEncodec(RecordVideoActivity.this, cameraView.getTextureId());
                String fileName=Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_makeup.mp4";
                Log.d("my_tag", "textureid:" + cameraView.getTextureId()+", fileName1:"+fileName);
                mediaEncodec.initEncodec(cameraView.getEglContext(),
                        "/mnt/sdcard/aa_makeup.mp4",
                        720, 1280,
                        sample_rate, channels);
                mediaEncodec.setOnMediaInfoListener(new BaseMediaEncoder.OnMediaInfoListener() {
                    @Override
                    public void onMediaTime(int times) {
                        Log.d("my_tag", "play time: " + times);
                    }
                });
                mediaEncodec.startRecord();
            }
        });
    }

    public void record(View view) {
        if(mediaEncodec == null)
        {
            audioPlayer.setSource("/sdcard/Music/爱的供养.mp3");
            audioPlayer.prepared();

            btnRecord.setText("正在录制");
        }
        else
        {
            mediaEncodec.stopRecord();
            btnRecord.setText("开始录制");
            mediaEncodec = null;
            audioPlayer.stop();
        }
    }
}
