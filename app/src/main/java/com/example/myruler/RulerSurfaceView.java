package com.example.myruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class RulerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    /**
     * 刻度线和刻度线文字颜色
     */
    public static final int COLOR_1 = 0xff666666;
    /**
     * 测量参考线颜色
     */
    public static final int COLOR_2 = 0xffff0000;

    /**
     * 屏幕宽度
     */
    private int WIDTH;
    /**
     * 屏幕高度
     */
    private int HEIGHT;
    private boolean isFlash;
    /**
     * 刻度线画笔
     */
    private Paint paintTickMarks;
    /**
     * 文字大小 -> 20dp
     */
    private float FONT_SIZE;
    /**
     * 刻度线文字画笔
     */
    private Paint paintText;
    /**
     * 刻度值 -> 1mm
     */
    private float SCALE_VALUE;
    /**
     * 刻度尺内收缩 -> 2dp
     */
    private float PADDING;
    /**
     * 整数刻度线长度 -> 30dp
     */
    private float SCALE_LENGTH;
    /**
     * 上一次测量线的位置
     */
    private float lastLineX;
    /**
     * 当前测量线位置
     */
    private float curLineX;
    /**
     * 测量线能否被拖动
     */
    boolean isAbleMoveLine;
    private Paint paintMeasure;
    /**
     * 测量参考线长度
     */
    private float MEASURE_LINE_LENGTH;
    private float startX;

    public RulerSurfaceView(Context context) {
        super(context);
        init(context);
    }

    /**
     * 初始化view相关东西
     */
    private void init(Context context) {
        /*
        主要工作：
            1、设置画笔
            2、设置数值
         */
        getHolder().addCallback(this);
        // 显示屏幕
        DisplayMetrics dm = getResources().getDisplayMetrics();
        WIDTH = dm.widthPixels;
        Log.d("yangxj", "width: " + WIDTH);
        HEIGHT = dm.heightPixels;
        Log.d("yangxj", "height: " + HEIGHT);

        /*WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);

        WIDTH = dm.widthPixels;
        Log.d("yangxj", "width: " + WIDTH);
        HEIGHT = dm.heightPixels;
        Log.d("yangxj", "height: " + HEIGHT);*/

        FONT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm);
        SCALE_VALUE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, dm);
        PADDING = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
        SCALE_LENGTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm);
        curLineX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
        lastLineX = curLineX;
        MEASURE_LINE_LENGTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, dm);

        // 尺子刻度线
        paintTickMarks = new Paint();
        paintTickMarks.setStyle(Paint.Style.STROKE);
        paintTickMarks.setColor(COLOR_1);
        // 刻度线文字
        paintText = new Paint();
        paintText.setTextSize(FONT_SIZE);
        paintText.setAntiAlias(true);
        paintText.setColor(COLOR_1);
        // 测量参考线
        paintMeasure = new Paint();
        paintMeasure.setStyle(Paint.Style.STROKE);
        paintMeasure.setStrokeWidth(1);
        paintMeasure.setColor(COLOR_2);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        isFlash = true;
        FlashThread flashThread = new FlashThread();
        flashThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        isFlash = false;
    }

    /**
     * 绘图
     */
    private void doDraw() {
        // 画布
        Canvas canvas = getHolder().lockCanvas();
        // 背景底色设为白色
        canvas.drawRGB(255, 255, 255);
        // 画刻度线和刻度文字
        drawTickMarksAndText(canvas);
        // 画尺子测量参考线和测量结果
        drawLineAndResule(canvas);

        getHolder().unlockCanvasAndPost(canvas);
    }

    /**
     * 画刻度线和刻度文字
     */
    private void drawTickMarksAndText(Canvas canvas) {
        float left = PADDING;
        // 刻度线长度占比
        // 1cm: 1  0.5cm: 0.75  1mm: 0.5
        float tickMarksRatio;
        for (int i = 0; WIDTH - left - PADDING> 0; i++) {
            tickMarksRatio = 0.5f;
            if (i % 5 == 0) {
                // i代表刻度线的个数，如果是到5个就是0.5cm，如果是到10个就是1cm
                if ((i & 0x1) == 0) {
                    // 整数cm
                    tickMarksRatio = 1f;

                } else {
                    tickMarksRatio = 0.7f;
                }
            }
            canvas.drawLine(left,0,
                    left,tickMarksRatio * SCALE_LENGTH,
                    paintTickMarks);
            left += SCALE_VALUE;
        }
    }

    /**
     * 画尺子测量参考线和测量结果
     */
    private void drawLineAndResule(Canvas canvas) {
        canvas.drawLine(curLineX, 0,
                curLineX, MEASURE_LINE_LENGTH,
                paintMeasure);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                touchDone(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_DOWN:
                touchStart(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event.getX(), event.getY());
                break;
        }
        return true;
    }

    /**
     * 手指接触屏幕
     * @param x
     * @param y
     */
    private void touchStart(float x, float y) {
        float lineSpace = Math.abs(x - lastLineX);
        if (lineSpace <= PADDING * 4) {
            // 手指点到测量线附近
            startX = x;
            isAbleMoveLine = true;
        }
    }

    /**
     * 手指滑动
     * @param x
     * @param y
     */
    private void touchMove(float x, float y) {
        if (isAbleMoveLine) {
            curLineX = x - startX + lastLineX;
            if (curLineX <= PADDING) {
                curLineX = PADDING;
            }
            // TODO: 2022/9/12 大于屏幕宽度时做判断
            startX = x;
            lastLineX = curLineX;
            doDraw();
        }
    }

    /**
     * 手指离开屏幕
     * @param x
     * @param y
     */
    private void touchDone(float x, float y) {
        isAbleMoveLine = false;
    }

    class FlashThread extends Thread {
        @Override
        public void run() {
            /*while (isFlash) {
                doDraw();
                try {
                    // 每500ms绘制一次
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
            doDraw();
        }
    }
}
