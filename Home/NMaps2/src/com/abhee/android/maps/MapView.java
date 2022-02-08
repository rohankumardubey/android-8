package com.abhee.android.maps;

import android.graphics.Point;
import android.os.Handler;
import android.view.GestureDetector;
import android.widget.Scroller;
import android.widget.ZoomControls;
import com.google.common.graphics.android.AndroidGraphics;

public class MapView extends android.view.ViewGroup
{
	public static final int MapViewS[] = {
        0x1010211
    };
    public static class LayoutParams extends android.view.ViewGroup.LayoutParams
    {

        public java.lang.String debug(java.lang.String output)
        {
            return (new StringBuilder()).append(output).append("MapView.LayoutParams={width=").append(LayoutParams.sizeToString(width)).append(", height=").append(LayoutParams.sizeToString(height)).append(" mode=").append(mode).append(" lat=").append(point.getLatitudeE6()).append(" lng=").append(point.getLongitudeE6()).append(" x= ").append(x).append(" y= ").append(y).append(" alignment=").append(alignment).append("}").toString();
        }

        public static final int MODE_MAP = 0;
        public static final int MODE_VIEW = 1;
        public int mode;
        public com.nader.android.maps.GeoPoint point;
        public int x;
        public int y;
        public int alignment;
        public static final int LEFT = 3;
        public static final int RIGHT = 5;
        public static final int TOP = 48;
        public static final int BOTTOM = 80;
        public static final int CENTER_HORIZONTAL = 1;
        public static final int CENTER_VERTICAL = 16;
        public static final int CENTER = 17;
        public static final int TOP_LEFT = 51;
        public static final int BOTTOM_CENTER = 81;

        public LayoutParams(int width, int height, com.nader.android.maps.GeoPoint point, int alignment)
        {
            this(width, height, point, 0, 0, alignment);
        }

        public LayoutParams(int width, int height, com.nader.android.maps.GeoPoint point, int x, int y, int alignment)
        {
            super(width, height);
            mode = 0;
            this.point = point;
            this.x = x;
            this.y = y;
            this.alignment = alignment;
        }

        public LayoutParams(int width, int height, int x, int y, int alignment)
        {
            super(width, height);
            mode = 1;
            this.x = x;
            this.y = y;
            this.alignment = alignment;
        }

        public LayoutParams(android.content.Context c, android.util.AttributeSet attrs)
        {
            super(c, attrs);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source)
        {
            super(source);
            mode = 1;
            alignment = 51;
        }
    }

    private class Repainter
        implements com.google.common.ui.RepaintListener
    {

        public void repaint()
        {
            postInvalidateDelayed(500L);
        }


        private Repainter()
        {
            super();
        }

    }

    public static enum ReticleDrawMode {DRAW_RETICLE_OVER, DRAW_RETICLE_UNDER, DRAW_RETICLE_NEVER};


    public MapView(android.content.Context context, java.lang.String apiKey)
    {
        this(context, null, 0x101008a, apiKey);
    }

    public MapView(android.content.Context context, android.util.AttributeSet attrs)
    {
        this(context, attrs, 0x101008a);
    }

    public MapView(android.content.Context context, android.util.AttributeSet attrs, int defStyle)
    {
        this(context, attrs, defStyle, null);
    }

    private MapView(android.content.Context context, android.util.AttributeSet attrs, int defStyle, java.lang.String apiKey)
    {
        super(context, attrs, defStyle);
        mDrawer = new AndroidGraphics(null);
        mOverlayBundle = null;
        mReticle = null;
        if(apiKey == null)
        {
            android.content.res.TypedArray a = context.obtainStyledAttributes(attrs, MapViewS);
            mKey = a.getString(0);
            a.recycle();
        } else
        {
            mKey = apiKey;
        }
        if(mKey == null)
            throw new IllegalArgumentException("You need to specify an API Key for each MapView.  See the MapView documentation for details.");
        setWillNotDraw(false);
        if(context instanceof com.nader.android.maps.NMapActivity)
            ((com.nader.android.maps.NMapActivity)context).setupMapView(this);
        else
            throw new IllegalArgumentException("MapViews can only be created inside instances of MapActivity.");
        mScroller = new Scroller(context);
        mGoogleLogo = context.getResources().getDrawable(0x1080161);
        mGoogleLogoWidth = mGoogleLogo.getIntrinsicWidth();
        mGoogleLogoHeight = mGoogleLogo.getIntrinsicHeight();
    }

    void setup(com.google.googlenav.map.Map map, com.google.googlenav.map.TrafficService traffic, com.google.common.DataRequestDispatcher dispatcher)
    {
        mMap = map;
        mConverter = new PixelConverter(map);
        mMap.setRepaintListener(new Repainter());
        mOverlayBundle = new OverlayBundle();
        mController = new MapController(mMap, this);
        mZoomHelper = new ZoomHelper(this, mController);
        mReticle = mContext.getResources().getDrawable(0x10801ab);
        mReticleDrawMode = ReticleDrawMode.DRAW_RETICLE_OVER;
        mOverlayRenderer = new AndroidTileOverlayRenderer(traffic, dispatcher);
        map.setTileOverlayRenderer(mOverlayRenderer, 300L);
        mHandler = new Handler();
        mGestureDetector = new GestureDetector(new android.view.GestureDetector.SimpleOnGestureListener() {

            public boolean onDown(android.view.MotionEvent e)
            {
                if(!mScroller.isFinished())
                    mScroller.abortAnimation();
                displayZoomControls(false);
                return false;
            }

            public boolean onScroll(android.view.MotionEvent e1, android.view.MotionEvent e2, float distanceX, float distanceY)
            {
                displayZoomControls(false);
                mController.scrollBy((int)distanceX, (int)distanceY);
                return true;
            }

            public boolean onSingleTapUp(android.view.MotionEvent e)
            {
                com.nader.android.maps.GeoPoint point = mConverter.fromPixels((int)e.getX(), (int)e.getY());
                return mOverlayBundle.onTap(point, MapView.this);
            }

            public boolean onFling(android.view.MotionEvent e1, android.view.MotionEvent e2, float velocityX, float velocityY)
            {
                mScroller.abortAnimation();
                mLastFlingX = 400;
                mLastFlingY = 400;
                mScroller.fling(mLastFlingX, mLastFlingX, (int)(-velocityX) / 2, (int)(-velocityY) / 2, 0, 800, 0, 800);
                postInvalidate();
                return false;
            }
        }
);
        mGestureDetector.setIsLongpressEnabled(false);
        mTrackballGestureDetector = new TrackballGestureDetector(mHandler);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mMap.resize(w, h);
        if(mReticle != null)
        {
            int left = w / 2 - mReticle.getIntrinsicWidth() / 2;
            int top = h / 2 - mReticle.getIntrinsicHeight() / 2;
            int right = left + mReticle.getIntrinsicWidth();
            int bottom = top + mReticle.getIntrinsicHeight();
            mReticle.setBounds(left, top, right, bottom);
        }
    }

    public void computeScroll()
    {
        if(mScroller.computeScrollOffset())
        {
            int x = mScroller.getCurrX() - mLastFlingX;
            int y = mScroller.getCurrY() - mLastFlingY;
            mLastFlingX = mScroller.getCurrX();
            mLastFlingY = mScroller.getCurrY();
            mController.scrollBy(x, y);
            postInvalidate();
        } else
        {
            super.computeScroll();
        }
    }

    protected final void onDraw(android.graphics.Canvas canvas)
    {
        if(mController.isDirty())
            onLayout(true, 0, 0, 0, 0);
        boolean fetchTiles = true;
        boolean drawAgain = false;
        long drawTime = getDrawingTime();
        fetchTiles = !drawAgain;
        if(mZoomHelper.shouldDrawMap(drawTime))
            drawAgain |= !drawMap(canvas, fetchTiles);
        if(mReticleDrawMode == ReticleDrawMode.DRAW_RETICLE_UNDER && !isInTouchMode())
            mReticle.draw(canvas);
        drawAgain |= mZoomHelper.onDraw(canvas, this, drawTime);
        drawAgain |= mOverlayBundle.draw(canvas, this, drawTime);
        if(mReticleDrawMode == ReticleDrawMode.DRAW_RETICLE_OVER && !isInTouchMode())
            mReticle.draw(canvas);
        mGoogleLogo.draw(canvas);
        drawAgain |= mController.stepAnimation(drawTime);
        if(drawAgain)
        {
            requestLayout();
            invalidate();
        }
    }

    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int maxWidth = mMeasuredWidth;
        int maxHeight = mMeasuredHeight;
        if(maxWidth == 0 || maxHeight == 0)
        {
            android.view.WindowManager wm = (android.view.WindowManager)mContext.getSystemService("window");
            android.view.Display display = wm.getDefaultDisplay();
            if(maxWidth == 0)
                maxWidth = display.getWidth();
            if(maxHeight == 0)
                maxHeight = display.getHeight();
        }
        setMeasuredDimension(com.nader.android.maps.MapView.resolveSize(maxWidth, widthMeasureSpec), com.nader.android.maps.MapView.resolveSize(maxHeight, heightMeasureSpec));
        mGoogleLogo.setBounds(10, mMeasuredHeight - mGoogleLogoHeight - 10, mGoogleLogoWidth + 10, mMeasuredHeight - 10);
        mMap.resize(mMeasuredWidth, mMeasuredHeight);
        mController.onMeasure();
    }

    public void onWindowFocusChanged(boolean hasFocus)
    {
        if(!hasFocus)
            mController.stopPanning();
        super.onWindowFocusChanged(hasFocus);
    }

    public void onFocusChanged(boolean hasFocus, int direction, android.graphics.Rect unused)
    {
        if(!hasFocus)
            mController.stopPanning();
        super.onWindowFocusChanged(hasFocus);
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        if(mOverlayBundle.onKeyDown(keyCode, event, this))
            return true;
        if(mController.onKey(this, keyCode, event))
            return true;
        else
            return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, android.view.KeyEvent event)
    {
        if(mOverlayBundle.onKeyUp(keyCode, event, this))
            return true;
        if(mController.onKey(this, keyCode, event))
            return true;
        else
            return super.onKeyUp(keyCode, event);
    }

    public boolean onTrackballEvent(android.view.MotionEvent event)
    {
        postInvalidate();
        if(mOverlayBundle.onTrackballEvent(event, this))
            return true;
        mTrackballGestureDetector.analyze(event);
        if(mTrackballGestureDetector.isScroll())
        {
            mController.scrollByTrackball((int)(event.getX() * 10F), (int)(event.getY() * 10F));
        } else
        if(mTrackballGestureDetector.isTap())
            mOverlayBundle.onTap(new GeoPoint(mMap.getCenterPoint()), this);
        return false;
    }

    public boolean onTouchEvent(android.view.MotionEvent ev)
    {
        if(!isEnabled() || !isClickable())
            return false;
        postInvalidate();
        if(mOverlayBundle.onTouchEvent(ev, this))
        {
            return true;
        } else
        {
            mGestureDetector.onTouchEvent(ev);
            return true;
        }
    }

    protected LayoutParams generateDefaultLayoutParams()
    {
        return new LayoutParams(-2, -2, new GeoPoint(0, 0), 17);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        updateZoomControls();
        int count = getChildCount();
        android.graphics.Point point = new Point();
        for(int i = 0; i < count; i++)
        {
            android.view.View child = getChildAt(i);
            if(child.getVisibility() == 8)
                continue;
            LayoutParams lp = (LayoutParams)child.getLayoutParams();
            if(lp.mode == 0)
            {
                mConverter.toPixels(lp.point, point);
                point.x += lp.x;
                point.y += lp.y;
            } else
            {
                point.x = lp.x;
                point.y = lp.y;
            }
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int x = point.x;
            int y = point.y;
            int alignment = lp.alignment;
            switch(alignment & 7)
            {
            case 1:
                x -= width / 2;
                break;

            case 5:
                x -= width - 1;
                break;
            }
            switch(alignment & 0x70)
            {
            case 16:
                y -= height / 2;
                break;

            case 80:
                y -= height - 1;
                break;
            }
            int childLeft = mPaddingLeft + x;
            int childTop = mPaddingTop + y;
            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }

        mController.clean();
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(android.util.AttributeSet attrs)
    {
        return new LayoutParams(getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p)
    {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p)
    {
        return new LayoutParams(p);
    }

    public void displayZoomControls(boolean takeFocus)
    {
        if(mZoomControls != null)
        {
            if(mZoomControls.getVisibility() == 8)
                mZoomControls.show();
            if(takeFocus)
                mZoomControls.requestFocus();
            mHandler.removeCallbacks(mZoomControlRunnable);
            mHandler.postDelayed(mZoomControlRunnable, ZOOM_CONTROLS_TIMEOUT);
        }
    }

    private boolean isLocationDisplayed()
    {
        if(mContext instanceof com.nader.android.maps.NMapActivity)
            return ((com.nader.android.maps.NMapActivity)mContext).isLocationDisplayed();
        else
            return false;
    }

    private boolean isRouteDisplayed()
    {
        if(mContext instanceof com.nader.android.maps.NMapActivity)
            return ((com.nader.android.maps.NMapActivity)mContext).isRouteDisplayed();
        else
            return false;
    }

    boolean drawMap(android.graphics.Canvas canvas, boolean fetchTiles)
    {
        mDrawer.setCanvas(canvas);

        try
        {
            return mMap.drawMap(mDrawer, fetchTiles, isLocationDisplayed(), isRouteDisplayed(), true);
        }
        catch(java.lang.IllegalStateException e)
        {
            android.util.Log.e("MapView", "IllegalStateException on drawMap. Wiping cache.", e);
        }
        mMap.eraseAll();
        return true;
    }

    public boolean canCoverCenter()
    {
        return mMap.canCover(mMap.getCenterPoint(), true);
    }

    public void preLoad()
    {
        mMap.preLoad(mMap.getCenterPoint());
    }

    com.google.map.Zoom getZoom()
    {
        return mMap.getZoom();
    }

    public int getZoomLevel()
    {
        return mMap.getZoom().getZoomLevel();
    }

    public void setSatellite(boolean on)
    {
        if(isSatellite() != on)
        {
            mMap.toggleSatellite();
            int maxMapZoom = mMap.getMaxMapZoomForPoint(mMap.getCenterPoint());
            if(mMap.getZoom().getZoomLevel() > maxMapZoom)
                mMap.setZoom(com.google.map.Zoom.getZoom(maxMapZoom));
            updateZoomControls();
            postInvalidate();
        }
    }

    public boolean isSatellite()
    {
        return mMap.isSatellite();
    }

    public void setTraffic(boolean on)
    {
        mOverlayRenderer.setShowTraffic(on);
        postInvalidate();
    }

    public boolean isTraffic()
    {
        return mOverlayRenderer.isShowTraffic();
    }

    public void setStreetView(boolean on)
    {
        mOverlayRenderer.setShowStreetView(on);
        postInvalidate();
    }

    public boolean isStreetView()
    {
        return mOverlayRenderer.isShowStreetView();
    }

    public com.nader.android.maps.GeoPoint getMapCenter()
    {
        return new GeoPoint(mMap.getCenterPoint());
    }

    public com.nader.android.maps.MapController getController()
    {
        return mController;
    }

    @SuppressWarnings("unchecked")
	public final java.util.List getOverlays()
    {
        return mOverlayBundle.getOverlays();
    }

    public int getLatitudeSpan()
    {
        return mMap.getLatitudeSpan();
    }

    public int getLongitudeSpan()
    {
        return mMap.getLongitudeSpan();
    }

    public void setReticleDrawMode(ReticleDrawMode mode)
    {
        if(mode == null)
        {
            throw new NullPointerException("The ReticleDrawMode cannot be null");
        } else
        {
            mReticleDrawMode = mode;
            return;
        }
    }

    public int getMaxZoomLevel()
    {
        return mMap.getMaxMapZoomForPoint(mMap.getCenterPoint());
    }

    public void onSaveInstanceState(android.os.Bundle state)
    {
        state.putInt(KEY_CENTER_LATITUDE, mMap.getCenterPoint().getLatitude());
        state.putInt(KEY_CENTER_LONGITUDE, mMap.getCenterPoint().getLongitude());
        state.putInt(KEY_ZOOM_LEVEL, getZoomLevel());
        if(mZoomControls != null && mZoomControls.getVisibility() == 0)
            state.putInt(KEY_ZOOM_DISPLAYED, 1);
        else
            state.putInt(KEY_ZOOM_DISPLAYED, 0);
    }

    public void onRestoreInstanceState(android.os.Bundle state)
    {
        if(state == null)
            return;
        if(mController != null)
        {
            int invalid = 0x7fffffff;
            int latitude = state.getInt(KEY_CENTER_LATITUDE, invalid);
            int longitude = state.getInt(KEY_CENTER_LONGITUDE, invalid);
            if(latitude != invalid && longitude != invalid)
                mController.setCenter(new GeoPoint(latitude, longitude));
            int zoomLevel = state.getInt(KEY_ZOOM_LEVEL, invalid);
            if(zoomLevel != invalid)
                mController.setZoom(zoomLevel);
        }
        boolean zoomDisplayed = state.getInt(KEY_ZOOM_DISPLAYED, 0) != 0;
        if(zoomDisplayed)
            displayZoomControls(false);
    }

    public android.view.View getZoomControls()
    {
        if(mZoomControls == null)
        {
            mZoomControls = createZoomControls();
            mZoomControls.setVisibility(8);
            mZoomControlRunnable = new java.lang.Runnable() {

                public void run()
                {
                    if(!mZoomControls.hasFocus())
                    {
                        mZoomControls.hide();
                    } else
                    {
                        mHandler.removeCallbacks(mZoomControlRunnable);
                        mHandler.postDelayed(mZoomControlRunnable, com.nader.android.maps.MapView.ZOOM_CONTROLS_TIMEOUT);
                    }
                }
            }
;
        }
        return mZoomControls;
    }

    private android.widget.ZoomControls createZoomControls()
    {
        android.widget.ZoomControls zoomControls = new ZoomControls(mContext);
        zoomControls.setZoomSpeed(2000L);
        zoomControls.setOnZoomInClickListener(new android.view.View.OnClickListener() {

            public void onClick(android.view.View v)
            {
                doZoom(true);
            }
        }
);
        zoomControls.setOnZoomOutClickListener(new android.view.View.OnClickListener() {

            public void onClick(android.view.View v)
            {
                doZoom(false);
            }
        }
);
        return zoomControls;
    }

    boolean doZoom(boolean zoomIn, int xOffset, int yOffset)
    {
        boolean success = false;
        if(zoomIn ? canZoomIn() : canZoomOut())
        {
            mZoomHelper.doZoom(zoomIn, true, xOffset, yOffset);
            success = true;
        }
        updateZoomControls();
        displayZoomControls(false);
        return success;
    }

    boolean doZoom(boolean zoomIn)
    {
        return doZoom(zoomIn, mMeasuredWidth / 2, mMeasuredHeight / 2);
    }

    private void updateZoomControls()
    {
        if(mZoomControls != null)
        {
        	boolean canZoomIn = canZoomIn();
        	boolean canZoomOut = canZoomOut();
            mZoomControls.setIsZoomInEnabled(canZoomIn);
            mZoomControls.setIsZoomOutEnabled(canZoomOut);
        }
    }

    private boolean canZoomOut()
    {
        return mMap.getZoom().getZoomLevel() > 1;
    }

    private boolean canZoomIn()
    {
        return mMap.getZoom().getZoomLevel() < getMaxZoomLevel();
    }

    public com.nader.android.maps.Projection getProjection()
    {
        return mConverter;
    }

    private static final java.lang.String KEY_ZOOM_DISPLAYED = (new StringBuilder()).append(com.nader.android.maps.MapView.class.getName()).append(".zoomDisplayed").toString();
    private static final java.lang.String KEY_CENTER_LATITUDE = (new StringBuilder()).append(com.nader.android.maps.MapView.class.getName()).append(".centerLatitude").toString();
    private static final java.lang.String KEY_CENTER_LONGITUDE = (new StringBuilder()).append(com.nader.android.maps.MapView.class.getName()).append(".centerLongitude").toString();
    private static final java.lang.String KEY_ZOOM_LEVEL = (new StringBuilder()).append(com.nader.android.maps.MapView.class.getName()).append(".zoomLevel").toString();
    private static final long ZOOM_CONTROLS_TIMEOUT = android.view.ViewConfiguration.getZoomControlsTimeout();
    private android.os.Handler mHandler;
    private final com.google.common.graphics.android.AndroidGraphics mDrawer;
    private com.google.googlenav.map.Map mMap;
    private com.nader.android.maps.PixelConverter mConverter;
    private com.nader.android.maps.OverlayBundle mOverlayBundle;
    private com.nader.android.maps.ZoomHelper mZoomHelper;
    private com.nader.android.maps.MapController mController;
    private android.graphics.drawable.Drawable mReticle;
    private com.nader.android.maps.MapView.ReticleDrawMode mReticleDrawMode;
    private final android.widget.Scroller mScroller;
    private int mLastFlingX;
    private int mLastFlingY;
    private com.nader.android.maps.AndroidTileOverlayRenderer mOverlayRenderer;
    private android.view.GestureDetector mGestureDetector;
    private com.nader.android.maps.TrackballGestureDetector mTrackballGestureDetector;
    private android.widget.ZoomControls mZoomControls;
    private java.lang.Runnable mZoomControlRunnable;
    private final android.graphics.drawable.Drawable mGoogleLogo;
    private final int mGoogleLogoHeight;
    private final int mGoogleLogoWidth;
    final java.lang.String mKey;












}