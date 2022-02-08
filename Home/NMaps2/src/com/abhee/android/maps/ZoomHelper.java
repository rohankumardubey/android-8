package com.abhee.android.maps;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

class ZoomHelper
{
    private static class Snapshot
    {

        public android.graphics.Bitmap bitmap;
        public com.nader.android.maps.GeoPoint fixedPoint;
        public com.google.map.Zoom zoom;
        public final float fixedPointCoords[];

        private Snapshot()
        {
            fixedPointCoords = new float[2];
        }

    }


    ZoomHelper(com.nader.android.maps.MapView mapView, com.nader.android.maps.MapController controller)
    {
        mSnapshot = null;
        mFading = false;
        mCommitTime = 0x7fffffffffffffffL;
        mMapView = mapView;
        mController = controller;
        mBitmapPaint.setFilterBitmap(true);
    }

    boolean doZoom(boolean zoomIn, boolean delay, int xOffset, int yOffset)
    {
        com.google.map.Zoom currentZoom = mMapView.getZoom();
        com.google.map.Zoom newZoom = zoomIn ? currentZoom.getNextHigherZoom() : currentZoom.getNextLowerZoom();
        if(newZoom == null || newZoom.getZoomLevel() > mMapView.getMaxZoomLevel())
            return false;
        if(mSnapshot == null)
            createSnapshot();
        com.nader.android.maps.PixelConverter pc = (com.nader.android.maps.PixelConverter)mMapView.getProjection();
        mSnapshot.fixedPoint = pc.fromPixels(xOffset, yOffset);
        mSnapshot.fixedPointCoords[0] = xOffset;
        mSnapshot.fixedPointCoords[1] = yOffset;
        android.graphics.Matrix inverse = new Matrix();
        if(!mLastDrawnScale.getMatrix().invert(inverse))
            android.util.Log.e("ZoomHelper", (new StringBuilder()).append("Singular matrix ").append(mLastDrawnScale.getMatrix()).toString());
        inverse.mapPoints(mSnapshot.fixedPointCoords);
        mController.zoomTo(newZoom);
        if(xOffset != mMapView.getWidth() / 2 || yOffset != mMapView.getHeight() / 2)
        {
            android.graphics.Point realLocationOfFixedPoint = pc.toPixels(mSnapshot.fixedPoint, null, false);
            mController.scrollBy(realLocationOfFixedPoint.x - xOffset, realLocationOfFixedPoint.y - yOffset);
        }
        addScale();
        stepAnimation(android.view.animation.AnimationUtils.currentAnimationTimeMillis(), pc);
        if(delay)
            mCommitTime = android.view.animation.AnimationUtils.currentAnimationTimeMillis() + 600L;
        else
            mMapView.preLoad();
        return true;
    }

    boolean onDraw(android.graphics.Canvas canvas, com.nader.android.maps.MapView mapView, long when)
    {
        if(mSnapshot == null)
            return false;
        com.nader.android.maps.PixelConverter converter = (com.nader.android.maps.PixelConverter)mMapView.getProjection();
        if(!shouldDrawMap(when))
            canvas.drawARGB(255, 255, 255, 255);
        if(when > mCommitTime)
        {
            mMapView.preLoad();
            mCommitTime = 0x7fffffffffffffffL;
        }
        stepAnimation(when, converter);
        mBitmapPaint.setAlpha((int)(255F * mLastDrawnScale.getAlpha()));
        canvas.save();
        canvas.concat(mLastDrawnScale.getMatrix());
        canvas.drawBitmap(mSnapshot.bitmap, 0.0F, 0.0F, mBitmapPaint);
        canvas.restore();
        if(mAnimations.hasEnded())
        {
            if(mFading)
            {
                mAnimations.getAnimations().clear();
                mSnapshot = null;
                mFading = false;
                converter.resetMatrix();
                return false;
            }
            if(mMapView.canCoverCenter())
            {
                mFading = true;
                addFade();
                return true;
            } else
            {
                return false;
            }
        } else
        {
            return true;
        }
    }

    private float getScale(com.google.map.Zoom numerator, com.google.map.Zoom denominator)
    {
        return denominator.isMoreZoomedIn(numerator) ? numerator.getZoomRatio(denominator) : 1.0F / (float)denominator.getZoomRatio(numerator);
    }

    private void addScale()
    {
        float fromFactor = mLastDrawnScale.getMatrix().mapRadius(1.0F);
        float toFactor = getScale(mSnapshot.zoom, mMapView.getZoom());
        android.view.animation.ScaleAnimation scale = new ScaleAnimation(fromFactor, toFactor, fromFactor, toFactor, mSnapshot.fixedPointCoords[0], mSnapshot.fixedPointCoords[1]);
        scale.setFillAfter(true);
        scale.setDuration(500L);
        scale.initialize(0, 0, 0, 0);
        scale.startNow();
        scale.setInterpolator(new LinearInterpolator());
        mFading = false;
        mAnimations.getAnimations().clear();
        mAnimations.addAnimation(scale);
    }

    private void addFade()
    {
        android.view.animation.AlphaAnimation fade = new AlphaAnimation(1.0F, 0.0F);
        fade.setFillAfter(true);
        fade.setDuration(300L);
        fade.initialize(0, 0, 0, 0);
        fade.startNow();
        mAnimations.addAnimation(fade);
    }

    boolean shouldDrawMap(long when)
    {
        return mSnapshot == null || mFading || mAnimations.hasEnded();
    }

    private void createSnapshot()
    {
        Snapshot snapshot = new Snapshot();
        snapshot.bitmap = android.graphics.Bitmap.createBitmap(mMapView.getWidth(), mMapView.getHeight(), android.graphics.Bitmap.Config.RGB_565);
        android.graphics.Canvas canvas = new Canvas(snapshot.bitmap);
        canvas.drawColor((int)android.view.animation.AnimationUtils.currentAnimationTimeMillis());
        mMapView.drawMap(canvas, false);
        snapshot.zoom = mMapView.getZoom();
        mLastDrawnScale.clear();
        mSnapshot = snapshot;
    }

    private void stepAnimation(long when, com.nader.android.maps.PixelConverter converter)
    {
        mAnimations.getTransformation(when, mLastDrawnScale);
        converter.setMatrix(mLastDrawnScale.getMatrix(), getScale(mMapView.getZoom(), mSnapshot.zoom), mSnapshot.fixedPoint, mSnapshot.fixedPointCoords[0], mSnapshot.fixedPointCoords[1]);
    }

    public static final long DELAY_MILLIS = 600L;
    private final com.nader.android.maps.MapView mMapView;
    private final com.nader.android.maps.MapController mController;
    private final android.view.animation.Transformation mLastDrawnScale = new Transformation();
    private final android.view.animation.AnimationSet mAnimations = new AnimationSet(false);
    private final android.graphics.Paint mBitmapPaint = new Paint();
    private Snapshot mSnapshot;
    private boolean mFading;
    private long mCommitTime;
    protected final android.graphics.Point mTempPoint = new Point();
}