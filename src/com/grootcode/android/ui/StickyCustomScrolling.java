package com.grootcode.android.ui;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.grootcode.android.R;
import com.grootcode.android.ui.widget.ObservableScrollView;
import com.grootcode.android.util.UIUtils;

public class StickyCustomScrolling implements ObservableScrollView.Callbacks {

    private View mStickyPlaceholderView;
    private View mStickyView;
    private ObservableScrollView mScrollView;
    private int mStickyViewTopPadding = 0;
    private final Rect mBufferRect = new Rect();

    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            onScrollChanged();
        }
    };

    private final ViewTreeObserver.OnPreDrawListener mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            ViewGroup.LayoutParams stickyPlaceHolderLayoutParams = mStickyPlaceholderView.getLayoutParams();
            if (stickyPlaceHolderLayoutParams.height != mStickyView.getMeasuredHeight()) {
                stickyPlaceHolderLayoutParams.height = mStickyView.getMeasuredHeight();
                mStickyPlaceholderView.setLayoutParams(stickyPlaceHolderLayoutParams);
            }
            return true;
        }
    };

    public void setupCustomScrolling(View rootView) {

        mStickyPlaceholderView = rootView.findViewById(R.id.sticky_placeholder);
        mStickyView = rootView.findViewById(R.id.sticky_view);
        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_view);

        ViewTreeObserver scrollViewVTO = mScrollView.getViewTreeObserver();
        if (scrollViewVTO.isAlive()) {
            scrollViewVTO.addOnGlobalLayoutListener(mGlobalLayoutListener);
            scrollViewVTO.addOnPreDrawListener(mPreDrawListener);
        }

        ViewTreeObserver stickyViewVTO = mStickyView.getViewTreeObserver();
        if (stickyViewVTO.isAlive()) {
            stickyViewVTO.addOnPreDrawListener(mPreDrawListener);
        }
    }

    /**
     * Cleanup the ViewTreeObserver setup if any.
     * 
     * @param rootView
     */
    // removeGlobalOnLayoutListener internally calls removeOnGlobalLayoutListener
    // removeGlobalOnLayoutListener required to support pre JellyBean API 16
    @SuppressWarnings("deprecation")
    public void cleaupCustomScrolling(View rootView) {

        if (mScrollView != null) {
            ViewTreeObserver vto = mScrollView.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.removeGlobalOnLayoutListener(mGlobalLayoutListener);
            }
        }

        if (mStickyView != null) {
            ViewTreeObserver vto = mStickyView.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.removeOnPreDrawListener(mPreDrawListener);
            }
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        onScrollChanged();
    }

    /**
     * Cache old top so that we don't do the whole rework in case the new top is same as old top.
     */
    private float oldTop = -1;

    private void onScrollChanged() {
        float newTop = getNewTop();

        if (newTop != oldTop) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mStickyView.getLayoutParams();
            UIUtils.setTranslationY(mStickyView, newTop);

            mScrollView.getGlobalVisibleRect(mBufferRect);
            int parentLeft = mBufferRect.left;
            int parentRight = mBufferRect.right;
            if (mStickyPlaceholderView.getGlobalVisibleRect(mBufferRect)) {
                lp.leftMargin = mBufferRect.left - parentLeft;
                lp.rightMargin = parentRight - mBufferRect.right;
            }
            mStickyView.setLayoutParams(lp);

            oldTop = newTop;
        }
    }

    public void setStickyViewTopPadding(int padding) {
        if (padding != mStickyViewTopPadding) {
            mStickyViewTopPadding = padding;
            float newTop = getNewTop();
            if (newTop != oldTop) {
                UIUtils.setTranslationY(mStickyView, newTop, UIUtils.ANIMATION_TRANSLATE_TIME);
                oldTop = newTop;
            }
        }
    }

    private int getPlaceHolderTop() {
        int top = mStickyPlaceholderView.getTop();

        ViewGroup parent = (ViewGroup) mStickyPlaceholderView.getParent();
        while (parent != mScrollView) {
            top += parent.getTop();
            parent = (ViewGroup) parent.getParent();
        }
        return top;
    }

    private float getNewTop() {
        return Math.max(getPlaceHolderTop(), mScrollView.getScrollY() + mStickyViewTopPadding);
    }
}
