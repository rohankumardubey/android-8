package com.abhee.android.maps;
public class OverlayItem
{

    public OverlayItem(com.nader.android.maps.GeoPoint point, java.lang.String title, java.lang.String snippet)
    {
        mMarker = null;
        mPoint = point;
        mTitle = title;
        mSnippet = snippet;
    }

    public void setMarker(android.graphics.drawable.Drawable marker)
    {
        mMarker = marker;
    }

    public android.graphics.drawable.Drawable getMarker(int stateBitset)
    {
        if(mMarker != null)
            com.nader.android.maps.OverlayItem.setState(mMarker, stateBitset);
        return mMarker;
    }

    public static void setState(android.graphics.drawable.Drawable drawable, int stateBitset)
    {
        drawable.setState(ITEM_STATE_TO_STATE_SET[stateBitset]);
    }

    public java.lang.String getTitle()
    {
        return mTitle;
    }

    public java.lang.String getSnippet()
    {
        return mSnippet;
    }

    public com.nader.android.maps.GeoPoint getPoint()
    {
        return mPoint;
    }

    public java.lang.String routableAddress()
    {
        java.lang.StringBuilder sb = new StringBuilder();
        sb.append((float)mPoint.getLatitudeE6() / 1000000F);
        sb.append(", ");
        sb.append((float)mPoint.getLongitudeE6() / 1000000F);
        return sb.toString();
    }

    protected final com.nader.android.maps.GeoPoint mPoint;
    protected final java.lang.String mTitle;
    protected final java.lang.String mSnippet;
    protected android.graphics.drawable.Drawable mMarker;
    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_SELECTED_MASK = 2;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    private static final int ITEM_STATE_TO_STATE_SET[][] = {
        {
            0xfefeff64, 0xfefeff5f, 0xfefeff59
        }, {
            0xfefeff64, 0xfefeff5f, 0x10100a7
        }, {
            0xfefeff64, 0x10100a1, 0xfefeff59
        }, {
            0xfefeff64, 0x10100a1, 0x10100a7
        }, {
            0x101009c, 0xfefeff5f, 0xfefeff59
        }, {
            0x101009c, 0xfefeff5f, 0x10100a7
        }, {
            0x101009c, 0x10100a1, 0xfefeff59
        }, {
            0x101009c, 0x10100a1, 0x10100a7
        }
    };

}