package com.abhee.android.maps;

import android.graphics.Matrix;
import com.google.common.geom.Point;

final class PixelConverter
    implements com.nader.android.maps.Projection
{

    PixelConverter(com.google.googlenav.map.Map map)
    {
        mMap = map;
        mMatrix.reset();
        mInverse.reset();
    }

    public android.graphics.Point toPixels(com.nader.android.maps.GeoPoint in, android.graphics.Point out)
    {
        return toPixels(in, out, true);
    }

    android.graphics.Point toPixels(com.nader.android.maps.GeoPoint in, android.graphics.Point out, boolean transform)
    {
        if(out == null)
            out = new android.graphics.Point();
        
        mMap.getPointXY(in.getMapPoint(), mTempPoint);
        if(transform)
            transformTempPoint();
        out.x = mTempPoint.x;
        out.y = mTempPoint.y;
        return out;
    }

    public com.nader.android.maps.GeoPoint fromPixels(int x, int y)
    {
        synchronized(mTempPoint)
        {
            mTempFloats[0] = x;
            mTempFloats[1] = y;
            mInverse.mapPoints(mTempFloats);
            x = (int)mTempFloats[0];
            y = (int)mTempFloats[1];
        }
        com.google.map.MapPoint centerPoint = mMap.getCenterPoint();
        com.google.common.geom.Point centerXY = mMap.getPointXY(centerPoint);
        int dx = x - centerXY.x;
        int dy = y - centerXY.y;
        com.google.map.MapPoint tapPoint = centerPoint.pixelOffset(dx, dy, mMap.getZoom());
        com.nader.android.maps.GeoPoint point = new GeoPoint(tapPoint);
        return point;
    }

    public float metersToEquatorPixels(float meters)
    {
        return mMatrix.mapRadius(mMap.getZoom().getPixelsForDistance((int)meters));
    }

    private void transformTempPoint()
    {
        mTempFloats[0] = mTempPoint.x;
        mTempFloats[1] = mTempPoint.y;
        mMatrix.mapPoints(mTempFloats);
        mTempPoint.x = (int)mTempFloats[0];
        mTempPoint.y = (int)mTempFloats[1];
    }

    void setMatrix(android.graphics.Matrix animationState, float scale, com.nader.android.maps.GeoPoint fixed, float fixedX, float fixedY)
    {
        mMatrix.reset();
        float correctionX;
        float correctionY;
        synchronized(mTempPoint)
        {
            mMap.getPointXY(fixed.getMapPoint(), mTempPoint);
            mMatrix.postTranslate(-mTempPoint.x, -mTempPoint.y);
            mMatrix.postScale(scale, scale);
            mMatrix.postTranslate(fixedX, fixedY);
            correctionX = (float)mTempPoint.x - fixedX;
            correctionY = (float)mTempPoint.y - fixedY;
        }
        animationState.postTranslate(correctionX, correctionY);
        mMatrix.postConcat(animationState);
        if(!mMatrix.invert(mInverse))
            android.util.Log.e("PixelConverter", (new StringBuilder()).append("Setting singular matrix ").append(mMatrix).toString());
    }

    void resetMatrix()
    {
        mMatrix.reset();
        mInverse.reset();
    }

    private final com.google.googlenav.map.Map mMap;
    private final android.graphics.Matrix mMatrix = new Matrix();
    private final android.graphics.Matrix mInverse = new Matrix();
    private final float mTempFloats[] = new float[2];
    private final com.google.common.geom.Point mTempPoint = new Point();
}