package com.peter.mypusheditvideo.imgvideo;

import android.content.Context;
import android.util.AttributeSet;

import com.peter.mypusheditvideo.egl.MyEGLSurfaceView;

public class ImgVideoView extends MyEGLSurfaceView {

    private ImgVideoRender imgVideoRender;
    private int fbotextureid;

    public ImgVideoView(Context context) {
        this(context, null);
    }

    public ImgVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImgVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        imgVideoRender = new ImgVideoRender(context);
        setRender(imgVideoRender);
        setRenderMode(MyEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
        imgVideoRender.setOnRenderCreateListener(new ImgVideoRender.OnRenderCreateListener() {
            @Override
            public void onCreate(int textureId) {
                fbotextureid = textureId;
            }
        });
    }

    public void setCurrentImg(int imgsr)
    {
        if(imgVideoRender != null)
        {
            imgVideoRender.setCurrentImgSrc(imgsr);
            requestRender();
        }
    }

    public int getFbotextureid() {
        return fbotextureid;
    }
}
