package com.abhee.android.maps;

import com.google.map.MapPoint;

public class GeoPoint
{

    GeoPoint(com.google.map.MapPoint mp)
    {
        mMapPoint = mp;
    }

    public GeoPoint(int latitudeE6, int longitudeE6)
    {
        if(longitudeE6 == 0xf5456b00)
            longitudeE6 *= -1;
        mMapPoint = new MapPoint(latitudeE6, longitudeE6);
    }

    public int getLatitudeE6()
    {
        return mMapPoint.getLatitude();
    }

    public int getLongitudeE6()
    {
        return mMapPoint.getLongitude();
    }

    public java.lang.String toString()
    {
        return (new StringBuilder()).append(java.lang.String.valueOf(mMapPoint.getLatitude())).append(",").append(java.lang.String.valueOf(mMapPoint.getLongitude())).toString();
    }

    public boolean equals(java.lang.Object object)
    {
        if(this == object)
            return true;
        if(object instanceof com.nader.android.maps.GeoPoint)
        {
            com.nader.android.maps.GeoPoint p = (com.nader.android.maps.GeoPoint)object;
            if(p.getMapPoint().equals(getMapPoint()))
                return true;
        }
        return false;
    }

    public int hashCode()
    {
        return mMapPoint.hashCode();
    }

    com.google.map.MapPoint getMapPoint()
    {
        return mMapPoint;
    }

    private final com.google.map.MapPoint mMapPoint;
}