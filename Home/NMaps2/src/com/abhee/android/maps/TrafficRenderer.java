package com.abhee.android.maps;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import com.google.common.graphics.android.AndroidImage;

class TrafficRenderer extends com.google.googlenav.ui.GmmTileOverlayRendererImpl
{

    public TrafficRenderer(com.google.googlenav.map.TrafficService trafficService)
    {
        super(trafficService, null);
    }

    public co.google.common.graphics.GoogleImage generateNewTileImage(com.google.googlenav.map.MapTile mapTile, com.google.googlenav.map.TrafficTile tt)
    {
        if(tt == null)
        {
            return mapTile.getImage();
        } else
        {
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(128, 128, android.graphics.Bitmap.Config.RGB_565);
            android.graphics.Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(((com.google.common.graphics.android.AndroidImage)mapTile.getImage()).getBitmap(), 0.0F, 0.0F, null);
            drawTraffic(canvas, tt);
            return new AndroidImage(bitmap);
        }
    }

    protected boolean isFast()
    {
        return true;
    }

    private void drawTraffic(android.graphics.Canvas canvas, com.google.googlenav.map.TrafficTile tt)
    {
        com.google.googlenav.map.Tile tile = tt.getLocation();
        int baseX = tile.getXPixelTopLeft();
        int baseY = tile.getYPixelTopLeft();
        com.google.map.Zoom zoom = tile.getZoom();
        int z = zoom.getZoomLevel();
        int bgLineWidth = z <= 13 ? ((int) (z <= 11 ? ((int) (z <= 9 ? 5 : 6)) : 8)) : 9;
        int fgLineWidth = z <= 13 ? ((int) (z <= 11 ? 2 : 3)) : 4;
        android.graphics.Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeJoin(android.graphics.Paint.Join.ROUND);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        com.google.googlenav.map.TrafficReading readings[][] = tt.getReadings();
        if(readings.length == 0 || readings[0].length == 0)
            return;
        android.graphics.Path path = new Path();
        com.google.googlenav.map.TrafficReading lastReading = readings[0][0];
        int lastColor = lastReading.getFillColor();
        for(int i = 0; i < readings.length; i++)
        {
            for(int j = 0; j < readings[i].length; j++)
            {
                com.google.googlenav.map.TrafficReading tr = readings[i][j];
                int color = tr.getFillColor();
                if(color != lastColor)
                {
                    com.nader.android.maps.TrafficRenderer.paintPath(path, canvas, paint, lastColor, bgLineWidth, fgLineWidth);
                    path = new Path();
                    com.nader.android.maps.TrafficRenderer.addPathPoint(path, lastReading, zoom, baseX, baseY, false);
                    lastColor = color;
                }
                com.nader.android.maps.TrafficRenderer.addPathPoint(path, tr, zoom, baseX, baseY, !tr.isDiscontinuity() && j != 0);
                lastReading = tr;
            }

        }

        com.abhee.android.maps.TrafficRenderer.paintPath(path, canvas, paint, lastColor, bgLineWidth, fgLineWidth);
    }

    private static void paintPath(android.graphics.Path path, android.graphics.Canvas canvas, android.graphics.Paint paint, int color, int bgLineWidth, int fgLineWidth)
    {
        paint.setColor(0xd0ffffff);
        paint.setStrokeWidth(bgLineWidth);
        canvas.drawPath(path, paint);
        paint.setColor(color);
        paint.setStrokeWidth(fgLineWidth);
        canvas.drawPath(path, paint);
    }

    private static void addPathPoint(android.graphics.Path path, com.google.googlenav.map.TrafficReading reading, com.google.map.Zoom z, int baseX, int baseY, boolean draw)
    {
        float x = reading.getXOffset();
        float y = reading.getYOffset();
        int separation = z.getZoomLevel() <= 10 ? 4 : 5;
        x = (float)((double)x + ((double)separation * java.lang.Math.cos(((double)reading.getAzi() * 3.1415926535897931D) / 180D)) / 2D);
        y = (float)((double)y + ((double)separation * java.lang.Math.sin(((double)reading.getAzi() * 3.1415926535897931D) / 180D)) / 2D);
        if(draw)
            path.lineTo(x, y);
        else
            path.moveTo(x, y);
    }

	public void setShowTraffic(boolean show) {
		// TODO Auto-generated method stub
		
	}
}