package com.hc.clock;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by admin on 2017/6/1.
 */

public class Clock extends View {
    //用来记录当前时间
    private Time mTime;
    private Timer timer = new Timer(true);
    //用来存放三张图片资源
    private Drawable biaopan;
    private Drawable shizhen;
    private Drawable fenzhen;
    private Drawable miaozhen;

    //用来记录表盘图片的宽和高
    private int biaopanWidth;
    private int biaopanheight;

    //用来记录View是否被加入到了Window中
    private boolean mAttached;

    private float minutes;
    private float hours;
    private float seconds;

    //用来跟踪View 的尺寸的变化
    private boolean mChanged;

    public Clock(Context context) {
        this(context, null);
    }

    public Clock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Clock(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Clock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final Resources resources = context.getResources();
        if (biaopan == null) {
            biaopan = context.getDrawable(R.mipmap.clock_dial);
        }
        if (shizhen == null) {
            shizhen = context.getDrawable(R.mipmap.clock_hand_hour);
        }
        if (fenzhen == null) {
            fenzhen = context.getDrawable(R.mipmap.clock_hand_minute);
        }
        if (miaozhen == null) {
            miaozhen = context.getDrawable(R.mipmap.clock_hand_minute);
        }

        mTime = new Time();
        biaopanWidth = biaopan.getIntrinsicWidth();
        biaopanheight = biaopan.getIntrinsicHeight();
    }

    //绑定视图，出现
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            TimerTask task = new TimerTask() {
                public void run() {
                    handler.sendMessage(handler.obtainMessage(1));
                }
            };
            //启动定时器
            timer.schedule(task, 0, 1000);
        }
        mTime = new Time();
    }

    //
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                //更新时间
                onTimeChanged();
                //重绘
                invalidate();
            }
        }
    };

    //消失就取消监听
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            mAttached = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < biaopanWidth) {
            hScale = (float) widthSize / (float) biaopanWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < biaopanheight) {
            vScale = (float) heightSize / (float) biaopanheight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (biaopanWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (biaopanheight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    //监听事件变化
    private void onTimeChanged() {
        mTime.setToNow();

        int hour = mTime.hour;
        int minute = mTime.minute;
        int second = mTime.second;

        /*Calendar可以是Linient模式，
            此模式下，minute和hour是可能超过60和24的*/
        seconds = second;
        minutes = minute + second / 60.0f;
        hours = hour + minutes / 60.0f;
        mChanged = true;
        Log.e("second", second + "");
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //View尺寸变化后，我们用changed变量记录下来，
        //同时，恢复mChanged为false，以便继续监听View的尺寸变化。
        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = super.getRight() - super.getLeft();
        int availableHeight = super.getBottom() - super.getTop();

        //x,y为view中心
        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable biao = biaopan;
        int w = biao.getIntrinsicWidth();
        int h = biao.getIntrinsicHeight();
        boolean scaled = false;


        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w, (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            biao.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        biao.draw(canvas);
        canvas.save();

        canvas.rotate(hours / 12.0f * 360.0f, x, y);
        final Drawable hourHand = shizhen;

        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();

            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();
        canvas.save();

        canvas.rotate(minutes / 60.0f * 360.0f, x, y);
        final Drawable minuteHand = fenzhen;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();

            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();
        canvas.save();

        canvas.rotate(seconds / 60.0f * 360.0f, x, y);
        final Drawable miaohand = miaozhen;
        if (changed) {
            w = miaohand.getIntrinsicWidth();
            h = miaohand.getIntrinsicHeight();

            miaohand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        miaohand.draw(canvas);
        canvas.restore();
        if (scaled) {
            canvas.restore();
        }


    }
}
