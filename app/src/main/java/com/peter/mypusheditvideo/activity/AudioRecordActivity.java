package com.peter.mypusheditvideo.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.peter.mypusheditvideo.R;
import com.peter.mypusheditvideo.recordaudio.AudioRecordUtil;

public class AudioRecordActivity extends AppCompatActivity {
    private static final String TAG="my_tag_AudioReActi";
    private AudioRecordUtil audioRecordUtil = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_record);
    }

    public void recordPcm(View view) {
        if(audioRecordUtil == null)
        {
            audioRecordUtil = new AudioRecordUtil("/sdcard/aa_my_record1.pcm", 44100);
           /* audioRecord.setOnRecordLisener(new AudioRecord.OnRecordLisener() {
                @Override
                public void recordByte(byte[] audioData, int readSize) {
                    Log.d(TAG, "readSize is : " + readSize);
                }
            });*/
            audioRecordUtil.startRecord();
        }
        else
        {
            audioRecordUtil.stopRecord();
            audioRecordUtil = null;
        }
    }

}
