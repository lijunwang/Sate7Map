package com.sate7.geo.map.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.sate7.geo.map.R;

/**
 * Author:      Sbingo
 * Date:        2016/11/20 0020
 * Time:        13:54
 * Description: a moveable radiogroup could fade out, and auto back to its parent's left or right,
 */

public class DragableLayout extends FrameLayout {
    private final String TAG = "DragableLayout";
    private MyCountDownTimer countDownTimer;
    /**
     * 倒计时时间
     */
    private long millisInFuture;
    /**
     * 倒计时过程中
     * 回调{@link CountDownTimer#onTick(long)}的间隔时间
     */
    private long countDownInterval = 500;
    private float currentX;
    private float currentY;
    private int currentLeft;
    private int currentTop;
    private int parentWidth;
    private int parentHeight;
    private int viewWidth;
    private int viewHight;
    private int minLeftMargin;
    private int maxLeftMargin;
    private int rightDistance;
    private int minTopMargin;
    private int maxTopMargin;
    private int bottomDistance;
    private int leftPadding;
    private int topPadding;
    private boolean moveable;
    private boolean autoBack;
    private float toAlpha;

    public DragableLayout(Context context) {
        this(context, null);
    }

    public DragableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.free);
        millisInFuture = ta.getInt(R.styleable.free_millisInFuture, 3 * 1000);
        toAlpha = ta.getFloat(R.styleable.free_toAlpha, 0.2f);
        moveable = ta.getBoolean(R.styleable.free_moveable, false);
        autoBack = ta.getBoolean(R.styleable.free_autoBack, false);
        ta.recycle();
        countDownTimer = new MyCountDownTimer(millisInFuture, countDownInterval);
        countDownTimer.start();
    }

    public void show() {
        setVisibility(VISIBLE);
//        layout(0, 500, getWidth(), 500 + getHeight());
//        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (moveable) {
            ViewGroup parentView = ((ViewGroup) getParent());
            MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
            viewWidth = getRight() - getLeft();
            viewHight = getBottom() - getTop();
            parentWidth = parentView.getMeasuredWidth();
            parentHeight = parentView.getMeasuredHeight();
            minLeftMargin = lp.leftMargin;
            leftPadding = parentView.getPaddingLeft();
            rightDistance = lp.rightMargin + parentView.getPaddingRight();
            maxLeftMargin = parentWidth - rightDistance - viewWidth - leftPadding;
            minTopMargin = lp.topMargin;
            topPadding = parentView.getPaddingTop();
            bottomDistance = lp.bottomMargin + parentView.getPaddingBottom();
            maxTopMargin = parentHeight - bottomDistance - viewHight - topPadding;
        }
    }

    private boolean fling = false;
    GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling .... ");
            fling = true;
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll .... " + distanceX + "," + distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    private GestureDetector gestureDetector = new GestureDetector(simpleOnGestureListener);

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "dispatchTouchEvent ... " + ev.getAction());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setAlpha(1f);
                fling = false;
                countDownTimer.cancel();
                if (moveable) {
                    MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
                    currentX = ev.getRawX();
                    currentY = ev.getRawY();
                    currentLeft = lp.leftMargin;
                    currentTop = lp.topMargin;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (moveable) {
                    currentLeft += ev.getRawX() - currentX;
                    currentTop += ev.getRawY() - currentY;
                    //判断左边界
                    currentLeft = currentLeft < minLeftMargin ? minLeftMargin : currentLeft;
                    //判断右边界
                    currentLeft = (leftPadding + currentLeft + viewWidth + rightDistance) > parentWidth ? maxLeftMargin : currentLeft;
                    //判断上边界
                    currentTop = currentTop < minTopMargin ? minTopMargin : currentTop;
                    //判断下边界
                    currentTop = (topPadding + currentTop + viewHight + bottomDistance) > parentHeight ? maxTopMargin : currentTop;
                    MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
                    lp.leftMargin = currentLeft;
                    lp.topMargin = currentTop;
                    setLayoutParams(lp);
                    currentX = ev.getRawX();
                    currentY = ev.getRawY();
                }
                break;
            case MotionEvent.ACTION_UP:
                countDownTimer.start();
                if (moveable && autoBack) {
                    MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
                    int fromLeftMargin = lp.leftMargin;
                    if (getLeft() < (parentWidth - getLeft() - viewWidth)) {
                        lp.leftMargin = minLeftMargin;
                    } else {
                        lp.leftMargin = maxLeftMargin;
                    }
                    ObjectAnimator marginChange = ObjectAnimator.ofInt(new Wrapper(this), "leftMargin", fromLeftMargin, lp.leftMargin);
                    marginChange.setDuration(500);
                    marginChange.start();
                }
                break;
            default:
        }
        gestureDetector.onTouchEvent(ev);
        if (fling) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
//        return true;
    }

    /**
     * 包装类
     */
    class Wrapper {
        private ViewGroup mTarget;

        public Wrapper(ViewGroup mTarget) {
            this.mTarget = mTarget;
        }

        public int getLeftMargin() {
            MarginLayoutParams lp = (MarginLayoutParams) mTarget.getLayoutParams();
            return lp.leftMargin;
        }

        public void setLeftMargin(int leftMargin) {
            MarginLayoutParams lp = (MarginLayoutParams) mTarget.getLayoutParams();
            lp.leftMargin = leftMargin;
            mTarget.requestLayout();
        }
    }

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            setAlpha(toAlpha);
        }
    }
}
