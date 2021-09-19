package com.peter.mypusheditvideo.encodec;

import android.content.Context;

public class MediaEncodec extends BaseMediaEncoder{
    private EncodecRender encodecRender;

    public MediaEncodec(Context context, int textureId) {
        super(context);

        encodecRender = new EncodecRender(context, textureId);
        setRender(encodecRender);
        setRenderMode(BaseMediaEncoder.RENDERMODE_CONTINUOUSLY);

    }
}
