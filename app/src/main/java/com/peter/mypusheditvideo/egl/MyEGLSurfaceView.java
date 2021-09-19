package com.peter.mypusheditvideo.egl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public abstract class MyEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private Surface surface;
    private EGLContext eglContext;

    private MyEGLThread myEGLThread;
    private MyGLRender myGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    public MyEGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //TODO 这句话不设置，不会调用onSurfaceCreated 等一系列接口 Why?
        getHolder().addCallback(this);
    }

    public void setRender(MyGLRender myGLRender) {
        this.myGLRender = myGLRender;
    }

    public void setRenderMode(int mRenderMode) {

        if(myGLRender == null)
        {
            throw  new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext)
    {
        this.surface = surface;
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext()
    {
        if(myEGLThread != null)
        {
            return myEGLThread.getEglContext();
        }
        return null;
    }

    public void requestRender()
    {
        if(myEGLThread != null)
        {
            myEGLThread.requestRender();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        Exception e = new Exception("my_tag_surfaceCreated");
//        e.printStackTrace();

        if(surface == null)
        {
            surface = holder.getSurface();
        }
        myEGLThread = new MyEGLThread(new WeakReference<MyEGLSurfaceView>(this));
        myEGLThread.isCreate = true;
        myEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        myEGLThread.width = width;
        myEGLThread.height = height;
        myEGLThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        myEGLThread.onDestory();
        myEGLThread = null;
        surface = null;
        eglContext = null;
    }

    public interface MyGLRender
    {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }


    static class MyEGLThread extends Thread {

        private WeakReference<MyEGLSurfaceView> myEGLSurfaceViewWeakReference;
        private EglHelper eglHelper = null;
        private Object object = null;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        private int width;
        private int height;

        public MyEGLThread(WeakReference<MyEGLSurfaceView> myEGLSurfaceViewWeakReference) {
            this.myEGLSurfaceViewWeakReference = myEGLSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(myEGLSurfaceViewWeakReference.get().surface, myEGLSurfaceViewWeakReference.get().eglContext);

            while (true)
            {
                if(isExit)
                {
                    //释放资源
                    release();
                    break;
                }

                if(isStart)
                {
                    if(myEGLSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY)
                    {
                        synchronized (object)
                        {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(myEGLSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY)
                    {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        throw  new RuntimeException("mRenderMode is wrong value");
                    }
                }


                onCreate();
                onChange(width, height);
                onDraw();

                isStart = true;


            }


        }

        private void onCreate()
        {
            if(isCreate && myEGLSurfaceViewWeakReference.get().myGLRender != null)
            {
                isCreate = false;
                myEGLSurfaceViewWeakReference.get().myGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height)
        {
            if(isChange && myEGLSurfaceViewWeakReference.get().myGLRender != null)
            {
                isChange = false;
                myEGLSurfaceViewWeakReference.get().myGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw()
        {
            if(myEGLSurfaceViewWeakReference.get().myGLRender != null && eglHelper != null)
            {
                myEGLSurfaceViewWeakReference.get().myGLRender.onDrawFrame();
                if(!isStart)
                {
                    myEGLSurfaceViewWeakReference.get().myGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        private void requestRender()
        {
            if(object != null)
            {
                synchronized (object)
                {
                    object.notifyAll();
                }
            }
        }

        public void onDestory()
        {
            isExit = true;
            requestRender();
        }


        public void release()
        {
            if(eglHelper != null)
            {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                myEGLSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext()
        {
            if(eglHelper != null)
            {
                return eglHelper.getEglContext();
            }
            return null;
        }

    }

}
