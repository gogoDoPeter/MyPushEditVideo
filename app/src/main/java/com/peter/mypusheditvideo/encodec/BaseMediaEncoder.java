package com.peter.mypusheditvideo.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.peter.mypusheditvideo.egl.EglHelper;
import com.peter.mypusheditvideo.egl.MyEGLSurfaceView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.time.LocalDate;

import javax.microedition.khronos.egl.EGLContext;

public abstract class BaseMediaEncoder {
    private static final String TAG = "my_tag_BaseMediaEncoder";
    private Surface surface;
    private EGLContext eglContext;

    private int width;
    private int height;

    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferinfo;

    private MediaMuxer mediaMuxer;

    private EGLMediaThread eglMediaThread;
    private VideoEncodecThread videoEncodecThread;

    private MyEGLSurfaceView.MyGLRender myGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener onMediaInfoListener;

    public BaseMediaEncoder(Context context) {
    }

    public void setRender(MyEGLSurfaceView.MyGLRender myGLRender) {
        this.myGLRender = myGLRender;
    }

    public void setRenderMode(int mRenderMode) {
        if (myGLRender == null) {
            throw new RuntimeException("must set render mode before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public void initEncodec(EGLContext eglContext, String savePath, String mimeType, int width, int height) {
        Log.d(TAG, "initEncodec +, width=" + width + ", height=" + height);
        this.width = width;
        this.height = height;
        this.eglContext = eglContext;
        initMediaEncodec(savePath, mimeType, width, height);
        Log.d(TAG, "initEncodec -");
    }

    public void startRecord() {
        Log.d(TAG, "startRecord +");
        if (surface != null && eglContext != null) {
            Log.d(TAG, "startRecord new eglMediaThread videoEncodecThread and start");

            eglMediaThread = new EGLMediaThread(new WeakReference<BaseMediaEncoder>(this));
            videoEncodecThread = new VideoEncodecThread(new WeakReference<BaseMediaEncoder>(this));
            eglMediaThread.isCreate = true;
            eglMediaThread.isChange = true;
            eglMediaThread.start();
            videoEncodecThread.start();
        }
        Log.d(TAG, "startRecord -");
    }

    public void stopRecord() {
        Log.d(TAG, "stopRecord +");
        if (eglMediaThread != null && videoEncodecThread != null) {
            videoEncodecThread.exit();
            eglMediaThread.onDestory();
            videoEncodecThread = null;
            eglMediaThread = null;
        }
        Log.d(TAG, "stopRecord -");
    }

    private void initMediaEncodec(String savePath, String mimeType, int width, int height) {
        try {
            mediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoEncodec(mimeType, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVideoEncodec(String mimeType, int width, int height) {
        Log.d(TAG, "initVideoEncodec +");
        try {
            videoBufferinfo = new MediaCodec.BufferInfo();
            videoFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            videoEncodec = MediaCodec.createEncoderByType(mimeType);
            videoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            surface = videoEncodec.createInputSurface();

        } catch (IOException e) {
            e.printStackTrace();
            videoEncodec = null;
            videoFormat = null;
            videoBufferinfo = null;
        }
        Log.d(TAG, "initVideoEncodec -");
    }

    static class EGLMediaThread extends Thread {
        private WeakReference<BaseMediaEncoder> encoder;
        private EglHelper eglHelper;
        private Object object;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public EGLMediaThread(WeakReference<BaseMediaEncoder> encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(encoder.get().surface, encoder.get().eglContext);
            while (true) {
                if (isExit) {
                    release();
                    break;
                }

                if (isStart) {
                    if (encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
//                        Log.d(TAG, "EGLMediaThread run RENDERMODE_WHEN_DIRTY isExit:"+isExit+", isStart:"+isStart);
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
//                            Log.d(TAG, "EGLMediaThread run RENDERMODE_CONTINUOUSLY, sleep isExit:"+isExit+", isStart:"+isStart);
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }
//                Log.d(TAG, "EGLMediaThread run onCreate");
                onCreate();
//                Log.d(TAG, "EGLMediaThread run onChange");
                onChange(encoder.get().width, encoder.get().height);
//                Log.d(TAG, "EGLMediaThread run onDraw");
                onDraw();
                isStart = true;
            }

        }

        private void onCreate() {
            if (isCreate && encoder.get().myGLRender != null) {
                isCreate = false;
                encoder.get().myGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && encoder.get().myGLRender != null) {
                isChange = false;
                encoder.get().myGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (encoder.get().myGLRender != null && eglHelper != null) {
                encoder.get().myGLRender.onDrawFrame();
                if (!isStart) {
                    encoder.get().myGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestory() {
            isExit = true;
            requestRender();
        }

        public void release() {
            if (eglHelper != null) {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                encoder = null;
            }
        }
    }

    static class VideoEncodecThread extends Thread {
        private WeakReference<BaseMediaEncoder> encoder;

        private boolean isExit;

        private MediaCodec videoEncodec;
        private MediaFormat videoFormat;
        private MediaCodec.BufferInfo videoBufferinfo;
        private MediaMuxer mediaMuxer;

        private int videoTrackIndex;
        private long pts;

        public VideoEncodecThread(WeakReference<BaseMediaEncoder> encoder) {
            Log.d(TAG, "VideoEncodecThread constructor");
            this.encoder = encoder;
            videoEncodec = encoder.get().videoEncodec;
            videoFormat = encoder.get().videoFormat;
            videoBufferinfo = encoder.get().videoBufferinfo;
            mediaMuxer = encoder.get().mediaMuxer;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            videoTrackIndex = -1;
            isExit = false;
            videoEncodec.start();

            while (true) {
                if (isExit) {
                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;

                    mediaMuxer.stop();
                    mediaMuxer.release();
                    mediaMuxer = null;
                    Log.d("my_tag", "VideoEncodecThread run 录制完成");
                    break;
                }

                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
//                Log.d(TAG, "VideoEncodecThread run, first outputBufferIndex:"+outputBufferIndex);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
                    Log.d(TAG, "VideoEncodecThread run mediaMuxer.start");
                    mediaMuxer.start();
                } else {
                    while (outputBufferIndex >= 0) {
//                        int tempNewLimit=videoBufferinfo.offset + videoBufferinfo.size;
//                        Log.d(TAG, "VideoEncodecThread run, get outputBuffer, outputBufIdx:"+outputBufferIndex+
//                                ", offset:"+videoBufferinfo.offset+", limit total size:"+tempNewLimit);

                        ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferinfo.offset);
                        outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);

                        //
                        if (pts == 0) {
                            pts = videoBufferinfo.presentationTimeUs;
                        }
//                        Log.d(TAG, "VideoEncodecThread run set videoBufferinfo.presentationTimeUs, pts:" + pts);
                        videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;
//                        Log.d(TAG, "VideoEncodecThread run mediaMuxer.writeSampleData, videoTrackIndex:" + videoTrackIndex +
//                                ", videoBufferinfo.presentationTimeUs:" + videoBufferinfo.presentationTimeUs);
                        mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, videoBufferinfo);
                        if (encoder.get().onMediaInfoListener != null) {
                            encoder.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }

                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
//                        Log.d(TAG, "VideoEncodecThread run, get new outputBufferIndex:"+outputBufferIndex);
                    }
                }
            }

        }

        public void exit() {
            isExit = true;
        }

    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);
    }
}
