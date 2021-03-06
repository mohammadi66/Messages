/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amlcurran.messages.ui;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.amlcurran.messages.R;

public class LoadingView extends View implements TimeAnimator.TimeListener {

    public static final float ANIMATION_DURATION = 1500f;
    private final int scaleFactor;
    private final Paint linePaint;
    private final Drawable bubbleDrawable;
    private final float fourDip;
    private float duration;
    private int width;
    private int height;

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.LoadingView);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoadingView, defStyleAttr, R.style.LoadingView);
        scaleFactor = array.getInteger(R.styleable.LoadingView_lv_scale, 1);
        array.recycle();

        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        bubbleDrawable = context.getResources().getDrawable(R.drawable.ic_sending_bg);
        fourDip = context.getResources().getDisplayMetrics().density * 4 / scaleFactor;
        setUpDimensions();
        if (isInEditMode()) {
            duration = 1200f;
        } else {
            setUpAnimator();
        }
    }

    private void setUpDimensions() {
        width = bubbleDrawable.getIntrinsicWidth() / scaleFactor;
        height = bubbleDrawable.getIntrinsicHeight() / scaleFactor;
        bubbleDrawable.setBounds(0, 0, bubbleDrawable.getIntrinsicWidth(), bubbleDrawable.getIntrinsicHeight());
    }

    private void setUpAnimator() {
        TimeAnimator animator = new TimeAnimator();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setTimeListener(this);
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(1 / (float) scaleFactor, 1 / (float) scaleFactor);
        bubbleDrawable.draw(canvas);
        canvas.restore();
        for (int i = 0; i < 3; i++) {
            linePaint.setAlpha((int) (255 * getAlphaFraction(i)));
            float lineLeft = 2 * fourDip;
            float lineWidth = 8 * fourDip * getWidthFraction(i);
            float lineTop = (3 + 2 * i) * fourDip;
            float lineHeight = fourDip;
            canvas.drawRect(lineLeft, lineTop, lineLeft + lineWidth, lineTop + lineHeight, linePaint);
        }
    }

    private float getAlphaFraction(int i) {
        float animationFraction = duration / ANIMATION_DURATION;
        animationFraction = (animationFraction - 0.75f) * 4;
        return bound(1 - animationFraction);
    }

    private float getWidthFraction(int i) {
        float animationFraction = duration / ANIMATION_DURATION;
        return bound(3 * (animationFraction - i * 0.25f));
    }

    private static float bound(float v) {
        v = Math.min(v, 1f);
        return Math.max(v, 0f);
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        duration = Math.abs(totalTime % ANIMATION_DURATION);
        invalidate();
    }
}
