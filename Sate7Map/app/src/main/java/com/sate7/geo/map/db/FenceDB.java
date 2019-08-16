package com.sate7.geo.map.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.baidu.mapapi.model.LatLng;
import com.sate7.geo.map.bean.Sate7Fence;
import com.sate7.geo.map.bean.Sate7Track;
import com.sate7.geo.map.util.XLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class FenceDB extends SQLiteOpenHelper {
    private static final String DB_NAME = "Sate7Map.db";
    private static final int DB_VERSION = 16;
    public static final String TABLE_FENCE = "fence";
    public static final String TABLE_TRACK = "track";
    public static final String FENCE_COLUMN_NAME = "name";//不能重复
    public static final String FENCE_COLUMN_CREATE_DATE = "created_time";
    public static final String FENCE_COLUMN_MONITOR_TYPE = "monitor_type";
    public static final String FENCE_COLUMN_START_MONTH = "start_month";
    public static final String FENCE_COLUMN_START_DAY = "start_day";
    public static final String FENCE_COLUMN_START_HOUR = "start_hour";
    public static final String FENCE_COLUMN_START_MINUTE = "start_minute";
    public static final String FENCE_COLUMN_END_MONTH = "end_month";
    public static final String FENCE_COLUMN_END_DAY = "end_day";
    public static final String FENCE_COLUMN_END_HOUR = "end_hour";
    public static final String FENCE_COLUMN_END_MINUTE = "end_minute";
    public static final String FENCE_COLUMN_FENCE_SHAPE = "fence_type";
    public static final String FENCE_COLUMN_CIRCLE_RADIUS = "fence_circle_radius";
    public static final String FENCE_COLUMN_CENTER_LAT = "fence_center_lat";
    public static final String FENCE_COLUMN_CENTER_LNG = "fence_center_lng";
    public static final String FENCE_COLUMN_POLYGON_POINTS = "fence_polygon_points";
    private String[] mAllSelection = new String[]{FENCE_COLUMN_NAME, FENCE_COLUMN_MONITOR_TYPE,
            FENCE_COLUMN_START_HOUR, FENCE_COLUMN_START_MINUTE, FENCE_COLUMN_END_HOUR,
            FENCE_COLUMN_END_MINUTE, FENCE_COLUMN_FENCE_SHAPE, FENCE_COLUMN_CIRCLE_RADIUS,
            FENCE_COLUMN_CENTER_LAT, FENCE_COLUMN_CENTER_LNG, FENCE_COLUMN_POLYGON_POINTS, FENCE_COLUMN_CREATE_DATE,
            FENCE_COLUMN_START_MONTH, FENCE_COLUMN_START_DAY, FENCE_COLUMN_END_MONTH, FENCE_COLUMN_END_DAY,
            "fence_polygon_point1Lat", "fence_polygon_point1Lng", "fence_polygon_point2Lat", "fence_polygon_point2Lng",
            "fence_polygon_point3Lat", "fence_polygon_point3Lng", "fence_polygon_point4Lat", "fence_polygon_point4Lng",
            "fence_polygon_point5Lat", "fence_polygon_point5Lng", "fence_polygon_point6Lat", "fence_polygon_point6Lng",
            "fence_polygon_point7Lat", "fence_polygon_point7Lng", "fence_polygon_point8Lat", "fence_polygon_point8Lng",
            "fence_polygon_point9Lat", "fence_polygon_point9Lng", "fence_polygon_point10Lat", "fence_polygon_point10Lng"};
    private static final String CREATE_TABLE_TRACK_SQL = "create table " + TABLE_TRACK + "("
            + "_id integer primary key autoincrement,"
            + "created_time TimeStamp NOT NULL DEFAULT (datetime('now','localtime')),"
            + "track_name varchar,"
            + "start_lat double,"
            + "start_lng double,"
            + "end_lat double,"
            + "end_lng double,"
            + "track_points_lat text not null,"
            + "track_points_lng text not null"
            + ")";
    //创建 students 表的 sql 语句
    private static final String CREATE_TABLE_FENCE_SQL = "create table " + TABLE_FENCE + "("
            + "_id integer primary key autoincrement,"
            + "created_time TimeStamp NOT NULL DEFAULT (datetime('now','localtime')),"
            + "name varchar not null,"
            + "monitor_type integer not null,"
            + "start_month integer not null,"
            + "start_day integer not null,"
            + "start_hour integer not null,"
            + "start_minute integer not null,"
            + "end_month integer not null,"
            + "end_day integer not null,"
            + "end_hour integer not null,"
            + "end_minute integer not null,"
            + "fence_type integer not null,"
            + "fence_circle_radius integer DEFAULT 100,"
            + "fence_center_lat double not null,"
            + "fence_center_lng double not null,"
            + "fence_polygon_points integer,"
            + "fence_polygon_point1Lat double DEFAULT -1,"
            + "fence_polygon_point1Lng double DEFAULT -1,"
            + "fence_polygon_point2Lat double DEFAULT -1,"
            + "fence_polygon_point2Lng double DEFAULT -1,"
            + "fence_polygon_point3Lat double DEFAULT -1,"
            + "fence_polygon_point3Lng double DEFAULT -1,"
            + "fence_polygon_point4Lat double DEFAULT -1,"
            + "fence_polygon_point4Lng double DEFAULT -1,"
            + "fence_polygon_point5Lat double DEFAULT -1,"
            + "fence_polygon_point5Lng double DEFAULT -1,"
            + "fence_polygon_point6Lat double DEFAULT -1,"
            + "fence_polygon_point6Lng double DEFAULT -1,"
            + "fence_polygon_point7Lat double DEFAULT -1,"
            + "fence_polygon_point7Lng double DEFAULT -1,"
            + "fence_polygon_point8Lat double DEFAULT -1,"
            + "fence_polygon_point8Lng double DEFAULT -1,"
            + "fence_polygon_point9Lat double DEFAULT -1,"
            + "fence_polygon_point9Lng double DEFAULT -1,"
            + "fence_polygon_point10Lat double DEFAULT -1,"
            + "fence_polygon_point10Lng double DEFAULT -1"
            + ");";
    private static final String UPDATE_TABLE_FENCE_SQL = "drop table if exists " + TABLE_FENCE;
    private static final String UPDATE_TABLE_TRACK_SQL = "drop table if exists " + TABLE_TRACK;

    public FenceDB(Context context) {
        this(context, DB_NAME, null, DB_VERSION);
    }

    public FenceDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public FenceDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FENCE_SQL);
        db.execSQL(CREATE_TABLE_TRACK_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(UPDATE_TABLE_FENCE_SQL);
        db.execSQL(UPDATE_TABLE_TRACK_SQL);
        db.execSQL(CREATE_TABLE_FENCE_SQL);
        db.execSQL(CREATE_TABLE_TRACK_SQL);
    }

    public long insertFence(Sate7Fence fence) {
        XLog.dFenceDB("insertFence ww ... " + fence);
        ContentValues values = new ContentValues();
        values.put(FENCE_COLUMN_NAME, fence.getFenceName());
        values.put(FENCE_COLUMN_MONITOR_TYPE, fence.getMonitorMode());
        values.put(FENCE_COLUMN_START_MONTH, fence.getMonitorStartMonth());
        values.put(FENCE_COLUMN_START_DAY, fence.getMonitorStartDay());
        values.put(FENCE_COLUMN_START_HOUR, fence.getMonitorStartHour());
        values.put(FENCE_COLUMN_START_MINUTE, fence.getMonitorStartMinute());
        values.put(FENCE_COLUMN_END_MONTH, fence.getMonitorEndMonth());
        values.put(FENCE_COLUMN_END_DAY, fence.getMonitorEndDay());
        values.put(FENCE_COLUMN_END_HOUR, fence.getMonitorEndHour());
        values.put(FENCE_COLUMN_END_MINUTE, fence.getMonitorEndMinute());
        values.put(FENCE_COLUMN_FENCE_SHAPE, fence.getFenceShape());
        if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_CIRCLE) {
            values.put(FENCE_COLUMN_CIRCLE_RADIUS, fence.getFenceCircleRadius());
        } else if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_POLYGON) {
            int points = fence.getFencePolygonPoints();
            values.put(FENCE_COLUMN_POLYGON_POINTS, points);
            double[] lats = fence.getPolygonPointLats();
            double[] lngs = fence.getPolygonPointLngs();
            XLog.dFenceDB("polygon points == " + points + "," + Arrays.toString(lngs) + "," + Arrays.toString(lats));
            for (int i = 1; i <= points; i++) {
                values.put("fence_polygon_point" + i + "Lat", lats[i - 1]);
                values.put("fence_polygon_point" + i + "Lng", lngs[i - 1]);
            }
        }
        values.put(FENCE_COLUMN_CENTER_LAT, fence.getFenceCenterLat());
        values.put(FENCE_COLUMN_CENTER_LNG, fence.getFenceCenterLng());
        return getWritableDatabase().insert(TABLE_FENCE, null, values);
    }

    public ArrayList<Sate7Fence> queryAllFence() {
        ArrayList<Sate7Fence> fenceList = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.query(TABLE_FENCE, mAllSelection, null, null, null, null, FENCE_COLUMN_CREATE_DATE, null);
        Sate7Fence fence;
        XLog.dFenceDB("queryAllFence ... cursor ... " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                String fenceName = cursor.getString(0);
                int monitorType = cursor.getInt(1);
                int startHour = cursor.getInt(2);
                int startMinute = cursor.getInt(3);
                int endHour = cursor.getInt(4);
                int endMinute = cursor.getInt(5);
                int shape = cursor.getInt(6);
                int radius = cursor.getInt(7);
                double centerLat = cursor.getDouble(8);
                double centerLng = cursor.getDouble(9);
                int polygonPoints = cursor.getInt(10);
                String timeStamp = cursor.getString(11);
                int startMonth = cursor.getInt(12);
                int startDay = cursor.getInt(13);
                int endMonth = cursor.getInt(14);
                int endDay = cursor.getInt(15);
                fence = new Sate7Fence(fenceName, monitorType, startMonth, startDay, startHour, startMinute, endMonth, endDay,
                        endHour, endMinute, shape, radius, centerLat, centerLng, polygonPoints);
                fence.setDateInfo(timeStamp);
//                XLog.dFenceDB("time debug ... " + timeStamp);
                if (shape == Sate7Fence.FENCE_TYPE_POLYGON) {
                    for (int i = 0; i < polygonPoints; i++) {
                        fence.addPolygonPointLatLng(new LatLng(cursor.getDouble(16 + i * 2), cursor.getDouble(13 + i * 2)));
                    }
                }
                fenceList.add(fence);
            } while (cursor.moveToNext());
        }
        cursor.close();
        XLog.dFenceDB("queryAllFence ... data ... " + fenceList);
        return fenceList;
    }

    public int clearAllFence() {
        SQLiteDatabase database = getWritableDatabase();
//        database.execSQL("delete from " + TABLE_FENCE);
        return database.delete(TABLE_FENCE, null, null);
    }

    public int deleteByFenceName(String name) {
        return getWritableDatabase().delete(TABLE_FENCE, FENCE_COLUMN_NAME + " = ?", new String[]{name});
    }

    public HashSet<String> getAllFenceName() {
        Cursor cursor = getWritableDatabase().query(TABLE_FENCE, new String[]{FENCE_COLUMN_NAME}, null, null, null, null, null);
        HashSet<String> nameSet = new HashSet();
        if (cursor.moveToFirst()) {
            do {
                nameSet.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return nameSet;
    }

    private String[] mSelectionTrack = new String[]{
            "created_time", "track_name", "track_points_lat", "track_points_lng"
    };

    public long insertTrack(String name, ArrayList<LatLng> points) {
        ArrayList<Double> lat = new ArrayList<>(points.size());
        ArrayList<Double> lng = new ArrayList<>(points.size());
        for (LatLng p : points) {
            lat.add(p.latitude);
            lng.add(p.longitude);
        }
        return insertTrack(name, lat, lng);
    }

    public int deleteTrackByName(String name) {
        return getWritableDatabase().delete(TABLE_TRACK, "track_name = ?", new String[]{name});
    }

    public long insertTrack(String name, ArrayList<Double> lats, ArrayList<Double> lngs) {
        JSONObject jsonObjectLat = new JSONObject();
        JSONObject jsonObjectLng = new JSONObject();
        String latText = null;
        String lngText = null;
        XLog.dFenceDB("insertTrack before lats ... " + lats);
        XLog.dFenceDB("insertTrack before lngs ... " + lngs);
        try {
            jsonObjectLat.put("track_points_lat", new JSONArray(lats));
            jsonObjectLng.put("track_points_lng", new JSONArray(lngs));

            latText = jsonObjectLat.toString();
            lngText = jsonObjectLng.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Track save error!");
//            Log.e("Error", "insertTrack ... JSONException " + e.getMessage());
        }
        XLog.dFenceDB("insertTrack latText ... " + latText);
        XLog.dFenceDB("insertTrack lngText ... " + lngText);
        ContentValues contentValues = new ContentValues();
        contentValues.put("track_name", name);
        if (latText != null) {
            contentValues.put("track_points_lat", latText);
        }

        if (lngText != null) {
            contentValues.put("track_points_lng", lngText);
        }

        return getWritableDatabase().insert(TABLE_TRACK, null, contentValues);
    }

    public HashSet<String> listAllTrackName() {
        HashSet<String> tracks = new HashSet<>();
        Cursor cursor = getWritableDatabase().query(TABLE_TRACK, new String[]{"created_time", "track_name"}, null, null, null, null, "created_time");
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                String name = cursor.getString(1);
                XLog.dFenceDB("listAllTrack ... " + date + "," + name);
                tracks.add(name);
            } while (cursor.moveToNext());
        }
        return tracks;
    }

    public ArrayList<Sate7Track> listAllTrackInfo() {
        ArrayList<Sate7Track> tracks = new ArrayList<>();
        Cursor cursor = getWritableDatabase().query(TABLE_TRACK, new String[]{"created_time", "track_name"}, null, null, null, null, "created_time");
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                String name = cursor.getString(1);
                XLog.dFenceDB("listAllTrack ... " + date + "," + name);
                tracks.add(new Sate7Track(name, date));
            } while (cursor.moveToNext());
        }
        return tracks;
    }

    public ArrayList<LatLng> queryTrackPoints(String name) {
        XLog.dFenceDB("queryTrackPoints aa ... " + name);
        Cursor cursor = getWritableDatabase().query(TABLE_TRACK, mSelectionTrack, "track_name = ?", new String[]{name}, null, null, "created_time");
        if (cursor.moveToFirst()) {
            try {
                XLog.dFenceDB("queryTrackPoints bb ... " + name + "," + cursor.getCount());
                String jsonLats = cursor.getString(2);
                String jsonLngs = cursor.getString(3);
                JSONObject lats = new JSONObject(jsonLats);
                JSONObject lngs = new JSONObject(jsonLngs);
                JSONArray latsArrays = lats.optJSONArray("track_points_lat");
                JSONArray lngsArrays = lngs.optJSONArray("track_points_lng");
                int lengthLat = latsArrays.length();
                ArrayList<LatLng> mPointList = new ArrayList<>(lengthLat);
                for (int i = 0; i < lengthLat; i++) {
                    mPointList.add(new LatLng((double) latsArrays.get(i), (double) lngsArrays.get(i)));
                }
                return mPointList;
            } catch (Exception e) {
                XLog.dFenceDB("Exception ... " + e.getMessage());
            }
        }
        cursor.close();
        return new ArrayList<>();
    }

    public void queryTrack() {
        Cursor cursor = getWritableDatabase().query(TABLE_TRACK, mSelectionTrack, null, null, null, null, "created_time");
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                String name = cursor.getString(1);
                String jsonLats = cursor.getString(2);
                String jsonLngs = cursor.getString(3);
//                XLog.dFenceDB("queryTrack jsonLats ... " + jsonLats);
//                XLog.dFenceDB("queryTrack jsonLngs ... " + jsonLngs);
                try {
                    JSONObject lats = new JSONObject(jsonLats);
                    JSONObject lngs = new JSONObject(jsonLngs);
                    JSONArray latsArrays = lats.optJSONArray("track_points_lat");
                    JSONArray lngsArrays = lngs.optJSONArray("track_points_lng");
                    int lengthLat = latsArrays.length();
                    int lengthLng = lngsArrays.length();
                    if (lengthLat != lengthLng) {
                        Log.e("Error", "lengthLat != lengthLng, something go wrong ... ");
                    }
                    ArrayList<LatLng> mPointList = new ArrayList<>(lengthLat);
                    for (int i = 0; i < lengthLat; i++) {
                        mPointList.add(new LatLng((double) latsArrays.get(i), (double) lngsArrays.get(i)));
                    }

                    XLog.dFenceDB("queryTrack ... " + mPointList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Track read error!");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}
