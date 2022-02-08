package com.abhee.android.maps;
import android.graphics.Canvas;
import com.google.common.graphics.android.AndroidImage;
import com.google.googlenav.map.Tile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

class StreetViewRenderer
    implements com.google.googlenav.map.TileOverlayRenderer
{
    private static class TileRequest
        implements com.google.common.DataRequest
    {

        public byte[] getData()
        {
            return mData;
        }

        public int getRequestType()
        {
            return 26;
        }

        public boolean requestImmediately()
        {
            return mLast;
        }

        public void writeRequestData(java.io.DataOutput dos)
            throws java.io.IOException
        {
            dos.writeShort(1);
            dos.writeShort(1);
            dos.writeShort(128);
            dos.writeLong(4L);
            mTile.write(dos);
        }

        public boolean readResponseData(java.io.DataInput dis)
            throws java.io.IOException
        {
            int responseCode = dis.readUnsignedByte();
            int tileCount = dis.readUnsignedShort();
            com.google.googlenav.map.Tile location = com.google.googlenav.map.Tile.read(dis);
            if(responseCode != 0)
                throw new IOException((new StringBuilder()).append("Server returned: ").append(responseCode).toString());
            if(tileCount != 1)
                throw new IOException((new StringBuilder()).append("Server returned ").append(tileCount).append(" tiles").toString());
            if(!location.equals(mTile))
            {
                throw new IOException((new StringBuilder()).append("Server returned wrong tile: ").append(location).toString());
            } else
            {
                int tileSize = dis.readUnsignedShort();
                byte imageBytes[] = new byte[tileSize];
                dis.readFully(imageBytes);
                mData = imageBytes;
                return true;
            }
        }

        private final com.google.googlenav.map.Tile mTile;
        private byte mData[];
        private boolean mLast;


        public TileRequest(com.google.googlenav.map.Tile tile)
        {
            mData = null;
            mLast = false;
            mTile = tile;
        }
    }


    @SuppressWarnings("unchecked")
	public StreetViewRenderer(com.google.common.DataRequestDispatcher dispatcher)
    {
        mEnabled = false;
        mRequests = new HashMap();
        mDispatcher = dispatcher;
    }

    public void begin()
    {
    }

    public boolean renderTile(com.google.googlenav.map.MapTile tile, boolean downloadData)
    {
        if(mEnabled)
        {
            android.graphics.Bitmap overlay;
            switch(tile.getImageVersion())
            {
            case 1:
                return false;

            default:
                tile.restoreBaseImage();

            case 0:
                overlay = getOverlay(tile, downloadData);
                break;
            }
            if(overlay == null)
                return false;
            else
                return com.nader.android.maps.StreetViewRenderer.generateNewTileImage(tile, overlay);
        }
        if(tile.getImageVersion() == 1)
        {
            tile.restoreBaseImage();
            return true;
        } else
        {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
	public void end()
    {
        if(!mPendingRequests.isEmpty())
        {
            ((TileRequest)mPendingRequests.lastElement()).mLast = true;
            TileRequest request;
            for(java.util.Iterator i = mPendingRequests.iterator(); i.hasNext(); mDispatcher.addDataRequest(request))
                request = (TileRequest)i.next();

            mPendingRequests.clear();
        }
    }

    private static boolean generateNewTileImage(com.google.googlenav.map.MapTile tile, android.graphics.Bitmap overlay)
    {
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(128, 128, android.graphics.Bitmap.Config.RGB_565);
        android.graphics.Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(((com.google.common.graphics.android.AndroidImage)tile.getImage()).getBitmap(), 0.0F, 0.0F, null);
        canvas.drawBitmap(overlay, 0.0F, 0.0F, null);
        tile.setImage(new AndroidImage(bitmap), 1);
        return true;
    }

    @SuppressWarnings("unchecked")
	private android.graphics.Bitmap getOverlay(com.google.googlenav.map.MapTile tile, boolean downloadData)
    {
        com.google.googlenav.map.Tile location = new Tile((byte)9, tile.getLocation());
        TileRequest request = (TileRequest)mRequests.get(location);
        if(request == null)
        {
            if(downloadData)
            {
                request = new TileRequest(location);
                mPendingRequests.add(request);
                mRequests.put(location, request);
            }
            return null;
        }
        byte data[] = request.getData();
        if(data == null)
        {
            return null;
        } else
        {
            android.graphics.Bitmap decoded = android.graphics.BitmapFactory.decodeByteArray(data, 0, data.length);
            mRequests.remove(location);
            return decoded;
        }
    }

    public boolean isEnabled()
    {
        return mEnabled;
    }

    public void setEnabled(boolean show)
    {
        mEnabled = show;
    }

    private final com.google.common.DataRequestDispatcher mDispatcher;
    private boolean mEnabled;
    @SuppressWarnings("unchecked")
	private java.util.Map mRequests;
    @SuppressWarnings("unchecked")
	private final java.util.Vector mPendingRequests = new Vector(18);

}