package com.nader.android.maps;

import android.graphics.Point;
import android.graphics.Rect;
import java.util.ArrayList;

public abstract class ItemizedOverlay extends com.nader.android.maps.Overlay
{
    public static interface OnFocusChangeListener
    {

        public abstract void onFocusChanged(com.nader.android.maps.ItemizedOverlay itemizedoverlay, com.nader.android.maps.OverlayItem overlayitem);
    }


    public ItemizedOverlay(android.graphics.drawable.Drawable defaultMarker)
    {
        mRanksOrderedByLat = null;
        mItemsOrderedByRank = null;
        mDrawFocusedItem = true;
        mLastSelectedItemRank = -1;
        mFocused = null;
        mLastFocusedIndex = -1;
        mBalloon = defaultMarker;
    }

    private android.graphics.Rect getTouchableBounds(android.graphics.Rect bounds)
    {
        int w = bounds.width();
        int h = bounds.height();
        if(w >= MINIMUM_TOUCH_DIAMETER && h >= MINIMUM_TOUCH_DIAMETER)
        {
            return bounds;
        } else
        {
            int cx = bounds.centerX();
            int cy = bounds.centerY();
            int touchW = java.lang.Math.max(MINIMUM_TOUCH_DIAMETER, w);
            int touchL = cx - touchW / 2;
            int touchH = java.lang.Math.max(MINIMUM_TOUCH_DIAMETER, h);
            int touchT = cy - touchH / 2;
            mTouchableBounds.set(touchL, touchT, touchL + touchW, touchT + touchH);
            return mTouchableBounds;
        }
    }

    protected static android.graphics.drawable.Drawable boundCenterBottom(android.graphics.drawable.Drawable balloon)
    {
        int width = balloon.getIntrinsicWidth();
        int w2 = width / 2;
        int height = balloon.getIntrinsicHeight();
        balloon.setBounds(-w2, 1 - height, width - w2, 1);
        return balloon;
    }

    protected static android.graphics.drawable.Drawable boundCenter(android.graphics.drawable.Drawable balloon)
    {
        int width = balloon.getIntrinsicWidth();
        int w2 = width / 2;
        int height = balloon.getIntrinsicHeight();
        int h2 = height / 2;
        balloon.setBounds(-w2, -h2, width - w2, height - h2);
        return balloon;
    }

    protected abstract com.nader.android.maps.OverlayItem createItem(int i);

    public abstract int size();

    public com.nader.android.maps.GeoPoint getCenter()
    {
        if(mRanksOrderedByLat.length > 0)
            return getItem(0).getPoint();
        else
            return null;
    }

    protected int getIndexToDraw(int drawingOrder)
    {
        return mRanksOrderedByLat[drawingOrder];
    }

    public void draw(android.graphics.Canvas canvas, com.nader.android.maps.MapView mapView, boolean shadow)
    {
        int size = size();
        int focusedItemRank = -1;
        for(int i = 0; i < size; i++)
        {
            int rank = getIndexToDraw(i);
            int itemState = mItemState[rank];
            if((itemState & 4) == 0)
                drawItem(canvas, mapView, shadow, rank);
            else
                focusedItemRank = rank;
        }

        if(mDrawFocusedItem && focusedItemRank >= 0)
            drawItem(canvas, mapView, shadow, focusedItemRank);
    }

    private void drawItem(android.graphics.Canvas canvas, com.nader.android.maps.MapView mapView, boolean shadow, int rank)
    {
        com.nader.android.maps.OverlayItem item = getItem(rank);
        android.graphics.drawable.Drawable marker = getDrawable(item, rank);
        mapView.getProjection().toPixels(item.getPoint(), mTempPoint);
        com.nader.android.maps.ItemizedOverlay.drawAt(canvas, marker, mTempPoint.x, mTempPoint.y, shadow);
    }

    private android.graphics.drawable.Drawable getDrawable(com.nader.android.maps.OverlayItem item, int rank)
    {
        int itemState = mItemState[rank];
        android.graphics.drawable.Drawable drawable = item.getMarker(itemState);
        if(drawable == null)
        {
            drawable = mBalloon;
            com.nader.android.maps.OverlayItem.setState(drawable, itemState);
        }
        return drawable;
    }

    public int getLatSpanE6()
    {
        return mLatSpanE6;
    }

    public int getLonSpanE6()
    {
        return mLonSpanE6;
    }

    @SuppressWarnings("unchecked")
	protected final void populate()
    {
        int resultCount = size();
        int minLat = 0x55d4a80;
        int maxLat = 0xfaa2b580;
        int minLon = 0xaba9500;
        int maxLon = 0xf5456b00;
        java.util.ArrayList itemsOrderedByRank = new ArrayList(resultCount);
        for(int i = 0; i < resultCount; i++)
        {
            com.nader.android.maps.OverlayItem item = createItem(i);
            itemsOrderedByRank.add(item);
            com.nader.android.maps.GeoPoint point = item.getPoint();
            minLat = java.lang.Math.min(minLat, point.getLatitudeE6());
            maxLat = java.lang.Math.max(maxLat, point.getLatitudeE6());
            minLon = java.lang.Math.min(minLon, point.getLongitudeE6());
            maxLon = java.lang.Math.max(maxLon, point.getLongitudeE6());
        }

        mLatSpanE6 = maxLat - minLat;
        mLonSpanE6 = maxLon - minLon;
        int ranksOrderedByLat[] = new int[resultCount];
        for(int i = 0; i < resultCount; i++)
        {
            int insertRank = i;
            for(int j = 0; j <= i; j++)
            {
                com.nader.android.maps.OverlayItem sorted = (com.nader.android.maps.OverlayItem)itemsOrderedByRank.get(ranksOrderedByLat[j]);
                if(j == i || sorted.getPoint().getLatitudeE6() < ((com.nader.android.maps.OverlayItem)itemsOrderedByRank.get(i)).getPoint().getLatitudeE6())
                {
                    int tmp = ranksOrderedByLat[j];
                    ranksOrderedByLat[j] = insertRank;
                    insertRank = tmp;
                }
            }

        }

        mRanksOrderedByLat = ranksOrderedByLat;
        mItemsOrderedByRank = itemsOrderedByRank;
        mItemState = new int[resultCount];
        mCurrentlySelectedItemRank = -1;
        mCurrentlyPressedItemRank = -1;
        mInGestureMask = 0;
    }

    private void setFocus(int rank, com.nader.android.maps.OverlayItem item)
    {
        boolean notify = mFocused != item && mOnFocusChangeListener != null;
        mLastFocusedIndex = maskHelper(mLastFocusedIndex, rank, 4);
        mFocused = item;
        if(notify)
            mOnFocusChangeListener.onFocusChanged(this, item);
    }

    @SuppressWarnings("unchecked")
	public void setFocus(com.nader.android.maps.OverlayItem item)
    {
        if(item == null)
        {
            setFocus(mLastFocusedIndex, null);
        } else
        {
            int index = 0;
            for(java.util.Iterator i = mItemsOrderedByRank.iterator(); i.hasNext();)
            {
                com.nader.android.maps.OverlayItem candidate = (com.nader.android.maps.OverlayItem)i.next();
                if(candidate == item)
                {
                    setFocus(index, candidate);
                    return;
                }
                index++;
            }

        }
    }

    public com.nader.android.maps.OverlayItem getFocus()
    {
        return mFocused;
    }

    public final int getLastFocusedIndex()
    {
        return mLastFocusedIndex;
    }

    public final com.nader.android.maps.OverlayItem getItem(int position)
    {
        return (com.nader.android.maps.OverlayItem)mItemsOrderedByRank.get(position);
    }

    public com.nader.android.maps.OverlayItem nextFocus(boolean forwards)
    {
        int rank = mLastFocusedIndex + (forwards ? 1 : -1);
        if(rank >= 0 && rank < mRanksOrderedByLat.length)
            return getItem(rank);
        else
            return null;
    }

    public boolean onTap(com.nader.android.maps.GeoPoint p, com.nader.android.maps.MapView mapView)
    {
        mapView.getProjection().toPixels(p, mTempPoint);
        int hit = getItemAtLocation(mTempPoint.x, mTempPoint.y, mapView);
        focus(hit);
        int selectHit = hit;
        select(selectHit);
        if(hit != -1)
            return onTap(hit);
        else
            return false;
    }

    public boolean onTrackballEvent(android.view.MotionEvent event, com.nader.android.maps.MapView mapView)
    {
        return handleMotionEvent(true, 1, event, mapView, mapView.getWidth() / 2, mapView.getHeight() / 2);
    }

    private boolean handleMotionEvent(boolean trackball, int gestureMask, android.view.MotionEvent event, com.nader.android.maps.MapView mapView, int x, int y)
    {
        int action = event.getAction();
        boolean isDown = action == 0;
        boolean isDownOrMove = isDown || action == 2;
        int hit = getItemAtLocation(x, y, mapView);
        boolean hitSomething = hit != -1;
        int selectHit = trackball ? hit : -1;
        select(selectHit);
        if(isDown)
            if(hitSomething)
                mInGestureMask |= gestureMask;
            else
                mInGestureMask &= ~gestureMask;
        boolean inGesture = (mInGestureMask & gestureMask) != 0;
        if(inGesture)
            if(isDownOrMove)
                press(hit);
            else
            if(action == 1)
            {
                press(-1);
                if(hitSomething)
                    onTap(hit);
                mInGestureMask &= ~gestureMask;
            }
        return inGesture;
    }

    private void focus(int hit)
    {
        com.nader.android.maps.OverlayItem hitItem = hit == -1 ? null : getItem(hit);
        setFocus(hit, hitItem);
    }

    private void select(int rank)
    {
        mCurrentlySelectedItemRank = maskHelper(mCurrentlySelectedItemRank, rank, 2);
    }

    private void press(int rank)
    {
        mCurrentlyPressedItemRank = maskHelper(mCurrentlyPressedItemRank, rank, 1);
    }

    private int maskHelper(int oldRank, int newRank, int mask)
    {
        if(oldRank != newRank)
        {
            if(oldRank != -1)
                mItemState[oldRank] &= ~mask;
            if(newRank != -1)
                mItemState[newRank] |= mask;
        }
        return newRank;
    }

    public boolean onTouchEvent(android.view.MotionEvent event, com.nader.android.maps.MapView mapView)
    {
        return handleMotionEvent(false, 2, event, mapView, (int)event.getX(), (int)event.getY());
    }

    @SuppressWarnings("unchecked")
	private int getItemAtLocation(int hitX, int hitY, com.nader.android.maps.MapView mapView)
    {
        java.util.ArrayList hitItems = getItemsAtLocation(hitX, hitY, mapView);
        int closestRank = -1;
        int closestDistanceSquared = 0x7fffffff;
        java.util.Iterator i = hitItems.iterator();
        do
        {
            if(!i.hasNext())
                break;
            int rank = ((java.lang.Integer)i.next()).intValue();
            com.nader.android.maps.OverlayItem item = (com.nader.android.maps.OverlayItem)mItemsOrderedByRank.get(rank);
            mapView.getProjection().toPixels(item.getPoint(), mTempPoint);
            int offsetX = hitX - mTempPoint.x;
            int offsetY = hitY - mTempPoint.y;
            android.graphics.drawable.Drawable marker = getDrawable(item, rank);
            android.graphics.Rect bounds = getTouchableBounds(marker.getBounds());
            int dx = bounds.centerX() - offsetX;
            int dy = bounds.centerY() - offsetY;
            int distanceSquared = dx * dx + dy * dy;
            if(distanceSquared < closestDistanceSquared)
            {
                closestDistanceSquared = distanceSquared;
                closestRank = rank;
            }
        } while(true);
        mLastSelectedItemRank = closestRank;
        return mLastSelectedItemRank;
    }

    @SuppressWarnings("unchecked")
	private java.util.ArrayList getItemsAtLocation(int hitX, int hitY, com.nader.android.maps.MapView mapView)
    {
        java.util.ArrayList itemsByRank = mItemsOrderedByRank;
        int ranksOrderedByLat[] = mRanksOrderedByLat;
        int length = ranksOrderedByLat.length;
        java.util.ArrayList hitItemRanks = new ArrayList(length);
        for(int i = length - 1; i >= 0; i--)
        {
            int rank = ranksOrderedByLat[i];
            int itemState = mItemState[rank];
            if(!mDrawFocusedItem && (itemState & 4) != 0)
                continue;
            com.nader.android.maps.OverlayItem item = (com.nader.android.maps.OverlayItem)itemsByRank.get(rank);
            mapView.getProjection().toPixels(item.getPoint(), mTempPoint);
            int offsetX = hitX - mTempPoint.x;
            int offsetY = hitY - mTempPoint.y;
            android.graphics.drawable.Drawable marker = getDrawable(item, rank);
            if(hitTest(item, marker, offsetX, offsetY))
                hitItemRanks.add(java.lang.Integer.valueOf(rank));
        }

        return hitItemRanks;
    }

    protected boolean hitTest(com.nader.android.maps.OverlayItem item, android.graphics.drawable.Drawable marker, int hitX, int hitY)
    {
        android.graphics.Rect bounds = getTouchableBounds(marker.getBounds());
        return bounds.contains(hitX, hitY);
    }

    public void setOnFocusChangeListener(com.nader.android.maps.ItemizedOverlay.OnFocusChangeListener l)
    {
        mOnFocusChangeListener = l;
    }

    public void setDrawFocusedItem(boolean drawFocusedItem)
    {
        mDrawFocusedItem = drawFocusedItem;
    }

    protected boolean onTap(int index)
    {
        return false;
    }

    private int mRanksOrderedByLat[];
    @SuppressWarnings("unchecked")
	private java.util.ArrayList mItemsOrderedByRank;
    private int mLatSpanE6;
    private int mLonSpanE6;
    private final android.graphics.drawable.Drawable mBalloon;
    private com.nader.android.maps.ItemizedOverlay.OnFocusChangeListener mOnFocusChangeListener;
    private boolean mDrawFocusedItem;
    private int mItemState[];
    private int mLastSelectedItemRank;
    private com.nader.android.maps.OverlayItem mFocused;
    protected int mLastFocusedIndex;
    private int mCurrentlyPressedItemRank;
    private int mCurrentlySelectedItemRank;
    private int mInGestureMask;
    private final android.graphics.Rect mTouchableBounds = new Rect();
    private static final int MINIMUM_TOUCH_DIAMETER = android.view.ViewConfiguration.getTouchSlop() * 4;
    private final android.graphics.Point mTempPoint = new Point();

}