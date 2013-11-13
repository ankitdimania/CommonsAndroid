package com.grootcode.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.grootcode.android.R;

public class InterceptedViewPager extends ViewPager {
    private GestureDetector mGestureDetector;
    private double mHeightToWidthRatio;

    public InterceptedViewPager(Context context) {
        super(context);
        init(context);
    }

    public InterceptedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GraphViewPager, 0, 0);

        try {
            mHeightToWidthRatio = a.getFloat(R.styleable.GraphViewPager_heightToWidthRatio, 0f);
        } finally {
            a.recycle();
        }
    }

    public void init(Context context) {
        mGestureDetector = new GestureDetector(context, new XScrollDetector());
        setFadingEdgeLength(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeightToWidthRatio != 0) {
            int h = getDefaultSize(0, heightMeasureSpec);
            int w = getDefaultSize(0, widthMeasureSpec);
            if (h == 0) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (w * mHeightToWidthRatio), MeasureSpec.EXACTLY);
            } else if (w == 0) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (h / mHeightToWidthRatio), MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    // Return false if we're scrolling in the x direction
    private class XScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceY) < Math.abs(distanceX)) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
        }
    }
}
