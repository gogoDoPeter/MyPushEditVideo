package com.peter.mypusheditvideo.activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.peter.mypusheditvideo.R;
import com.peter.videolibrary.player.AudioPlayer;

public class Record2Activity extends AppCompatActivity {
    private static final String TAG="my_tag_Record2Act";
    private AudioPlayer audioPlayer = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_record2);
        audioPlayer = AudioPlayer.getInstance();
    }

    public void startRecordPcm2(View view) {
        Log.d(TAG,"startRecordPcm2 ");
        audioPlayer.startRecordAudio(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_opensl_record.pcm");
    }

    public void stopRecordPcm2(View view) {
        Log.d(TAG,"stopRecordPcm2 ");

        audioPlayer.stopRecordAudio();
    }
}
