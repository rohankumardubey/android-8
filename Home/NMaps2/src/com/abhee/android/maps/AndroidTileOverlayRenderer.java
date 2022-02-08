package com.abhee.android.maps;

class AndroidTileOverlayRenderer
    implements com.google.googlenav.map.TileOverlayRenderer
{

    public AndroidTileOverlayRenderer(com.google.googlenav.map.TrafficService trafficService, com.google.common.DataRequestDispatcher dispatcher)
    {
        mMode = 0;
        mTrafficRenderer = new TrafficRenderer(trafficService);
        mStreetViewRenderer = new StreetViewRenderer(dispatcher);
    }

    public void begin()
    {
        mTrafficRenderer.begin();
        mStreetViewRenderer.begin();
    }

    public boolean renderTile(com.google.googlenav.map.MapTile tile, boolean downloadData)
    {
        if(!tile.isComplete())
            return false;
        switch(mMode)
        {
        case 1:
            return mStreetViewRenderer.renderTile(tile, downloadData);

        case 2:
            return mTrafficRenderer.renderTile(tile, downloadData);

        case 0:
        default:
            return false;
        }
    }

    public void end()
    {
        mStreetViewRenderer.end();
        mTrafficRenderer.end();
    }

    public boolean isShowTraffic()
    {
        return mTrafficRenderer.isShowTraffic();
    }

    public void setShowTraffic(boolean show)
    {
        if(show)
        {
            setShowStreetView(false);
            mMode = 2;
        }
        mTrafficRenderer.setShowTraffic(show);
    }

    public boolean isShowStreetView()
    {
        return mStreetViewRenderer.isEnabled();
    }

    public void setShowStreetView(boolean show)
    {
        if(show)
        {
            setShowTraffic(false);
            mMode = 1;
        }
        mStreetViewRenderer.setEnabled(show);
    }

    private int mMode;
    private final com.abhee.android.maps.TrafficRenderer mTrafficRenderer;
    private final com.abhee.android.maps.StreetViewRenderer mStreetViewRenderer;
}