package com.example.myruler;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class RulerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public RulerSurfaceView(Context context) {
        super(context);
        init();
    }

    /**
     * 初始化view相关东西
     */
    private void init() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}
