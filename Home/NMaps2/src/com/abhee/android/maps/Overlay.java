package com.abhee.android.maps;

public abstract class Overlay
{

    public Overlay()
    {
    }

    protected static void drawAt(android.graphics.Canvas canvas, android.graphics.drawable.Drawable drawable, int x, int y, boolean shadow)
    {
        if(x > 16000 || y > 16000 || x < -16000 || y < -16000)
            return;
        if(shadow)
            drawable.setColorFilter(0x7f000000, android.graphics.PorterDuff.Mode.SRC_IN);
        canvas.save();
        canvas.translate(x, y);
        if(shadow)
        {
            canvas.skew(-0.9F, 0.0F);
            canvas.scale(1.0F, 0.5F);
        }
        drawable.draw(canvas);
        if(shadow)
            drawable.clearColorFilter();
        canvas.restore();
    }

    public boolean onTouchEvent(android.view.MotionEvent e, com.nader.android.maps.MapView mapView)
    {
        return false;
    }

    public boolean onTrackballEvent(android.view.MotionEvent event, com.nader.android.maps.MapView mapView)
    {
        return false;
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event, com.nader.android.maps.MapView mapView)
    {
        return false;
    }

    public boolean onKeyUp(int keyCode, android.view.KeyEvent event, com.nader.android.maps.MapView mapView)
    {
        return false;
    }

    public boolean onTap(com.nader.android.maps.GeoPoint p, com.nader.android.maps.MapView mapView)
    {
        return false;
    }

    public void draw(android.graphics.Canvas canvas1, com.nader.android.maps.MapView mapview, boolean flag)
    {
    }

    public boolean draw(android.graphics.Canvas canvas, com.nader.android.maps.MapView mapView, boolean shadow, long when)
    {
        draw(canvas, mapView, shadow);
        return false;
    }

    protected static final float SHADOW_X_SKEW = -0.9F;
    protected static final float SHADOW_Y_SCALE = 0.5F;
}