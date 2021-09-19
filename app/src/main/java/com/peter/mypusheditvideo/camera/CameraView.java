package com.peter.mypusheditvideo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.peter.mypusheditvideo.egl.MyEGLSurfaceView;

public class CameraView extends MyEGLSurfaceView {
    private MyCameraRender myCameraRender;
    private MyCamera myCamera;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private int textureId = -1;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        myCameraRender = new MyCameraRender(context);
        myCamera = new MyCamera(context);
        setRender(myCameraRender);
        previewAngle(context);

        myCameraRender.setOnSurfaceCreateListener(new MyCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int textureId_) {
                myCamera.initCamera(surfaceTexture, cameraId);
                textureId = textureId_;
            }
        });
    }

    public void onDestory()
    {
        if(myCamera != null)
        {
            myCamera.stopPreview();
        }
    }

    public void previewAngle(Context context)
    {
        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        myCameraRender.resetMatrix();
        switch (angle)
        {
            case Surface.ROTATION_0:
                Log.d("my_tag", "previewAngle 0");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    myCameraRender.setAngle(90, 0, 0, 1);
                    myCameraRender.setAngle(180, 1, 0, 0);
                }
                else
                {
                    myCameraRender.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                Log.d("my_tag", "previewAngle 90");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    myCameraRender.setAngle(180, 0, 0, 1);
                    myCameraRender.setAngle(180, 0, 1, 0);
                }
                else
                {
                    myCameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                Log.d("my_tag", "previewAngle 180");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    myCameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    myCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                }
                else
                {
                    myCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                Log.d("my_tag", "previewAngle 270");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    myCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                }
                else
                {
                    myCameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public int getTextureId()
    {
        return textureId;
    }
}
