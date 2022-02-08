package com.abhee.android.maps;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import com.google.map.MapPoint;

public final class MapController
    implements android.view.View.OnKeyListener
{
	static enum VertPanState {UP, DOWN, NONE};
	static enum HorizPanState {LEFT, RIGHT, NONE};

    MapController(com.google.googlenav.map.Map map, com.nader.android.maps.MapView mapView)
    {
        mHorizPan = HorizPanState.NONE;
        mVertPan = VertPanState.NONE;
        mXPanSpeed = 0.0F;
        mYPanSpeed = 0.0F;
        mPanAnimation = null;
        mPanPoint = null;
        mMapView = null;
        mAnimationCompletedMessage = null;
        mAnimationCompletedRunnable = null;
        mHasBeenMeasured = false;
        mDeferredLatSpanE6 = -1;
        mDeferredLonSpanE6 = -1;
        mMap = map;
        mMapView = mapView;
    }

    public void stopPanning()
    {
        mHorizPan = HorizPanState.NONE;
        mVertPan = VertPanState.NONE;
    }

    int[] getDeltas()
    {
        switch(mHorizPan)
        {
        case LEFT:
            mXPanSpeed = curve(mXPanSpeed, -8F);
            break;

        case RIGHT:
            mXPanSpeed = curve(mXPanSpeed, 8F);
            break;

        case NONE:
            mXPanSpeed = 0.0F;
            break;
        }
        switch(mVertPan)
        {
        case UP:
            mYPanSpeed = curve(mYPanSpeed, -8F);
            break;

        case DOWN:
            mYPanSpeed = curve(mYPanSpeed, 8F);
            break;

        case NONE:
            mYPanSpeed = 0.0F;
            break;
        }
        mDeltas[0] = (int)mXPanSpeed;
        mDeltas[1] = (int)mYPanSpeed;
        return mDeltas;
    }

    public boolean onKey(android.view.View v, int keyCode, android.view.KeyEvent event)
    {
        switch(event.getAction())
        {
        case 0:
            if(onKeyDown(keyCode))
            {
                repaint();
                return true;
            } else
            {
                return false;
            }

        case 1:
            if(onKeyUp(keyCode))
            {
                repaint();
                return true;
            } else
            {
                return false;
            }
        }
        throw new IllegalArgumentException((new StringBuilder()).append("Unknown key action: ").append(event.getAction()).toString());
    }

    private boolean onKeyUp(int keyCode)
    {
        switch(keyCode)
        {
        case 19:
            if(mVertPan == VertPanState.UP)
            {
                mVertPan = VertPanState.NONE;
                return true;
            } else
            {
                return false;
            }

        case 20:
            if(mVertPan == VertPanState.DOWN)
            {
                mVertPan = VertPanState.NONE;
                return true;
            } else
            {
                return false;
            }

        case 21:
            if(mHorizPan == HorizPanState.LEFT)
            {
                mHorizPan = HorizPanState.NONE;
                return true;
            } else
            {
                return false;
            }

        case 22:
            if(mHorizPan == HorizPanState.RIGHT)
            {
                mHorizPan = HorizPanState.NONE;
                return true;
            } else
            {
                return false;
            }
        }
        return false;
    }

    private boolean onKeyDown(int keyCode)
    {
        switch(keyCode)
        {
        case 19:
            mVertPan = VertPanState.UP;
            return true;

        case 20:
            mVertPan = VertPanState.DOWN;
            return true;

        case 21:
            mHorizPan = HorizPanState.LEFT;
            return true;

        case 22:
            mHorizPan = HorizPanState.RIGHT;
            return true;
        }
        return false;
    }

    private float curve(float last, float max)
    {
        return last + (max - last) / 8F;
    }

    public void animateTo(com.nader.android.maps.GeoPoint point)
    {
        animateTo(point, null, null);
    }

    public void animateTo(com.nader.android.maps.GeoPoint point, android.os.Message message)
    {
        animateTo(point, null, message);
    }

    public void animateTo(com.nader.android.maps.GeoPoint point, java.lang.Runnable runnable)
    {
        animateTo(point, runnable, null);
    }

    private void animateTo(com.nader.android.maps.GeoPoint point, java.lang.Runnable runnable, android.os.Message message)
    {
        mAnimationCompletedRunnable = runnable;
        mAnimationCompletedMessage = message;
        com.google.map.MapPoint mapPoint = point.getMapPoint();
        stopAnimation(false);
        mMap.preLoad(mapPoint);
        mPanPoint = mapPoint;
        com.google.map.MapPoint center = mMap.getCenterPoint();
        double distance = java.lang.Math.sqrt((int)mapPoint.pixelDistanceSquared(center, mMap.getZoom()));
        int animateMillis = (int)java.lang.Math.min(200D + distance * 10D, 800D);
        mPanAnimation = new TranslateAnimation((float)center.getLatitude() / 1000000F, (float)mapPoint.getLatitude() / 1000000F, (float)center.getLongitude() / 1000000F, (float)mapPoint.getLongitude() / 1000000F);
        mPanAnimation.setDuration(animateMillis);
        mPanAnimation.startNow();
        mPanAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mPanAnimation.initialize(0, 0, 0, 0);
        repaint();
    }

    boolean stepAnimation(long drawTime)
    {
        int panDeltas[] = getDeltas();
        if(panDeltas[0] != 0 || panDeltas[1] != 0)
        {
            scrollBy(panDeltas[0], panDeltas[1]);
            return true;
        }
        if(mPanAnimation != null)
        {
            android.view.animation.Transformation xform = EMPTY_TRANSFORM;
            xform.clear();
            if(mPanAnimation.getTransformation(drawTime, xform))
            {
                mOrigin[0] = 0.0F;
                mOrigin[1] = 0.0F;
                xform.getMatrix().mapPoints(mOrigin);
                com.google.map.MapPoint newCenter = new MapPoint((int)((double)mOrigin[0] * 1000000D), (int)((double)mOrigin[1] * 1000000D));
                centerMapToInternal(newCenter);
                return true;
            }
            centerMapToInternal(mPanPoint);
            mPanPoint = null;
            mPanAnimation = null;
            if(mAnimationCompletedMessage != null)
            {
                mAnimationCompletedMessage.sendToTarget();
                mAnimationCompletedMessage = null;
            }
            if(mAnimationCompletedRunnable != null)
            {
                mMapView.post(mAnimationCompletedRunnable);
                mAnimationCompletedRunnable = null;
            }
            return false;
        } else
        {
            return false;
        }
    }

    public void scrollBy(int x, int y)
    {
        stopAnimation(false);
        com.google.map.MapPoint newCenter = mMap.getCenterPoint().pixelOffset(x, y, mMap.getZoom());
        centerMapToInternal(newCenter);
    }

    void scrollByTrackball(int x, int y)
    {
        if(mPanAnimation != null)
        {
            long delta = android.view.animation.AnimationUtils.currentAnimationTimeMillis() - mPanAnimation.getStartTime();
            if(delta < 250L)
                return;
        }
        scrollBy(x, y);
    }

    void repaint()
    {
        mDirty = true;
        mMapView.postInvalidate();
    }

    public void setCenter(com.nader.android.maps.GeoPoint point)
    {
        centerMapToInternal(point.getMapPoint());
    }

    private void centerMapToInternal(com.google.map.MapPoint mapPoint)
    {
        mMap.setCenterPoint(mapPoint);
        repaint();
    }

    public void stopAnimation(boolean jumpToFinish)
    {
        if(mPanAnimation != null)
        {
            if(jumpToFinish)
                synchronized(mMap)
                {
                    centerMapToInternal(mPanPoint);
                }
            mPanAnimation = null;
            mPanPoint = null;
        }
        mAnimationCompletedMessage = null;
    }

    void zoomTo(com.google.map.Zoom zoom)
    {
        mMap.setZoom(zoom);
        repaint();
    }

    public int setZoom(int zoomLevel)
    {
        zoomLevel = java.lang.Math.min(21, java.lang.Math.max(1, zoomLevel));
        zoomTo(com.google.map.Zoom.getZoom(zoomLevel));
        return zoomLevel;
    }

    public void zoomToSpan(int latSpanE6, int lonSpanE6)
    {
        if(mHasBeenMeasured)
        {
            mMap.zoomToSpan(latSpanE6, lonSpanE6);
            repaint();
        } else
        {
            mDeferredLatSpanE6 = latSpanE6;
            mDeferredLonSpanE6 = lonSpanE6;
        }
    }

    public boolean zoomIn()
    {
        return mMapView.doZoom(true);
    }

    public boolean zoomOut()
    {
        return mMapView.doZoom(false);
    }

    public boolean zoomInFixing(int xPixel, int yPixel)
    {
        return mMapView.doZoom(true, xPixel, yPixel);
    }

    public boolean zoomOutFixing(int xPixel, int yPixel)
    {
        return mMapView.doZoom(false, xPixel, yPixel);
    }

    void onMeasure()
    {
        if(!mHasBeenMeasured)
        {
            mHasBeenMeasured = true;
            if(mDeferredLatSpanE6 >= 0)
                zoomToSpan(mDeferredLatSpanE6, mDeferredLonSpanE6);
        }
    }

    boolean isDirty()
    {
        return mDirty;
    }

    void clean()
    {
        mDirty = false;
    }

    private final com.google.googlenav.map.Map mMap;
    private final float mOrigin[] = {
        0.0F, 0.0F
    };
    private final int mDeltas[] = {
        0, 0
    };
    private HorizPanState mHorizPan;
    private VertPanState mVertPan;
    private float mXPanSpeed;
    private float mYPanSpeed;
    private android.view.animation.Animation mPanAnimation;
    private com.google.map.MapPoint mPanPoint;
    private com.nader.android.maps.MapView mMapView;
    private android.os.Message mAnimationCompletedMessage;
    private java.lang.Runnable mAnimationCompletedRunnable;
    private static final android.view.animation.Transformation EMPTY_TRANSFORM = new Transformation();
    private boolean mHasBeenMeasured;
    private int mDeferredLatSpanE6;
    private int mDeferredLonSpanE6;
    private volatile boolean mDirty;
}