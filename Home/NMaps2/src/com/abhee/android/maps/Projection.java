package com.abhee.android.maps;

public interface Projection
{

    public abstract android.graphics.Point toPixels(com.nader.android.maps.GeoPoint geopoint, android.graphics.Point point);

    public abstract com.nader.android.maps.GeoPoint fromPixels(int i, int j);

    public abstract float metersToEquatorPixels(float f);
}