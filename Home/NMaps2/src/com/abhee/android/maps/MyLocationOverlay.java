package com.abhee.android.maps;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.LinkedList;

public class MyLocationOverlay extends com.nader.android.maps.Overlay
    implements android.hardware.SensorListener, android.location.LocationListener
{
    private static class NameAndDate
    {

        public java.lang.String name;
        public long date;

        public NameAndDate(java.lang.String name)
        {
            this.name = name;
            date = 0x8000000000000000L;
        }
    }

    @SuppressWarnings("unchecked")
	class LocationTimeComparator
        implements java.util.Comparator
    {

        public int compare(android.location.Location object1, android.location.Location object2)
        {
            long t1 = object1.getTime();
            long t2 = object2.getTime();
            if(t1 < t2)
                return -1;
            return t2 <= t1 ? 0 : 1;
        }

        public int compare(java.lang.Object x0, java.lang.Object x1)
        {
            return compare((android.location.Location)x0, (android.location.Location)x1);
        }

        LocationTimeComparator()
        {
            super();
        }
    }


    public MyLocationOverlay(android.content.Context context, com.nader.android.maps.MapView mapView)
    {
        mIsCompassEnabled = false;
        mOrientation = (0.0F / 0.0F);
        mIsMyLocationEnabled = false;
        mLastFix = null;
        mMyLocation = null;
        mPreviousMyLocation = null;
        mLocationChangedSinceLastDraw = false;
        mIsOnScreen = true;
        mUserActivityReporter = new java.lang.Runnable() {

            public void run()
            {
                mPowerManager.userActivity(android.os.SystemClock.uptimeMillis(), true);
            }
        }
;
        if(mapView == null)
        {
            throw new IllegalArgumentException("mapView == null");
        } else
        {
            mContext = context;
            mMapView = mapView;
            mController = mapView.getController();
            mPowerManager = (android.os.PowerManager)context.getSystemService("power");
            return;
        }
    }

    private android.graphics.drawable.LevelListDrawable getLocationDot()
    {
        if(mLocationDot == null)
        {
            mLocationDot = (android.graphics.drawable.LevelListDrawable)mContext.getResources().getDrawable(0x1080139);
            int w = mLocationDot.getIntrinsicWidth() / 2;
            int h = mLocationDot.getIntrinsicHeight() / 2;
            mLocationDot.setBounds(-w, -h, w, h);
        }
        return mLocationDot;
    }

    private android.graphics.drawable.Drawable getCompassBase()
    {
        if(mCompassBase == null)
        {
            mCompassBase = mContext.getResources().getDrawable(0x1080112);
            int w = mCompassBase.getIntrinsicWidth() / 2;
            int h = mCompassBase.getIntrinsicHeight() / 2;
            mCompassBase.setBounds(-w, -h, w, h);
        }
        return mCompassBase;
    }

    private android.graphics.drawable.Drawable getCompassArrow()
    {
        if(mCompassArrow == null)
        {
            mCompassArrow = mContext.getResources().getDrawable(0x1080111);
            int w = mCompassArrow.getIntrinsicWidth() / 2;
            int h = mCompassArrow.getIntrinsicHeight();
            mCompassArrow.setBounds(-w, -28, w, h - 28);
        }
        return mCompassArrow;
    }

    public synchronized boolean enableCompass()
    {
        if(!mIsCompassEnabled)
        {
            android.hardware.SensorManager sm = (android.hardware.SensorManager)mContext.getSystemService("sensor");
            if(sm != null)
            {
                sm.registerListener(this, 1, 2);
                mIsCompassEnabled = true;
            } else
            {
                android.util.Log.w("Maps.MyLocationOverlay", "Compass SensorManager was unavailable.");
            }
        }
        return mIsCompassEnabled;
    }

    public synchronized void disableCompass()
    {
        android.hardware.SensorManager sm = (android.hardware.SensorManager)mContext.getSystemService("sensor");
        if(sm != null)
            sm.unregisterListener(this, 1);
        mIsCompassEnabled = false;
    }

    private void logFix(java.lang.String s, android.location.Location location1)
    {
    }

    @SuppressWarnings("unchecked")
	public synchronized boolean enableMyLocation()
    {
        android.location.LocationManager service;
        android.location.Location lastKnown;
        long now;
        android.location.Location candidates[];
        int count;
        java.lang.String arr[];
        int len;
        int ic;
        if(mIsMyLocationEnabled)
            return true; //Nader : I think this needs extra check
        service = (android.location.LocationManager)mContext.getSystemService("location");
        lastKnown = null;
        now = java.lang.System.currentTimeMillis();
        candidates = new android.location.Location[DESIRED_PROVIDER_NAMES.length];
        count = 0;
        arr = DESIRED_PROVIDER_NAMES;
        len = arr.length;
        ic = 0;
        while(ic < len)
        {
        	java.lang.String name = arr[ic];
            boolean isProviderEnabled = service.isProviderEnabled(name);
            if(!isProviderEnabled)
                continue;
            mIsMyLocationEnabled = true;
            mEnabledProviders.add(new NameAndDate(name));
            service.requestLocationUpdates(name, 0L, 0.0F, this);
            android.location.Location location = service.getLastKnownLocation(name);
            if(location == null)
                continue;
            try
            {
                long time = location.getTime();
                if(now - time <= 0x927c0L)
                {
                    logFix("Got last-known location from ", location);
                    candidates[count++] = location;
                } else
                {
                    logFix("Ignoring too old last-known location from ", location);
                }
            }
            catch(java.lang.SecurityException e)
            {
                android.util.Log.w("Maps.MyLocationOverlay", (new StringBuilder()).append("Couldn't get provider ").append(name).append(": ").append(e.getMessage()).toString());
            }
            catch(java.lang.IllegalArgumentException e)
            {
                android.util.Log.w("Maps.MyLocationOverlay", (new StringBuilder()).append("Couldn't get provider ").append(name).append(": ").append(e.getMessage()).toString());
            }
            ic++;
        }
        
        java.util.Arrays.sort(candidates, 0, count, new LocationTimeComparator());
        for(int i = 0; i < count; i++)
        {
            android.location.Location loc = candidates[i];
            logFix("Candidate fix from ", loc);
            if(i == 0)
            {
                logFix("Initial best fix from ", loc);
                lastKnown = loc;
                continue;
            }
            if(loc.getAccuracy() < lastKnown.getAccuracy() || loc.getTime() - lastKnown.getTime() > 30000L)
            {
                logFix("Current best fix from ", loc);
                lastKnown = loc;
            }
        }

        if(lastKnown != null)
        {
            logFix("*** Best fix from ", lastKnown);
            onLocationChanged(lastKnown);
        }
        if(!mIsMyLocationEnabled)
            android.util.Log.w("Maps.MyLocationOverlay", "None of the desired Location Providers are available");
        return mIsMyLocationEnabled;
    }

    public synchronized void disableMyLocation()
    {
        if(mIsMyLocationEnabled)
        {
            android.location.LocationManager service = (android.location.LocationManager)mContext.getSystemService("location");
            service.removeUpdates(this);
            mEnabledProviders.clear();
            mIsMyLocationEnabled = false;
        }
    }

    public synchronized void onSensorChanged(int sensor, float values[])
    {
        if(mIsCompassEnabled)
        {
            mOrientation = values[0];
            mMapView.postInvalidate();
        }
    }

    private boolean isLocationOnScreen(com.nader.android.maps.MapView mapView, com.nader.android.maps.GeoPoint location)
    {
        android.graphics.Point tempPoint = new Point();
        mapView.getProjection().toPixels(location, tempPoint);
        android.graphics.Rect screen = new Rect();
        screen.set(0, 0, mapView.getWidth(), mapView.getHeight());
        return screen.contains(tempPoint.x, tempPoint.y);
    }

    @SuppressWarnings("unchecked")
	public synchronized void onLocationChanged(android.location.Location location)
    {
        if(!mIsMyLocationEnabled)
            return;
        long now = location.getTime();
        long then = now - 10000L;
        java.lang.String name = location.getProvider();
        java.util.Iterator i = mEnabledProviders.iterator();
        do
        {
            if(!i.hasNext())
                break;
            NameAndDate provider = (NameAndDate)i.next();
            if(provider.name.equals(name))
            {
                provider.date = now;
                break;
            }
            if(provider.date > then)
            {
                android.util.Log.i("Maps.MyLocationOverlay", "Got fallback update soon after preferred udpate, ignoring");
                return;
            }
        } while(true);
        mLocationChangedSinceLastDraw = true;
        mPreviousMyLocation = mMyLocation;
        mMyLocation = new GeoPoint((int)(location.getLatitude() * 1000000D), (int)(location.getLongitude() * 1000000D));
        mLastFix = location;
        if(isLocationOnScreen(mMapView, mMyLocation))
            mMapView.postInvalidate();
        java.lang.Runnable runnable;
        while((runnable = (java.lang.Runnable)mRunOnFirstFix.poll()) != null) 
        {
            android.util.Log.i("Maps.MyLocationOverlay", (new StringBuilder()).append("Running deferred on first fix: ").append(runnable).toString());
            (new Thread(runnable)).start();
        }
    }

    public void onStatusChanged(java.lang.String s, int i)
    {
    }

    public void onStatusChanged(java.lang.String s, int i, android.os.Bundle bundle)
    {
    }

    public void onProviderEnabled(java.lang.String s)
    {
    }

    public void onProviderDisabled(java.lang.String s)
    {
    }

    public boolean onTap(com.nader.android.maps.GeoPoint p, com.nader.android.maps.MapView map)
    {
        if(mMyLocation != null)
        {
            map.getProjection().toPixels(mMyLocation, mTempPoint);
            long myLocationX = mTempPoint.x;
            long myLocationY = mTempPoint.y;
            map.getProjection().toPixels(p, mTempPoint);
            long dx = java.lang.Math.abs(myLocationX - (long)mTempPoint.x);
            long dy = java.lang.Math.abs(myLocationY - (long)mTempPoint.y);
            float tapRadius = 32F;
            if((float)(dx * dx + dy * dy) < tapRadius * tapRadius)
            {
                dispatchTap();
                return true;
            } else
            {
                return false;
            }
        } else
        {
            return false;
        }
    }

    protected boolean dispatchTap()
    {
        return false;
    }

    public synchronized boolean draw(android.graphics.Canvas canvas, com.nader.android.maps.MapView mapView, boolean shadow, long when)
    {
        if(shadow)
            return false;
        if(mMyLocation != null)
            drawMyLocation(canvas, mapView, mLastFix, mMyLocation, when);
        if(!java.lang.Float.isNaN(mOrientation))
            drawCompass(canvas, mOrientation);
        return false;
    }

    protected void drawMyLocation(android.graphics.Canvas canvas, com.nader.android.maps.MapView mapView, android.location.Location lastFix, com.nader.android.maps.GeoPoint myLocation, long when)
    {
        if(!mIsMyLocationEnabled)
            return;
        com.nader.android.maps.Projection converter = mapView.getProjection();
        converter.toPixels(myLocation, mTempPoint);
        if(lastFix.hasAccuracy())
        {
            float radius = converter.metersToEquatorPixels((int)lastFix.getAccuracy());
            canvas.drawCircle(mTempPoint.x, mTempPoint.y, radius, LOCATION_ACCURACY_FILL_PAINT);
            canvas.drawCircle(mTempPoint.x, mTempPoint.y, radius, LOCATION_ACCURACY_STROKE_PAINT);
        }
        int level = ((int)(when % 1000L) * 10000) / 1000;
        android.graphics.drawable.LevelListDrawable locationDot = getLocationDot();
        locationDot.setLevel(level);
        com.nader.android.maps.MyLocationOverlay.drawAt(canvas, locationDot, mTempPoint.x, mTempPoint.y, false);
        if(mLocationChangedSinceLastDraw && mController != null)
        {
            int width = mapView.getWidth();
            int height = mapView.getHeight();
            mTempRect.set(0, 0, width, height);
            mIsOnScreen = mTempRect.contains(mTempPoint.x, mTempPoint.y);
            mTempRect.inset(width / 20, height / 20);
            boolean isOnScreenInset = mTempRect.contains(mTempPoint.x, mTempPoint.y);
            if(!isOnScreenInset)
            {
                boolean wasOnScreen = false;
                if(mPreviousMyLocation != null)
                {
                    converter.toPixels(mPreviousMyLocation, mTempPoint);
                    wasOnScreen = mTempRect.contains(mTempPoint.x, mTempPoint.y);
                }
                if(wasOnScreen)
                {
                    converter.toPixels(mMyLocation, mTempPoint);
                    mController.animateTo(mMyLocation);
                    mMapView.post(mUserActivityReporter);
                }
            }
        }
        if(mIsOnScreen)
            mMapView.postInvalidateDelayed(250L);
        mLocationChangedSinceLastDraw = false;
    }

    protected void drawCompass(android.graphics.Canvas canvas, float bearing)
    {
        canvas.save();
        canvas.translate(50F, 58F);
        com.nader.android.maps.MyLocationOverlay.drawAt(canvas, getCompassBase(), 0, 0, false);
        canvas.rotate(bearing);
        com.nader.android.maps.MyLocationOverlay.drawAt(canvas, getCompassArrow(), 0, 0, false);
        canvas.restore();
    }

    public com.nader.android.maps.GeoPoint getMyLocation()
    {
        return mMyLocation;
    }

    public android.location.Location getLastFix()
    {
        return mLastFix;
    }

    public float getOrientation()
    {
        return mOrientation;
    }

    public boolean isMyLocationEnabled()
    {
        return mIsMyLocationEnabled;
    }

    @SuppressWarnings("unchecked")
	public synchronized boolean runOnFirstFix(java.lang.Runnable runnable)
    {
        if(mMyLocation != null)
        {
            runnable.run();
            return true;
        } else
        {
            mRunOnFirstFix.offer(runnable);
            return false;
        }
    }


    private static final java.lang.String DESIRED_PROVIDER_NAMES[] = {
        "gps", "network"
    };
    private static final android.graphics.Paint LOCATION_ACCURACY_FILL_PAINT;
    private static final android.graphics.Paint LOCATION_ACCURACY_STROKE_PAINT;
    private final android.content.Context mContext;
    private final com.nader.android.maps.MapController mController;
    private final com.nader.android.maps.MapView mMapView;
    private volatile boolean mIsCompassEnabled;
    private volatile float mOrientation;
    private android.graphics.drawable.Drawable mCompassBase;
    private android.graphics.drawable.Drawable mCompassArrow;
    private volatile boolean mIsMyLocationEnabled;
    private volatile android.location.Location mLastFix;
    private volatile com.nader.android.maps.GeoPoint mMyLocation;
    private volatile com.nader.android.maps.GeoPoint mPreviousMyLocation;
    private volatile boolean mLocationChangedSinceLastDraw;
    private volatile boolean mIsOnScreen;
    @SuppressWarnings("unchecked")
	private final java.util.ArrayList mEnabledProviders = new ArrayList(2);
    private final android.graphics.Point mTempPoint = new Point();
    private final android.graphics.Rect mTempRect = new Rect();
    @SuppressWarnings("unchecked")
	private final java.util.Queue mRunOnFirstFix = new LinkedList();
    private android.graphics.drawable.LevelListDrawable mLocationDot;
    private android.os.PowerManager mPowerManager;
    private java.lang.Runnable mUserActivityReporter;

    static 
    {
        LOCATION_ACCURACY_FILL_PAINT = new Paint();
        LOCATION_ACCURACY_FILL_PAINT.setARGB(30, 0, 0, 255);
        LOCATION_ACCURACY_FILL_PAINT.setStyle(android.graphics.Paint.Style.FILL);
        LOCATION_ACCURACY_STROKE_PAINT = new Paint();
        LOCATION_ACCURACY_STROKE_PAINT.setARGB(100, 0, 0, 255);
        LOCATION_ACCURACY_STROKE_PAINT.setStrokeWidth(2.5F);
        LOCATION_ACCURACY_STROKE_PAINT.setStyle(android.graphics.Paint.Style.STROKE);
        LOCATION_ACCURACY_STROKE_PAINT.setAntiAlias(true);
    }

}