package com.peter.mypusheditvideo.activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.peter.mypusheditvideo.R;
import com.peter.mypusheditvideo.encodec.MediaEncodec;
import com.peter.mypusheditvideo.imgvideo.ImgVideoView;
import com.peter.videolibrary.bean.TimeInfoBean;
import com.peter.videolibrary.listener.MyOnCompleteListener;
import com.peter.videolibrary.listener.OnCutPcmDataListener;
import com.peter.videolibrary.listener.OnPreparedListener;
import com.peter.videolibrary.listener.OnTimeInfoListener;
import com.peter.videolibrary.player.AudioPlayer;

public class ImageVideoActivity extends AppCompatActivity {
    private static final String TAG="my_tag_ImageVideoAct";
    private ImgVideoView imgVideoView;
    private MediaEncodec mediaEncodec;
    private AudioPlayer audioPlayer;
    private Button btnMakeUp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imagevideo);
        imgVideoView = findViewById(R.id.imgvideoview);
        btnMakeUp = ((Button) findViewById(R.id.btn_makeup));

        imgVideoView.setCurrentImg(R.drawable.img_1);

        audioPlayer = AudioPlayer.getInstance();
//        wlMusic.setCallBackPcmData(true); //可以把isShowPcm放到这里设置

        audioPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
//                audioPlayer.cutAudioPlayer(39, 50, true);//ShowPcm表示是否把裁剪的pcm数据回调给App
                audioPlayer.cutAudioPlayer(0, 40,true);
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

            @Override
            public void onPcmDataInfo(int sample_rate, int bit, int channels) {
                //TODO fboTextureId
                mediaEncodec = new MediaEncodec(ImageVideoActivity.this, imgVideoView.getFbotextureid());
                mediaEncodec.initEncodec(imgVideoView.getEglContext(),
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_image_video.mp4",
                        720, 500, sample_rate, channels);
                mediaEncodec.startRecord();
                startImgs();
            }
        });
        audioPlayer.setOnCompleteListener(new MyOnCompleteListener() {
            @Override
            public void onComplete() {
                btnMakeUp.setText("合成完成1");
                Log.d(TAG,"合成完成1");
            }
        });

        audioPlayer.setOnTimeInfoListener(new OnTimeInfoListener() {
            @Override
            public void onTimeInfo(TimeInfoBean timeInfoBean) {
                Log.d(TAG,"play time:"+timeInfoBean.toString());
            }
        });
    }

    private void startImgs()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 1; i <= 257; i++)
                {
                    int imgSrcIndex = getResources().getIdentifier("img_" + i, "drawable", "com.peter.mypusheditvideo");
//                    Log.d(TAG,"setCurrentImg img idx:"+i +", imgSrcIndex:"+imgSrcIndex);
                    imgVideoView.setCurrentImg(imgSrcIndex);
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(mediaEncodec != null)
                {
                    Log.d(TAG,"img play done, to release mediaEncodec resource and stop");
                    audioPlayer.stop();
                    mediaEncodec.stopRecord();
                    mediaEncodec = null;
                }
                btnMakeUp.setText("合成完成2");
                Log.d(TAG,"合成完成2");
            }
        }).start();
    }

    public void startMakeUp(View view) {
        //        audioPlayer.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/the girl.m4a");
//        audioPlayer.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/the girl.m4a");//可惜不是你.mp3
        audioPlayer.setSource("/sdcard/Music/爱的供养.mp3");
        audioPlayer.prepared();
        btnMakeUp.setText("正在合成");
    }
}
