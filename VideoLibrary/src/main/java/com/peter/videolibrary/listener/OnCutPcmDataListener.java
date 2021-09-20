package com.peter.videolibrary.listener;

public interface OnCutPcmDataListener {
    void onPcmCutData(byte[] buffer, int size);
    void onPcmDataInfo(int sample_rate, int bit, int channels);
}
