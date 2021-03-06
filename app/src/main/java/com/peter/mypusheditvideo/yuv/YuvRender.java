package com.peter.mypusheditvideo.yuv;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.peter.mypusheditvideo.R;
import com.peter.mypusheditvideo.egl.MyEGLSurfaceView;
import com.peter.mypusheditvideo.util.ShaderUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class YuvRender implements MyEGLSurfaceView.MyGLRender {
    private static final String TAG="my_tag_MyYuvRender";
    private Context context;
    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f,1f,
            -1f,1f,
            1f,-1f,
            -1f,-1f
    };

    private FloatBuffer textureBuffer;
    private final float[] textureVertexData = {
            1f,0f,
            0f,0f,
            1f,1f,
            0f,1f
    };

    private int program;
    private int vPosition;
    private int fPosition;

    private int sampler_y;
    private int sampler_u;
    private int sampler_v;

    private int [] texture_yuv;

    private int fboId;
    private int textureid;

    int yuvWidth;
    int yuvHeight;

    Buffer y;
    Buffer u;
    Buffer v;

    private ScreenRender screenRender;

    private float[] matrix = new float[16];
    private int u_matrix;

    public YuvRender(Context context) {
        Log.d(TAG,"YuvRender constructor, dataWidth="+yuvWidth+", dataHeight="+yuvHeight);
        this.context = context;
        screenRender = new ScreenRender(context);
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureBuffer.position(0);
        Matrix.setIdentityM(matrix, 0);

    }

    //TODO width --> surfaceWidth, height-->SurfaceHeight
    public void setMatrix(int width, int height){
//        initMatrix(matrix);
        Matrix.setIdentityM(matrix, 0);

        if(yuvWidth > 0 && yuvHeight > 0)
        {
            float screen_r = 1.0f * width / height;
            float picture_r = 1.0f * yuvWidth / yuvHeight;

            if(screen_r > picture_r) //??????????????????
            {
                float r = width / (1.0f * height / yuvHeight * yuvWidth);
//                orthoM(-r, r, -1, 1, matrix);
                Matrix.orthoM(matrix,0, -r, r, -1, 1,-1,1);

            } else{//??????????????????
                float r = height / (1.0f * width / yuvWidth * yuvHeight);
//                orthoM(-1, 1, -r, r, matrix);
                Matrix.orthoM(matrix,0,-1, 1, -r, r, -1,1);//TODO offset 0, default near -1, far 1
            }
        }
    }

    @Override
    public void onSurfaceCreated() {
        Log.d(TAG,"onSurfaceCreated +, dataWidth="+yuvWidth+", dataHeight="+yuvHeight);
        screenRender.onCreate();
        String vertexShader = ShaderUtil.getRawResource(context, R.raw.vertex_shader_yuv);
        String fragmentShader = ShaderUtil.getRawResource(context, R.raw.fragment_shader_yuv);
        program = ShaderUtil.createProgram(vertexShader, fragmentShader);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        u_matrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        sampler_y = GLES20.glGetUniformLocation(program, "sampler_y");
        sampler_u = GLES20.glGetUniformLocation(program, "sampler_u");
        sampler_v = GLES20.glGetUniformLocation(program, "sampler_v");

        texture_yuv = new int[3];
        GLES20.glGenTextures(3, texture_yuv, 0);

        for(int i = 0; i < 3; i++)
        {
            Log.d(TAG,"texture_yuv["+i+"] id = "+texture_yuv[i]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        int []textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureid = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);
        Log.d(TAG,"fbo id="+fboId+", textureId="+textureid);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Log.d(TAG,"bind textureId to framebuffer object, fbo id="+fboId+", textureId="+textureid);
        //surfaceWidth:1440, surfaceHeight:875
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 720, 500,
//                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1440, 875,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureid, 0);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
        {
            Log.e(TAG, "fbo wrong");
        }
        else
        {
            Log.e(TAG, "fbo success");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.d(TAG,"surfaceWidth:"+width+", surfaceHeight:"+height);
//        Matrix.rotateM(matrix, 0, 180f, 1, 0, 0);
        GLES20.glViewport(0, 0, width, height);
        setMatrix(width, height);
        Matrix.rotateM(matrix, 0, 180f, 1, 0, 0);
        screenRender.onChange(width, height);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        if(yuvWidth > 0 && yuvHeight > 0 && y != null && u != null && v != null)
        {
            GLES20.glUseProgram(program);
            GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);
            GLES20.glEnableVertexAttribArray(vPosition);
            GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false,
                    8, vertexBuffer);
            GLES20.glEnableVertexAttribArray(fPosition);
            GLES20.glVertexAttribPointer(fPosition,2,GLES20.GL_FLOAT,false,8, textureBuffer);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, yuvWidth, yuvHeight,
                    0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
            GLES20.glUniform1i(sampler_y, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, yuvWidth / 2, yuvHeight / 2,
                    0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    u);
            GLES20.glUniform1i(sampler_u, 1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_yuv[2]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, yuvWidth / 2, yuvHeight / 2,
                    0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    v);
            GLES20.glUniform1i(sampler_v, 2);

            y.clear();
            u.clear();
            v.clear();

            y = null;
            u = null;
            v = null;
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        screenRender.onDraw(textureid);
    }

    public void setFrameData(int width, int height, byte[] by, byte[] bu, byte[] bv) {
//        Log.d(TAG,"setFrameData +, dataWidth="+width+", dataHeight="+height);
        this.yuvWidth=width;
        this.yuvHeight=height;

        setMatrix(width, height);
        Matrix.rotateM(matrix, 0, 180f, 1, 0, 0);

        this.y = ByteBuffer.wrap(by);
        this.u = ByteBuffer.wrap(bu);
        this.v = ByteBuffer.wrap(bv);

    }
}
