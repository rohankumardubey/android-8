package com.abhee.android.maps;

import java.util.ArrayList;

final class OverlayBundle
{

    OverlayBundle()
    {
    }

    @SuppressWarnings("unchecked")
	boolean draw(android.graphics.Canvas canvas, com.nader.android.maps.MapView mapView, long drawTime)
    {
        boolean again = false;
        for(java.util.Iterator i = mOverlays.iterator(); i.hasNext();)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)i.next();
            again |= overlay.draw(canvas, mapView, true, drawTime);
        }

        for(java.util.Iterator i = mOverlays.iterator(); i.hasNext();)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)i.next();
            again |= overlay.draw(canvas, mapView, false, drawTime);
        }

        return again;
    }

    boolean onTouchEvent(android.view.MotionEvent e, com.nader.android.maps.MapView mapView)
    {
        int size = mOverlays.size();
        for(int i = size - 1; i >= 0; i--)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)mOverlays.get(i);
            if(overlay.onTouchEvent(e, mapView))
                return true;
        }

        return false;
    }

    boolean onTap(com.nader.android.maps.GeoPoint p, com.nader.android.maps.MapView mapView)
    {
        int size = mOverlays.size();
        for(int i = size - 1; i >= 0; i--)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)mOverlays.get(i);
            if(overlay.onTap(p, mapView))
                return true;
        }

        return false;
    }

    boolean onKeyDown(int keyCode, android.view.KeyEvent event, com.nader.android.maps.MapView mapView)
    {
        int size = mOverlays.size();
        for(int i = size - 1; i >= 0; i--)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)mOverlays.get(i);
            if(overlay.onKeyDown(keyCode, event, mapView))
                return true;
        }

        return false;
    }

    boolean onKeyUp(int keyCode, android.view.KeyEvent event, com.nader.android.maps.MapView mapView)
    {
        int size = mOverlays.size();
        for(int i = size - 1; i >= 0; i--)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)mOverlays.get(i);
            if(overlay.onKeyUp(keyCode, event, mapView))
                return true;
        }

        return false;
    }

    boolean onTrackballEvent(android.view.MotionEvent event, com.nader.android.maps.MapView mapView)
    {
        int size = mOverlays.size();
        for(int i = size - 1; i >= 0; i--)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)mOverlays.get(i);
            if(overlay.onTrackballEvent(event, mapView))
                return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
	public java.util.List getOverlays()
    {
        return mOverlays;
    }

    @SuppressWarnings("unchecked")
	private final java.util.List mOverlays = java.util.Collections.synchronizedList(new ArrayList());
}