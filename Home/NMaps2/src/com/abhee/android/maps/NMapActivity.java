package com.abhee.android.maps;
import android.net.NetworkConnectivityListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.common.android.AndroidConfig;
import com.google.googlenav.map.Map;
import com.google.googlenav.map.TrafficService;
import com.google.map.MapPoint;
import java.lang.ref.WeakReference;

public class NMapActivity extends android.app.Activity
{

    public NMapActivity()
    {
        mMap = null;
    }

    void setupMapView(com.nader.android.maps.MapView mapView)
    {
        if(mMapView != null)
        {
            throw new IllegalStateException("You are only allowed to have a single MapView in a MapActivity");
        } else
        {
            mDataRequestDispatcher.setProperty("key", mapView.mKey);
            mMapView = mapView;
            mMapView.setup(mMap, mTrafficService, mDataRequestDispatcher);
            return;
        }
    }

    protected void onCreate(android.os.Bundle icicle)
    {
        super.onCreate(icicle);
        mNetworkWatcher = new NetworkConnectivityListener();
        mNetworkWatcher.registerHandler(mNetworkHandler, 0);
        mConfig = new AndroidConfig(this);
        com.google.common.Config.setConfig(mConfig);
        com.google.common.graphics.android.AndroidImage.mapResource("/loading_tile.png", 0x108015f);
        com.google.common.graphics.android.AndroidImage.mapResource("/blank_tile.png", 0x10800a2);
        com.google.common.graphics.android.AndroidImage.mapResource("/no_tile_128.png", 0x108016f);
        createMap();
        
        LinearLayout zoomLayout = new LinearLayout(this);
        
        mMapView = new MapView(this, "nadermaps");
        mMapView.setClickable(true);
        mMapView.setEnabled(true);

        mMapView.addView(zoomLayout, new ViewGroup.LayoutParams(com.nader.android.maps.MapView.LayoutParams.FILL_PARENT,
        		com.nader.android.maps.MapView.LayoutParams.WRAP_CONTENT));
        zoomLayout.addView(mMapView.getZoomControls(),
                new ViewGroup.LayoutParams(com.nader.android.maps.MapView.LayoutParams.WRAP_CONTENT,
                		com.nader.android.maps.MapView.LayoutParams.WRAP_CONTENT));

        setContentView(mMapView);
    }

    protected void onResume()
    {
        super.onResume();
        mTrafficService.start();
        mDataRequestDispatcher.start();
        mNetworkWatcher.startListening(this);
        mMap.resume();
    }

    protected void onPause()
    {
        super.onPause();
        if(sActivityReference.get() == this)
        {
            mTrafficService.stop();
            mMap.pause();
            mDataRequestDispatcher.stop();
            mNetworkWatcher.stopListening();
            mMap.saveState();
            mConfig.getPersistentStore().savePreferences();
        } else
        {
            android.util.Log.d("MapActivity", (new StringBuilder()).append("onPause leaving the lights on for ").append(sActivityReference.get()).toString());
        }
    }

    protected void onDestroy()
    {
        super.onDestroy();
        if(sActivityReference.get() == this)
        {
            mNetworkWatcher.unregisterHandler(mNetworkHandler);
            mNetworkWatcher = null;
            mTrafficService.close();
            mMap.close(false);
        } else
        {
            android.util.Log.d("MapActivity", (new StringBuilder()).append("onDestroy leaving the lights on for ").append(sActivityReference.get()).toString());
        }
        mConfig.getConnectionFactory().close();
    }

    @SuppressWarnings("unchecked")
	private void createMap()
    {
        mDataRequestDispatcher = com.google.common.DataRequestDispatcher.getInstance();
        if(mDataRequestDispatcher != null)
        {
            android.util.Log.w("MapActivity", (new StringBuilder()).append("Replacing old dispatcher ").append(mDataRequestDispatcher).toString());
            com.google.common.DataRequestDispatcher.clearInstance();
        }
        java.lang.String serverUrl = "http://www.google.com/glm/mmap/a";
        java.lang.String version = "1.0.0";
        java.lang.String platformId = (new StringBuilder("android:")).append(android.os.SystemProperties.get("ro.product.manufacturer", "unknown").replace('-', '_')).append("-").append(android.os.SystemProperties.get("ro.product.device", "unknown").replace('-', '_')).append("-").append(android.os.SystemProperties.get("ro.product.model", "unknown").replace('-', '_')).append("-").append(getClass().getName()).toString();
        java.lang.String distChannel = android.os.SystemProperties.get("ro.com.google.clientid", "android:unknown");
        mDataRequestDispatcher = com.google.common.DataRequestDispatcher.createInstance(serverUrl, platformId, version, distChannel);
        java.lang.String signature = com.nader.android.maps.KeyHelper.getSignatureFingerprint(super.getPackageManager(), super.getPackageName());
        mDataRequestDispatcher.setProperty("sig", signature);
        com.google.map.MapPoint startPoint = null;
        com.google.map.Zoom zoom = null;
        int startingLatLng[] = getResources().getIntArray(0x1070006);
        startPoint = new MapPoint(startingLatLng[0], startingLatLng[1]);
        int startingZoom[] = getResources().getIntArray(0x1070007);
        zoom = com.google.map.Zoom.getZoom(startingZoom[0]);
        mMap = (com.google.googlenav.map.Map)sMapReference.get();
        if(mMap == null)
            mMap = new Map(null, 0x493e0, 0x30d40, 0x64000, startPoint, zoom, 10);
        else
            android.util.Log.v("MapActivity", "Recycling map object.");
        sMapReference = new WeakReference(mMap);
        sActivityReference = new WeakReference(this);
        mTrafficService = new TrafficService(0x1d4c0);
    }

    protected boolean isRouteDisplayed() {
    	return true;
    }

    @SuppressWarnings("unchecked")
	protected boolean isLocationDisplayed()
    {
        if(mMapView == null)
            return false;
        java.util.List overlays = mMapView.getOverlays();
        for(java.util.Iterator i = overlays.iterator(); i.hasNext();)
        {
            com.nader.android.maps.Overlay overlay = (com.nader.android.maps.Overlay)i.next();
            if((overlay instanceof com.nader.android.maps.MyLocationOverlay) && ((com.nader.android.maps.MyLocationOverlay)overlay).isMyLocationEnabled())
                return true;
        }
        return false;
    }

    private com.google.googlenav.map.TrafficService mTrafficService;
    private com.google.googlenav.map.Map mMap;
    private com.nader.android.maps.MapView mMapView;
    private com.google.common.DataRequestDispatcher mDataRequestDispatcher;
    private com.google.common.android.AndroidConfig mConfig;
    private android.net.NetworkConnectivityListener mNetworkWatcher;
    @SuppressWarnings("unchecked")
	private static volatile java.lang.ref.WeakReference sMapReference = new WeakReference(null);
    @SuppressWarnings("unchecked")
	private static volatile java.lang.ref.WeakReference sActivityReference = new WeakReference(null);
    private final android.os.Handler mNetworkHandler = new android.os.Handler() {

        public void handleMessage(android.os.Message message)
        {
            if(mNetworkWatcher == null)
                return;
            android.net.NetworkConnectivityListener.State state = mNetworkWatcher.getState();
            android.util.Log.i("MapActivity", (new StringBuilder()).append("Handling network change notification:").append(state.toString()).toString());
            if(mDataRequestDispatcher != null)
                if(state != android.net.NetworkConnectivityListener.State.CONNECTED)
                    mDataRequestDispatcher.stop();
                else
                    mDataRequestDispatcher.start();
            com.google.common.io.android.AndroidHttpConnectionFactory factory;
            factory = mConfig.getConnectionFactory();
            if(factory == null)
            {
                android.util.Log.e("MapActivity", "Couldn't get connection factory");
                return;
            }
            com.google.android.net.GoogleHttpClient client;
            client = factory.getClient();
            if(client == null)
            {
                android.util.Log.e("MapActivity", "Couldn't get connection factory client");
                return;
            }
            org.apache.http.conn.ClientConnectionManager manager;
            manager = client.getConnectionManager();
            if(manager == null)
            {
                android.util.Log.e("MapActivity", "Couldn't get client connection manager");
                return;
            }
            try
            {
                manager.closeIdleConnections(1L, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
            catch(java.lang.Exception e)
            {
                android.util.Log.e("MapActivity", "Couldn't reset connection pool.", e);
            }
            
            return;
        }
    };
    
    private static final int MENU_ITEM_CLEAR_MAP = 10000;
    private static final int MENU_ITEM_MAP_MODE = 10001;
    private static final int MENU_ITEM_MAP_MODE_SUB_MAP = 10002;
    private static final int MENU_ITEM_MAP_MODE_SUB_SATELLITE = 10003;
    private static final int MENU_ITEM_MAP_MODE_SUB_TRAFFIC = 10004;
    private static final int MENU_ITEM_ZOOM = 10005;
    
    public boolean onCreateOptionsMenu(Menu pMenu) {
        super.onCreateOptionsMenu(pMenu);
        
        MenuItem menuItem = null;
        SubMenu subMenu = null;
        
        menuItem = pMenu.add(Menu.NONE, MENU_ITEM_CLEAR_MAP,
            Menu.CATEGORY_SECONDARY, "Clear map");
        menuItem.setAlphabeticShortcut('c');
        menuItem.setIcon(android.R.drawable.ic_menu_revert);
        
        subMenu = pMenu.addSubMenu(Menu.NONE, MENU_ITEM_MAP_MODE,
            Menu.CATEGORY_SECONDARY, "Map mode");
        subMenu.setIcon(android.R.drawable.ic_menu_mapmode);
        
        menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_MAP, Menu.NONE,
            "Map");
        menuItem.setAlphabeticShortcut('m');
        menuItem.setCheckable(true);
        menuItem.setChecked(false);
        
        menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_SATELLITE,
            Menu.NONE, "Satellite");
        menuItem.setAlphabeticShortcut('s');
        menuItem.setCheckable(true);
        menuItem.setChecked(false);
        
        menuItem = subMenu.add(0, MENU_ITEM_MAP_MODE_SUB_TRAFFIC, Menu.NONE,
            "Traffic");
        menuItem.setAlphabeticShortcut('t');
        menuItem.setCheckable(true);
        menuItem.setChecked(false);
        
        menuItem = pMenu.add(0, MENU_ITEM_ZOOM, Menu.CATEGORY_SECONDARY,
            "Zoom");
        menuItem.setAlphabeticShortcut('z');
        menuItem.setIcon(android.R.drawable.ic_menu_zoom);
        
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem pItem) {
        switch (pItem.getItemId()) {
            case MENU_ITEM_CLEAR_MAP:
                mMapView.setSatellite(false);
                mMapView.setTraffic(false);
                
                return true;
                
            case MENU_ITEM_MAP_MODE_SUB_MAP:
                mMapView.setSatellite(false);
                
                return true;
                
            case MENU_ITEM_MAP_MODE_SUB_SATELLITE:
                mMapView.setSatellite(true);
                
                return true;
                
            case MENU_ITEM_MAP_MODE_SUB_TRAFFIC:
                mMapView.setTraffic(!mMapView.isTraffic());
                
                return true;
                
            case MENU_ITEM_ZOOM:
                mMapView.displayZoomControls(true);
                
                return true;
        }
        
        return super.onOptionsItemSelected(pItem);
    }
    
    public boolean onPrepareOptionsMenu(Menu pMenu) {
        super.onPrepareOptionsMenu(pMenu);
        
        boolean isSatellite = mMapView.isSatellite();
        boolean isTraffic = mMapView.isTraffic();
        
        pMenu.findItem(MENU_ITEM_CLEAR_MAP)
            .setEnabled(isSatellite || isTraffic);
        pMenu.findItem(MENU_ITEM_MAP_MODE_SUB_MAP)
            .setChecked(!isSatellite).setEnabled(isSatellite);
        pMenu.findItem(MENU_ITEM_MAP_MODE_SUB_SATELLITE)
            .setChecked(isSatellite).setEnabled(!isSatellite);
        pMenu.findItem(MENU_ITEM_MAP_MODE_SUB_TRAFFIC).setChecked(isTraffic);
        
        return true;
    }
}