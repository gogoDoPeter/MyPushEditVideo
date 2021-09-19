package com.peter.mypusheditvideo.yuv;

import android.content.Context;
import android.util.AttributeSet;

import com.peter.mypusheditvideo.egl.MyEGLSurfaceView;

public class YuvView extends MyEGLSurfaceView {
    private YuvRender yuvRender;

    public YuvView(Context context) {
        this(context, null);
    }

    public YuvView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YuvView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        yuvRender = new YuvRender(context);
        setRender(yuvRender);
        setRenderMode(MyEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    public void setFrameData(int w, int h, byte[] by, byte[] bu, byte[] bv)
    {
        if(yuvRender != null)
        {
            yuvRender.setFrameData(w, h, by, bu, bv);
            requestRender();
        }
    }
}
