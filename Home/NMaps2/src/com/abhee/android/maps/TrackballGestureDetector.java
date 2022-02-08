package com.abhee.android.maps;
public class TrackballGestureDetector
{

    TrackballGestureDetector(android.os.Handler handler)
    {
        mHandler = handler;
        mOurLongPressRunnable = new java.lang.Runnable() {

            public void run()
            {
                dispatchLongPress();
            }
        }
;
    }

    public void analyze(android.view.MotionEvent ev)
    {
        int action = ev.getAction();
        float y = ev.getY();
        float x = ev.getX();
        mIsScroll = false;
        mIsTap = false;
        mIsDoubleTap = false;
        switch(action)
        {
        default:
            break;

        case 0:
            mLastMotionX = x;
            mLastMotionY = y;
            mFirstDownX = mCurrentDownX;
            mFirstDownY = mCurrentDownY;
            mCurrentDownX = x;
            mCurrentDownY = y;
            mPreviousDownTime = mDownTime;
            mDownTime = ev.getDownTime();
            mAlwaysInTapRegion = true;
            mInLongPress = false;
            mHandler.removeCallbacks(mOurLongPressRunnable);
            mHandler.postAtTime(mOurLongPressRunnable, mDownTime + 1500L);
            break;

        case 2:
            if(mInLongPress)
                break;
            mScrollX = mLastMotionX - x;
            mScrollY = mLastMotionY - y;
            mLastMotionX = x;
            mLastMotionY = y;
            int manhattanTapDistance = (int)(java.lang.Math.abs(x - mCurrentDownX) + java.lang.Math.abs(y - mCurrentDownY));
            if(manhattanTapDistance > 5)
            {
                mAlwaysInTapRegion = false;
                mHandler.removeCallbacks(mOurLongPressRunnable);
            }
            mIsScroll = true;
            break;

        case 1:
            if(mInLongPress)
            {
                mInLongPress = false;
                break;
            }
            if(mAlwaysInTapRegion)
            {
                long eventTime = ev.getEventTime();
                if(eventTime - mPreviousDownTime < 600L)
                    mIsDoubleTap = true;
                else
                if(eventTime - mDownTime < 300L)
                    mIsTap = true;
            }
            mHandler.removeCallbacks(mOurLongPressRunnable);
            break;
        }
    }

    public void registerLongPressCallback(java.lang.Runnable runnable)
    {
        mUserLongPressRunnable = runnable;
    }

    private void dispatchLongPress()
    {
        mInLongPress = true;
        if(mUserLongPressRunnable != null)
            mUserLongPressRunnable.run();
    }

    public boolean isScroll()
    {
        return mIsScroll;
    }

    public float scrollX()
    {
        return mScrollX;
    }

    public float scrollY()
    {
        return mScrollY;
    }

    public boolean isTap()
    {
        return mIsTap;
    }

    public float getCurrentDownX()
    {
        return mCurrentDownX;
    }

    public float getCurrentDownY()
    {
        return mCurrentDownY;
    }

    public boolean isDoubleTap()
    {
        return mIsDoubleTap;
    }

    public float getFirstDownX()
    {
        return mFirstDownX;
    }

    public float getFirstDownY()
    {
        return mFirstDownY;
    }

    private android.os.Handler mHandler;
    private java.lang.Runnable mUserLongPressRunnable;
    private java.lang.Runnable mOurLongPressRunnable;
    private boolean mIsScroll;
    private boolean mIsTap;
    private boolean mIsDoubleTap;
    private float mScrollX;
    private float mScrollY;
    private boolean mAlwaysInTapRegion;
    private boolean mInLongPress;
    private float mCurrentDownX;
    private float mCurrentDownY;
    private float mFirstDownX;
    private float mFirstDownY;
    private long mDownTime;
    private long mPreviousDownTime;
    private float mLastMotionY;
    private float mLastMotionX;

}