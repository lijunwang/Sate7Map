package com.sate7.geo.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.sate7.geo.map.bean.Sate7Fence;
import com.sate7.geo.map.db.FenceDB;
import com.sate7.geo.map.util.BitmapGetter;
import com.sate7.geo.map.util.GPS;
import com.sate7.geo.map.util.NotificationHelper;
import com.sate7.geo.map.util.SpHelper;
import com.sate7.geo.map.util.XLog;
import com.sate7.geo.map.view.DragableLayout;
import com.sate7.geo.map.view.SlideFromBottomPopup;
import com.sate7.geo.map.view.SlideFromBottomPopupPolyline;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.Screen;
import com.yhao.floatwindow.ViewStateListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.reactivex.functions.Consumer;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, BaiduMap.OnMarkerClickListener, BaiduMap.OnPolylineClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private MapView mapView;
    private BaiduMap mBaiduMap;
    private ArrayList<LatLng> mTraceData = new ArrayList();
    private FenceDB mFenceDB;
    private final boolean mHintDB = true;
    private RxPermissions mRxPermissions;
    private LocationClient mLocationClient;
    private LocationClientOption mLocationClientOption;
    private MyLocationListener mLocationListener;
    private MyLocationConfiguration mLocationConfiguration;
    private FloatingActionButton mFloatingAddFence;
    private FloatingActionButton mFloatingQueryFence;
    private FloatingActionButton mFloatingRecordTrack;
    private FloatingActionButton mFloatingQueryTrack;
    private FloatingActionButton mFloatingDownloadMap;
    private FloatingActionsMenu mFloatingActionsMenu;
    private TextView mDebugInfo;
    private ImageView mSaveTrack;
    private DragableLayout mSaveTrackContainer;
    private ImageView mMyLocation;
    private final int MSG_CONTINUE_CENTER = 0x112;
    private final int MSG_CONTINUE_CENTER_CREATE_FENCE_AUTO = 0x113;
    private final int MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL = 0x114;
    private final int MSG_CONTINUE_CENTER_VIEW_FENCE = 0x115;
    private final int MSG_CONTINUE_CENTER_EDIT_FENCE = 0x116;
    private final int MSG_CONTINUE_CENTER_VIEW_TRACK = 0x117;
    private int CONTINUE_CENTER_CREATE_FENCE_DELAY_AUTO;
    private int CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL;
    private int CONTINUE_CENTER_VIEW_FENCE_DELAY;
    private int CONTINUE_CENTER_EDIT_FENCE_DELAY;
    private int CONTINUE_CENTER_VIEW_TRACK_DELAY;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONTINUE_CENTER:
                    mStopCenter = false;
                    break;
                case MSG_CONTINUE_CENTER_CREATE_FENCE_AUTO:
                    mStopCenterForCreateFenceAuto = false;
                    break;
                case MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL:
                    mStopCenterForCreateFenceManual = false;
                    break;
                case MSG_CONTINUE_CENTER_VIEW_FENCE:
                    mStopCenterForViewFence = false;
                    break;
                case MSG_CONTINUE_CENTER_EDIT_FENCE:
                    mStopCenterForEditFence = false;
                    break;
                case MSG_CONTINUE_CENTER_VIEW_TRACK:
                    mStopCenterForViewTrack = false;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        CONTINUE_CENTER_CREATE_FENCE_DELAY_AUTO = getResources().getInteger(R.integer.create_fence_time_limit_auto);
        CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL = getResources().getInteger(R.integer.create_fence_time_limit_manual);
        CONTINUE_CENTER_VIEW_FENCE_DELAY = getResources().getInteger(R.integer.view_fence_time_limit_max);
        CONTINUE_CENTER_EDIT_FENCE_DELAY = getResources().getInteger(R.integer.edit_fence_time_limit);
        CONTINUE_CENTER_VIEW_TRACK_DELAY = getResources().getInteger(R.integer.view_track_time_limit_max);
        initViews();
        mRxPermissions = new RxPermissions(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        String space = PreferenceManager.getDefaultSharedPreferences(this).
                getString("frequency", "" + getResources().getInteger(R.integer.track_freq_default));
        mFrequency = Integer.parseInt(space);
        mRxPermissions.requestEach(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Permission>() {
            @Override
            public void accept(Permission permission) throws Exception {
                XLog.dPermission("" + permission.name + "," + permission.granted);
                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission.name) && permission.granted) {
                    startLocation();
                } else if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission.name) && permission.shouldShowRequestPermissionRationale) {

                }
            }
        });

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                XLog.dLocation("nani ...onScroll ... " + distanceX + "," + distanceY);
                mStopCenter = true;
                mHandler.removeMessages(MSG_CONTINUE_CENTER);
                mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER, 5000);
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                XLog.dLocation("nani ...onFling ... " + velocityX + "," + velocityY);
                return false;
            }
        });

        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                XLog.dLocation("nani ...onMarkerClick ... " + marker.getTitle());
                return false;
            }
        });
//        requestOverlayPermission();
//        initTrackInfo();
//        drawDatabaseFenceInfo();
        mFenceDB = new FenceDB(this);
        drawDatabaseFenceInfo();
        initTrackInfo();
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MapActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        XLog.d("onCreate ... ");
    }

    private final String TRACK_NAME = "TrackName";
    private HashSet<String> mTrackSet;

    private void initTrackInfo() {
        mPolylineBundle.putString(TRACK_NAME, "Test");
        mTrackPolyline.extraInfo(mPolylineBundle);
        mTrackSet = mFenceDB.listAllTrackName();
    }

    private void initViews() {
        mDebugInfo = findViewById(R.id.debugInfo);
        mFloatingActionsMenu = findViewById(R.id.multiple_actions);
        mFloatingAddFence = findViewById(R.id.action_new_fence);
        mFloatingQueryFence = findViewById(R.id.action_query_fence);
        mFloatingRecordTrack = findViewById(R.id.action_record_track);
        mFloatingDownloadMap = findViewById(R.id.action_download);
        mFloatingQueryTrack = findViewById(R.id.action_query_track);
        mSaveTrackContainer = findViewById(R.id.save_tack_container);
        findViewById(R.id.action_settings).setOnClickListener(this);
        mSaveTrack = findViewById(R.id.save_track);
        mMyLocation = findViewById(R.id.myLocation);
        mMyLocation.setOnClickListener(this);
        mFloatingAddFence.setOnClickListener(this);
        mFloatingQueryFence.setOnClickListener(this);
        mFloatingRecordTrack.setOnClickListener(this);
        mFloatingQueryTrack.setOnClickListener(this);
        mFloatingDownloadMap.setOnClickListener(this);
        mSaveTrack.setOnClickListener(this);
        mapView = findViewById(R.id.baiduMap);
        mapView.showZoomControls(false);
        mBaiduMap = mapView.getMap();
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnPolylineClickListener(this);
//        开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(this);
        mLocationClientOption = new LocationClientOption();
        mLocationClientOption.setOpenGps(true); // 打开gps
        mLocationClientOption.setCoorType("bd09ll"); // 设置坐标类型
        mLocationClientOption.setScanSpan(1000);
//        mLocationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClientOption.setIgnoreKillProcess(true);
        mLocationClientOption.setNeedDeviceDirect(true);

        mLocationClient.setLocOption(mLocationClientOption);

        mLocationListener = new MyLocationListener();
        mLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, BitmapDescriptorFactory.fromResource(R.mipmap.icon_point), Color.TRANSPARENT, Color.TRANSPARENT);
        mLocationClient.registerLocationListener(mLocationListener);
        mBaiduMap.setMyLocationConfiguration(mLocationConfiguration);
    }

    private void drawDatabaseFenceInfo() {
        ArrayList<Sate7Fence> fenceList = mFenceDB.queryAllFence();
        for (Sate7Fence fence : fenceList) {
            XLog.dFenceDB("add fence ... " + fence.getFenceName());
            drawFence(fence);
            mFenceInOutStateMap.put(fence.getFenceName(), false);
            mFenceList.add(fence);
        }
    }

    private void drawFence(Sate7Fence fence) {
        boolean circle = fence.getFenceShape() == Sate7Fence.FENCE_TYPE_CIRCLE;
        XLog.dFenceDB("drawFence ... " + fence.getFenceName() + "," + circle + "," + fence.getDateInfo());
        if (circle) {
            drawCircleFence(fence);
        } else {
            drawPolygonFence(fence);
        }
    }

    private void drawCircleFence(Sate7Fence fence) {
        XLog.dFenceDB("addCircleFence ... " + fence.getFenceCircleRadius() + "," + fence.getFenceCenterLat() + "," + fence.getFenceCenterLng());
        LatLng center = new LatLng(fence.getFenceCenterLat(), fence.getFenceCenterLng());
        mCircleOptions = new CircleOptions().center(center)
                .radius(fence.getFenceCircleRadius())
                .fillColor(getResources().getColor(R.color.fence_circle_fill_color)) //填充颜色
                .stroke(new Stroke(5, getResources().getColor(R.color.fence_circle_stroke_color))); //边框宽和边框颜色
        Overlay fenceCircle = mBaiduMap.addOverlay(mCircleOptions);
        //add center marker
        Overlay centerMarker = addPointMarker(center, BitmapDescriptorFactory.fromResource(R.mipmap.icon_markx), fence.getFenceName());
        OverlayOptions mTextOptions = new TextOptions()
                .text(fence.getFenceName()) //文字内容
                .bgColor(getResources().getColor(R.color.fence_text_bg_color)) //背景色
                .fontSize(getResources().getDimensionPixelSize(R.dimen.fenc_text_size)) //字号
                .fontColor(getResources().getColor(R.color.fence_text_color)) //文字颜色
                .position(center);
        Overlay textMarker = mBaiduMap.addOverlay(mTextOptions);
        ArrayList<Overlay> overlays = new ArrayList<>();
        overlays.add(fenceCircle);
        overlays.add(centerMarker);
        overlays.add(textMarker);
        mFenceOverlayMaps.put(fence.getFenceName(), overlays);
    }

    private void drawPolygonFence(Sate7Fence fence) {
        LatLng center = new LatLng(fence.getFenceCenterLat(), fence.getFenceCenterLng());
        XLog.d("addPolygonFence ..." + center.latitude + "," + center.longitude + "," + fence.getFenceName());
        if (fence.getFenceShape() != Sate7Fence.FENCE_TYPE_POLYGON) {
            throw new IllegalArgumentException("addPolygonFence only is invoke by FENCE_TYPE_POLYGON");
        }
        double[] lats = fence.getPolygonPointLats();
        double[] lngs = fence.getPolygonPointLngs();
        ArrayList<LatLng> points = new ArrayList<>();
        int length = lats.length;
        for (int i = 0; i < length; i++) {
            points.add(new LatLng(lats[i], lngs[i]));
        }
        //draw Polygon
        PolygonOptions mPolygonOptions = new PolygonOptions()
                .points(points)
                .fillColor(getResources().getColor(R.color.fence_polygon_fill_color)) //填充颜色
                .stroke(new Stroke(5, getResources().getColor(R.color.fence_polygon_stroke_color))); //边框宽度和颜色
        Overlay fenceOverlay = mBaiduMap.addOverlay(mPolygonOptions);
        Overlay centerOverlay = addPointMarker(center, BitmapDescriptorFactory.fromResource(R.mipmap.icon_markx), fence.getFenceName());
        OverlayOptions mTextOptions = new TextOptions()
                .text(fence.getFenceName()) //文字内容
                .bgColor(getResources().getColor(R.color.fence_text_bg_color)) //背景色
                .fontSize(getResources().getDimensionPixelSize(R.dimen.fenc_text_size)) //字号
                .fontColor(getResources().getColor(R.color.fence_text_color)) //文字颜色
                .position(center);
        Overlay titleOverlay = mBaiduMap.addOverlay(mTextOptions);
        ArrayList<Overlay> overlays = new ArrayList<>();
        overlays.add(fenceOverlay);
        overlays.add(centerOverlay);
        overlays.add(titleOverlay);
        mFenceOverlayMaps.put(fence.getFenceName(), overlays);
    }

    private void startLocation() {
        XLog.dLocation("startLocation ... ");
        mLocationClient.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (!GPS.isGPSOpen(this)) {

            new AlertDialog.Builder(this).setTitle(R.string.open_gps_title).setMessage(R.string.open_gps_message).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivityForResult(GPS.getOpenGPSIntent(MapActivity.this), REQUEST_CODE_OPEN_GPS);
                }
            }).show();
        }
    }

    private boolean mStartTack = false;
    private boolean mStopCenter = false;
    private boolean mStopCenterForCreateFenceAuto = false;
    private boolean mStopCenterForCreateFenceManual = false;
    private boolean mStopCenterForEditFence = false;
    private boolean mStopCenterForViewFence = false;
    private boolean mStopCenterForMoveMap = false;
    private boolean mStopCenterForViewTrack = false;
    private AlertDialog mHintDialog;
    private AlertDialog.Builder mHintDialogBuilder;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        XLog.d("onNewIntent ..." + intent);
        handleEditFence(intent);
    }

    private void handleEditFence(Intent intent) {
        String oldName = intent.getStringExtra("old_name");
        if (TextUtils.isEmpty(oldName)) {
            return;
        }
        Sate7Fence fence = intent.getParcelableExtra("edit_fence");
        //remove first;
        int delete = mFenceDB.deleteByFenceName(oldName);
        long insert = mFenceDB.insertFence(fence);
        mStopCenterForEditFence = true;
        deleteMarkerByName(oldName);
        XLog.d("handleEditFence ww ..." + delete + "," + insert + "," + oldName + "," + fence);
        drawDatabaseFenceInfo();
        moveToMyLocation(new LatLng(fence.getFenceCenterLat(), fence.getFenceCenterLng()));
        mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_EDIT_FENCE, CONTINUE_CENTER_EDIT_FENCE_DELAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        XLog.d("onActivityResult map Activity ... " + requestCode + " , " + resultCode + " , " + data);
        if (requestCode == REQUEST_CODE_OPEN_GPS && !GPS.isGPSOpen(this)) {
            finish();
        }
        if (requestCode == REQUEST_CODE_NEW_FENCE && resultCode == Activity.RESULT_OK) {
            Sate7Fence fence = data.getExtras().getParcelable("DATA_FENCE");
            boolean selfData = data.getBooleanExtra("SELF_DATA", false) ||
                    (data != null && data.getAction() != null && data.getAction().equals("com.wlj.nani"));
            XLog.d("onActivityResult create fence .. " + selfData + "," + fence);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(mBaiduMap.getMaxZoomLevel() - 2));

            if (selfData) {//create fence directly
                mStopCenterForCreateFenceAuto = true;
                mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_CREATE_FENCE_AUTO, CONTINUE_CENTER_CREATE_FENCE_DELAY_AUTO);
                mPointLists.clear();
                mPolygonMarkers.clear();
                moveToMyLocation(new LatLng(fence.getFenceCenterLat(), fence.getFenceCenterLng()));
                if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_CIRCLE) {
                    addCircleFence(fence, new LatLng(fence.getFenceCenterLat(), fence.getFenceCenterLng()));
                } else if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_POLYGON) {
                    long insertId = mFenceDB.insertFence(fence);
                    drawPolygonFence(fence);
                    XLog.d("draw polygon fence ... " + insertId);
                }
                mPointLists.clear();
                mPolygonMarkers.clear();
            } else {
                mStopCenterForCreateFenceManual = true;
                showHintDialogIfNeeded(fence);
                mMapClickListener = new MapClickListener(fence);
                mBaiduMap.setOnMapClickListener(mMapClickListener);
            }
        }

        if (requestCode == REQUEST_FENCE_LIST_ACTIVITY && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra("center_fence", false)) {
                XLog.d("onActivityResult view fence ... ");
                Sate7Fence fence = data.getParcelableExtra("fence");
                mStopCenterForViewFence = true;
                moveToMyLocation(new LatLng(fence.getFenceCenterLat(), fence.getFenceCenterLng()));
                mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_VIEW_FENCE, CONTINUE_CENTER_VIEW_FENCE_DELAY);
            }
            if (data.getBooleanExtra("show_track", false)) {
                String trackName = data.getStringExtra("track_name");
                XLog.d("onActivityResult view track ... " + trackName);
                drawTrack(mFenceDB.queryTrackPoints(trackName), trackName);
            }
            //delete fence
            String[] names = data.getExtras().getStringArray("delete_fence_name");
            if (names != null) {
                XLog.d("onActivityResult delete fence ... " + Arrays.toString(names));
                for (String name : names) {
                    deleteMarkerByName(name);
                }
            }
            //delete track
            String[] trackNames = data.getExtras().getStringArray("delete_track_name");
            if (trackNames != null) {
                XLog.d("onActivityResult delete track ... " + Arrays.toString(trackNames));
                for (String name : trackNames) {
                    mFenceDB.deleteTrackByName(name);
                    hideTrackByName(name);
                }
            }
        }
    }

    //Fence related
    private CircleOptions mCircleOptions;
    private ArrayList<LatLng> mPointLists = new ArrayList<>();
    private ArrayList<Overlay> mPolygonMarkers = new ArrayList<>();
    private HashMap<String, ArrayList<Overlay>> mFenceOverlayMaps = new HashMap<>();
    private MapClickListener mMapClickListener;

    @Override
    public boolean onMarkerClick(final Marker marker) {
        XLog.dFenceDB("onMarkerClick ... " + marker.getTitle());
        SlideFromBottomPopup popup = new SlideFromBottomPopup(MapActivity.this, new SlideFromBottomPopup.OnPopupClickListener() {
            @Override
            public void onSureClick() {
                XLog.dFenceDB("onMarkerClick ... " + marker.getTitle());
                Set<String> keys = mFenceOverlayMaps.keySet();
                ArrayList<Overlay> overlayLists;
                for (String key : keys) {
                    if (key.equals(marker.getTitle())) {
                        //find the list
                        overlayLists = mFenceOverlayMaps.get(key);
                        for (Overlay overlay : overlayLists) {
                            //delete in Local;
                            overlay.remove();
                            XLog.dFenceDB("onMarkerClick remove ... " + marker.getTitle() + "," + overlay.getClass());
                        }
                        mFenceOverlayMaps.remove(marker.getTitle());
                        break;
                    }
                }
//delete from DB
                int delete = mFenceDB.deleteByFenceName(marker.getTitle());
                if (delete > -1 || mHintDB) {
                    XLog.dFenceDB("delete " + marker.getTitle() + " success " + delete);
                    synchronized (mFenceList) {
                        Sate7Fence fenceToDelete = null;
                        for (Sate7Fence fence : mFenceList) {
                            if (fence.getFenceName().equals(marker.getTitle())) {
                                fenceToDelete = fence;
                                break;
                            }
                        }
                        if (fenceToDelete != null) {
                            XLog.dFenceDB("before delete ... " + mFenceList + " |||| " + mFenceInOutStateMap);
                            mFenceList.remove(fenceToDelete);
                            mFenceInOutStateMap.remove(marker.getTitle());
                            XLog.dFenceDB("after delete ... " + mFenceList + " |||| " + mFenceInOutStateMap);
                        }
                    }
                }
            }
        });
        popup.showPopupWindow();
        popup.setTitle(getResources().getString(R.string.delete_fence_title, marker.getTitle()));
        return false;
    }

    private boolean deleteMarkerByName(String title) {
        XLog.d("deleteMarkerByName ... " + title);
        Set<String> keys = mFenceOverlayMaps.keySet();
        ArrayList<Overlay> overlayLists;
        for (String key : keys) {
            if (key.equals(title)) {
                //find the list
                overlayLists = mFenceOverlayMaps.get(key);
                for (Overlay overlay : overlayLists) {
                    //delete in Local;
                    XLog.d("deleteMarkerByName 11... ");
                    overlay.remove();
                }
                break;
            }
        }

        synchronized (mFenceList) {
            Sate7Fence fenceToDelete = null;
            for (Sate7Fence fence : mFenceList) {
                if (fence.getFenceName().equals(title)) {
                    fenceToDelete = fence;
                    break;
                }
            }
            if (fenceToDelete != null) {
                XLog.dFenceDB("before delete ... " + mFenceList + " || " + mFenceInOutStateMap);
                mFenceList.remove(fenceToDelete);
                mFenceInOutStateMap.remove(title);
                XLog.dFenceDB("after delete ... " + mFenceList + " || " + mFenceInOutStateMap);
            }
        }
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        XLog.d("onPolylineClick ... " + polyline.getExtraInfo());
        final String name = polyline.getExtraInfo().getString("track_name");
        SlideFromBottomPopupPolyline popup = new SlideFromBottomPopupPolyline(MapActivity.this, new SlideFromBottomPopupPolyline.OnPopupClickListener() {
            @Override
            public void onSureClick() {
                int id = mFenceDB.deleteTrackByName(name);
                XLog.d("onSureClick delete ..." + id);
                hideTrackByName(name);
            }

            @Override
            public void onHideClick() {
                XLog.d("onHideClick before hide ..." + mTrackOverlays);
                hideTrackByName(name);
            }

        });
        popup.showPopupWindow();
        popup.setTitle(getResources().getString(R.string.delete_track_title, name));
        return false;
    }
    private void hideTrackByName(String trackName){
        ArrayList<Overlay> overlays = mTrackOverlays.get(trackName);
        if(mTrackOverlays.isEmpty()){
            return;
        }
        for (Overlay overlay : overlays) {
            XLog.d("onHideClick remove ... " + overlay.getClass());
            overlay.remove();
        }
        mTrackOverlays.remove(trackName);
        XLog.d("onHideClick after hide ..." + mTrackOverlays);
    }
    private int mFrequency;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String frequency = sharedPreferences.getString("frequency", "" + getResources().getInteger(R.integer.track_freq_default));
        mFrequency = Integer.parseInt(frequency);
        XLog.d("onSharedPreferenceChanged ... " + mFrequency);
    }

    private class MapClickListener implements BaiduMap.OnMapClickListener {
        private Sate7Fence fence;

        public MapClickListener(Sate7Fence fence) {
            this.fence = fence;
        }

        @Override
        public void onMapClick(final LatLng latLng) {
            XLog.d("onMapClick AA ..." + latLng.longitude + "," + latLng.latitude);
            if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_CIRCLE) {
                if (SpHelper.needLastSure()) {
                    new AlertDialog.Builder(MapActivity.this).setTitle(R.string.create_fence).setMessage(R.string.create_fence_message).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mBaiduMap.setOnMapClickListener(null);
                            addCircleFence(fence, latLng);
                            mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL, CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL);
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mBaiduMap.setOnMapClickListener(null);
                            mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL, CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL);
                        }
                    }).setCancelable(false).show();
                } else {
                    addCircleFence(fence, latLng);
                    mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL, CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL);
                }

            } else if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_POLYGON) {
                addPolygonFence(fence, latLng);
            }
        }

        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            XLog.d("onMapPoiClick BB ..." + mapPoi.getPosition().latitude + "," + mapPoi.getPosition().latitudeE6 + "," + mapPoi.getPosition().longitude + "," + mapPoi.getPosition().longitudeE6);
            return false;
        }
    }

    private void addCircleFence(Sate7Fence fence, LatLng center) {
        XLog.d("addCircleFence ... " + fence);
        mCircleOptions = new CircleOptions().center(center)
                .radius(fence.getFenceCircleRadius())
                .fillColor(getResources().getColor(R.color.fence_circle_fill_color)) //填充颜色
                .stroke(new Stroke(5, getResources().getColor(R.color.fence_circle_stroke_color))); //边框宽和边框颜色
        Overlay fenceCircle = mBaiduMap.addOverlay(mCircleOptions);
        //add center marker
        Overlay centerMarker = addPointMarker(center, BitmapDescriptorFactory.fromResource(R.mipmap.icon_markx), fence.getFenceName());
        mBaiduMap.setOnMapClickListener(mMapClickListener);
        mBaiduMap.setOnMapClickListener(null);
        OverlayOptions mTextOptions = new TextOptions()
                .text(fence.getFenceName()) //文字内容
                .bgColor(getResources().getColor(R.color.fence_text_bg_color)) //背景色
                .fontSize(getResources().getDimensionPixelSize(R.dimen.fenc_text_size)) //字号
                .fontColor(getResources().getColor(R.color.fence_text_color)) //文字颜色
                .position(center);
        Overlay textMarker = mBaiduMap.addOverlay(mTextOptions);
        //TODO
        fence.setFenceCenterLat(center.latitude);
        fence.setFenceCenterLng(center.longitude);
        //save to DB
        long id = mFenceDB.insertFence(fence);
        if (mHintDB || id > 0) {
            XLog.dFenceDB("save to fence db success .." + id);
            synchronized (mFenceList) {
                mFenceList.add(fence);
                mFenceInOutStateMap.put(fence.getFenceName(), false);
            }
        }
        ArrayList<Overlay> overlays = new ArrayList<>();
        overlays.add(fenceCircle);
        overlays.add(centerMarker);
        overlays.add(textMarker);
        mFenceOverlayMaps.put(fence.getFenceName(), overlays);
    }

    private void realAddPolygonFence(Sate7Fence fence) {
        XLog.d("realAddPolygonFence ...");
        //draw Polygon
        PolygonOptions mPolygonOptions = new PolygonOptions()
                .points(mPointLists)
                .fillColor(getResources().getColor(R.color.fence_polygon_fill_color)) //填充颜色
                .stroke(new Stroke(5, getResources().getColor(R.color.fence_polygon_stroke_color))); //边框宽度和颜色
        Overlay fenceOverlay = mBaiduMap.addOverlay(mPolygonOptions);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : mPointLists) {
            builder.include(latLng);
            fence.addPolygonPointLatLng(latLng);
        }
        LatLng center = builder.build().getCenter();
        fence.setFenceCenterLng(center.longitude);
        fence.setFenceCenterLat(center.latitude);
        //save this points to Database
        long id = mFenceDB.insertFence(fence);
        if (id > 0) {
            synchronized (mFenceList) {
                mFenceList.add(fence);
                mFenceInOutStateMap.put(fence.getFenceName(), false);
            }
        }
        for (Overlay overlay : mPolygonMarkers) {
            overlay.remove();
        }
        mPolygonMarkers.clear();
        Overlay centerOverlay = addPointMarker(center, BitmapDescriptorFactory.fromResource(R.mipmap.icon_markx), fence.getFenceName());
        OverlayOptions mTextOptions = new TextOptions()
                .text(fence.getFenceName()) //文字内容
                .bgColor(getResources().getColor(R.color.fence_text_bg_color)) //背景色
                .fontSize(getResources().getDimensionPixelSize(R.dimen.fenc_text_size)) //字号
                .fontColor(getResources().getColor(R.color.fence_text_color)) //文字颜色
                .position(center);
        Overlay textOverlay = mBaiduMap.addOverlay(mTextOptions);
        mBaiduMap.setOnMapClickListener(null);
        ArrayList<Overlay> overlays = new ArrayList<>();
        overlays.add(fenceOverlay);
        overlays.add(centerOverlay);
        overlays.add(textOverlay);
        mPointLists.clear();
        mFenceOverlayMaps.put(fence.getFenceName(), overlays);
    }

    private void addPolygonFence(final Sate7Fence fence, LatLng current) {
        XLog.d("addPolygonFence ...");
        if (fence.getFenceShape() != Sate7Fence.FENCE_TYPE_POLYGON) {
            throw new IllegalArgumentException("addPolygonFence only is invoke by FENCE_TYPE_POLYGON");
        }
        if (mPointLists.size() < fence.getFencePolygonPoints()) {
            mPointLists.add(current);
            //draw Point Marker
            addPolygonPointMarker(current);
        }
        if (mPointLists.size() == fence.getFencePolygonPoints()) {
            if (SpHelper.needLastSure()) {
                new AlertDialog.Builder(MapActivity.this).setTitle(R.string.create_fence).setMessage(R.string.create_fence_message).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBaiduMap.setOnMapClickListener(null);
                        realAddPolygonFence(fence);
                        mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL, CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBaiduMap.setOnMapClickListener(null);
                        //remove the Point Marker
                        for (Overlay overlay : mPolygonMarkers) {
                            overlay.remove();
                        }
                        mPolygonMarkers.clear();
                        mPointLists.clear();
                        mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL, CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL);
                    }
                }).setCancelable(false).show();
            } else {
                realAddPolygonFence(fence);
                mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_CREATE_FENCE_MANUAL, CONTINUE_CENTER_CREATE_FENCE_DELAY_MANUAL);
            }

        }
    }

    //必须保证CenterMarker名字和Fence名字一致,要不有bug;
    private Overlay addPointMarker(LatLng current, BitmapDescriptor bitmapDescriptor, String title) {
        OverlayOptions option = new MarkerOptions()
                .position(current)
                .icon(bitmapDescriptor).title(title);
        return mBaiduMap.addOverlay(option);
    }

    private void addPolygonPointMarker(LatLng current) {
        BitmapDescriptor bitmap = BitmapGetter.getBitmap(mPointLists.size());
        OverlayOptions option = new MarkerOptions()
                .position(current)
                .icon(bitmap);
        Overlay overlay = mBaiduMap.addOverlay(option);
        mPolygonMarkers.add(overlay);
    }

    private void showHintDialogIfNeeded(Sate7Fence fence) {
        XLog.d("showHintDialogIfNeeded ... ");
        if (!SpHelper.needShowFenceHint()) {
            return;
        }
        mHintDialogBuilder = new AlertDialog.Builder(this);
        mHintDialogBuilder.setTitle(fence.getFenceName()).setMessage(fence.getFenceShape() == Sate7Fence.FENCE_TYPE_CIRCLE ? getResources().getString(R.string.create_circle_fence_hint) :
                getResources().getString(R.string.create_polygon_fence_hint, fence.getFencePolygonPoints())).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNeutralButton(R.string.not_show_no_longer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SpHelper.setNoHintFence();
            }
        });
        mHintDialog = mHintDialogBuilder.create();
        mHintDialog.show();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        goHome();
    }

    private void goHome() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
    }

    private final int REQUEST_CODE_OPEN_GPS = 0x10;
    private final int REQUEST_CODE_NEW_FENCE = 0x11;
    private final int REQUEST_FENCE_LIST_ACTIVITY = 0x12;

    @Override
    public void onClick(View v) {
        XLog.dLocation("onClick ..." + v.getId());
        switch (v.getId()) {
            case R.id.action_new_fence:
                startActivityForResult(new Intent(this, FenceOptionActivity.class), REQUEST_CODE_NEW_FENCE);
                break;
            case R.id.action_query_fence:
                startActivityForResult(new Intent(this, FenceListActivity.class), REQUEST_FENCE_LIST_ACTIVITY);
                break;
            case R.id.action_record_track:
                new AlertDialog.Builder(this).setTitle(R.string.track_title).setMessage(R.string.track_message).setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mStartTack = true;
                        /*if (isCanOverlays()) {
                            showFloatWindow();
                        } else {
                            requestOverlayPermission();
                        }*/
//                        mSaveTrackContainer.setVisibility(View.VISIBLE);
                        mSaveTrackContainer.show();
                    }
                }).show();
                break;
            case R.id.action_download:
                Intent intent = new Intent(this, OfflineMapActivity.class);
                intent.putExtra("ListTrack", true);
                startActivity(intent);
                break;
            case R.id.action_query_track:
//                mFenceDB.clearAllFence();
                intent = new Intent(this, FenceListActivity.class);
                intent.putExtra("ListTrack", true);
                startActivityForResult(intent, REQUEST_FENCE_LIST_ACTIVITY);
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.save_track:
                XLog.d("FloatButton clicked ... ");
                showSaveTrackDialog();
                break;
            case R.id.myLocation:
                XLog.d("myLocation clicked ... " + mLastBDLocation);
                if (mLastBDLocation != null) {
                    moveToMyLocation(new LatLng(mLastBDLocation.getLatitude(), mLastBDLocation.getLongitude()));
                    mStopCenter = false;
                }
                break;
            default:
        }

        mFloatingActionsMenu.collapse();
    }

    private void moveToMyLocation(LatLng center) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(center)
                .zoom(mBaiduMap.getMaxZoomLevel())
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
    }

    private void moveToMyLocation(LatLng center, float level) {
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(center)
                .zoom(level)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
    }

    private BDLocation mLastBDLocation;
    private MyLocationData myLocationData;

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            XLog.dLocation("onReceiveLocation ww22 ... " + bdLocation.getLongitude() + "," + bdLocation.getLatitude() + "," + mStopCenter + "," + mStartTack);
            mLastBDLocation = bdLocation;
            //mapView 销毁后不在处理新接收的位置
            if (bdLocation == null || mapView == null ||
                    mStopCenter ||
                    mStopCenterForMoveMap ||
                    mStopCenterForCreateFenceAuto ||
                    mStopCenterForCreateFenceManual ||
                    mStopCenterForEditFence ||
                    mStopCenterForViewFence ||
                    mStopCenterForViewTrack) {
                return;
            }
            myLocationData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(myLocationData);
            if (mStartTack) {
                startTrack(bdLocation);
            }
            //handler in or out fence
            handlerInOutFence(bdLocation);
        }
    }

    private HashMap<String, Boolean> mFenceInOutStateMap = new HashMap<>();
    private StringBuilder debugStringBuild = new StringBuilder();
    private boolean currentInFence = false;
    private boolean lastTimeInFence = false;
    private LatLng current;
    private ArrayList<Sate7Fence> mFenceList = new ArrayList<>();
    private PendingIntent pendingIntent;

    private void handlerInOutFence(BDLocation bdLocation) {
        XLog.dLocation("handlerInOutFence ww...");
        current = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        debugStringBuild.delete(0, debugStringBuild.length());
        for (Sate7Fence fence : mFenceList) {
            currentInFence = inFence(fence, current);
            lastTimeInFence = mFenceInOutStateMap.get(fence.getFenceName());
            /*debugStringBuild.append(fence.getFenceName() + "\n当前是否在围栏:" + currentInFence +
                    "\n上次是否在围栏:" + lastTimeInFence +
                    "\n是否改变了:" + (lastTimeInFence != currentInFence) +
                    "\n更新前HashMap:" + mFenceInOutStateMap);*/
            XLog.dFenceDB("fence === " + getFenceNotifyType(fence));
            if (currentInFence != lastTimeInFence) {
                if (currentInFence &&
                        (getFenceNotifyType(fence) == Sate7Fence.MONITOR_MODE_IN ||
                                getFenceNotifyType(fence) == Sate7Fence.MONITOR_MODE_IN_OUT)) {
                    NotificationHelper.showIntoFenceNf(this, 25, fence.getFenceName(), pendingIntent);
                }

                if (!currentInFence && (getFenceNotifyType(fence) == Sate7Fence.MONITOR_MODE_OUT ||
                        getFenceNotifyType(fence) == Sate7Fence.MONITOR_MODE_IN_OUT)) {
                    NotificationHelper.showExitFenceNf(this, 25, fence.getFenceName(), pendingIntent);
                }
            }
            mFenceInOutStateMap.put(fence.getFenceName(), currentInFence);
//            debugStringBuild.append("\n更新后HashMap:" + mFenceInOutStateMap);
        }
//        mDebugInfo.setText(debugStringBuild.toString());
    }

    private int getFenceNotifyType(Sate7Fence fence) {

        int startHour = fence.getMonitorStartHour();
        int startMinute = fence.getMonitorStartMinute();
        int start = startHour * 60 + startMinute;

        int endMinute = fence.getMonitorEndMinute();
        int endHour = fence.getMonitorEndHour();
        int end = endHour * 60 + endMinute;

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int current = currentHour * 60 + currentMinute;
        XLog.dFenceDB("getFenceNotifyType ..." + start + "," + end + "," + current);
        if (current > start && current < end) {
            return fence.getMonitorMode();
        } else {
            return -1;
        }
    }

    private boolean inFence(Sate7Fence fence, LatLng current) {
        XLog.dLocation("inFence ... ");
        if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_CIRCLE) {
            LatLng center = new LatLng(fence.getFenceCenterLat(), fence.getFenceCenterLng());
            int radio = fence.getFenceCircleRadius();
            return DistanceUtil.getDistance(center, current) < radio;
        } else if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_POLYGON) {
            int size = fence.getFencePolygonPoints();
            ArrayList<LatLng> points = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                points.add(new LatLng(fence.getPolygonPointLats()[i], fence.getPolygonPointLngs()[i]));
            }
            return SpatialRelationUtil.isPolygonContainsPoint(points, current);
        }
        return false;
    }

    private LatLng mLastRecordBDLocation = new LatLng(0, 0);
    private long mLastRecordTime;
    private Bundle mPolylineBundle = new Bundle();
    private PolylineOptions mTrackPolyline = new PolylineOptions().width(10).color(0xAAFF0000);
    private Overlay mTrackOverlay;

    private void startTrack(BDLocation bdLocation) {
        XLog.dLocation("startTrack ww... " + bdLocation.getSpeed() + "," + mFrequency);
        //采点要求：1、速度大于0，二、间隔10s，三、距离超过5m
        if (bdLocation.getSpeed() > 0.0 &&
                System.currentTimeMillis() - mLastRecordTime >= mFrequency /*&&
                DistanceUtil.getDistance(current, mLastRecordBDLocation) > */) {
            XLog.dLocation("startTrack save ww ... " + mTraceData.size());
            mLastRecordTime = System.currentTimeMillis();
            mLastRecordBDLocation = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            mTraceData.add(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude()));
            if (mTraceData.size() < 2) {
                return;
            }
            mTrackPolyline.points(mTraceData);
            if (mTrackOverlay != null) {
                mTrackOverlay.remove();
            }
            mTrackOverlay = mBaiduMap.addOverlay(mTrackPolyline);
        }
    }


    private PolylineOptions mTrackPolylineShow = new PolylineOptions().width(10).color(0xAA00FF00);
    private HashMap<String, ArrayList<Overlay>> mTrackOverlays = new HashMap();

    private void drawTrack(ArrayList<LatLng> points, String trackName) {
        ArrayList<Overlay> overlayList = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putString("track_name", trackName);
        mTrackPolylineShow.points(points);
        mTrackPolylineShow.extraInfo(bundle);
        //路线overlay
        Overlay overlayLine = mBaiduMap.addOverlay(mTrackPolylineShow);
        //TextView overlay
        TextOptions textOptions = new TextOptions()
                .text(trackName) //文字内容
                .bgColor(getResources().getColor(R.color.fence_text_bg_color)) //背景色
                .fontSize(getResources().getDimensionPixelSize(R.dimen.fenc_text_size)) //字号
                .fontColor(getResources().getColor(R.color.track_text_color)) //文字颜色
                .position(points.get(points.size() / 2));
        Overlay overlayText = mBaiduMap.addOverlay(textOptions);
        overlayList.add(overlayLine);
        overlayList.add(overlayText);
        mTrackOverlays.put(trackName, overlayList);
        moveToMyLocation(points.get(points.size() / 2), mBaiduMap.getMaxZoomLevel() - 2);
        mStopCenterForViewTrack = true;
        mHandler.sendEmptyMessageDelayed(MSG_CONTINUE_CENTER_VIEW_TRACK, CONTINUE_CENTER_VIEW_TRACK_DELAY);
    }


    private AlertDialog.Builder mBuilder;
    private AlertDialog mAlertDialog;
    private EditText mEditText;

    private void showSaveTrackDialog() {
        if (mBuilder == null) {
            mEditText = new EditText(this);
            mEditText.setHint(R.string.track_name_hint);
            mBuilder = new AlertDialog.Builder((Context) this).
                    setTitle(R.string.track_save_title).
                    setCancelable(false).
                    setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            XLog.d("setNegativeButton onClick ...");
                            mEditText.setText("");
                        }
                    }).
                    setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            XLog.d("setPositiveButton onClick ...");
                        }
                    }).setView(mEditText);
        }

        if (mAlertDialog == null) {
            mAlertDialog = mBuilder.create();
        }
        mAlertDialog.show();//must show first
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mEditText.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    mEditText.requestFocus();
                    return;
                }
                if (mTrackSet.contains(name)) {
                    mEditText.setError(getResources().getString(R.string.track_name_exists));
                    return;
                }

                if (mTraceData.size() < 20) {
                    mEditText.setError(getResources().getString(R.string.track_info_less));
                    return;
                }

                long id = mFenceDB.insertTrack(name, mTraceData);
                if (id > 0) {
                    mTrackSet.add(name);
                    mEditText.setText("");
                }
                XLog.d("View onClick 22... " + name + "," + id + "," + mTrackSet + "," + mTrackSet.contains(name));
                mAlertDialog.dismiss();
                mSaveTrackContainer.setVisibility(View.GONE);
            }
        });
    }
}
