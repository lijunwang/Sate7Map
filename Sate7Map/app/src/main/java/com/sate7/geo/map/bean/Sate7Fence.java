package com.sate7.geo.map.bean;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;
import com.sate7.geo.map.util.XLog;

import java.util.ArrayList;
import java.util.Arrays;

public class Sate7Fence implements Parcelable {
    public static final String MONITOR_MODE_EXTRA = "MONITOR_MODE";
    public static final String FENCE_TYPE_EXTRA = "FENCE_TYPE";
    public static final String FENCE_CIRCLE_RADIUS_EXTRA = "FENCE_CIRCLE_RADIUS";
    public static final String FENCE_POLYGON_EXTRA = "FENCE_POLYGON_POINTS";
    public static final int MONITOR_MODE_IN = 0;
    public static final int MONITOR_MODE_OUT = 1;
    public static final int MONITOR_MODE_IN_OUT = 2;
    public static final int FENCE_TYPE_CIRCLE = 0;
    public static final int FENCE_TYPE_POLYGON = 1;
    private String mFenceName;
    private int mMonitorMode = MONITOR_MODE_IN_OUT;
    private int mMonitorStartMonth = 10;
    private int mMonitorStartDay = 1;
    private int mMonitorStartHour = 10;
    private int mMonitorStartMinute = 0;
    private int mMonitorEndMonth = 12;
    private int mMonitorEndDay = 31;
    private int mMonitorEndHour = 22;
    private int mMonitorEndMinute = 0;
    private int mFenceShape = FENCE_TYPE_CIRCLE;
    private int mFenceCircleRadius = 100;
    private int mFencePolygonPoints = -1;
    private double mFenceCenterLat = -1;
    private double mFenceCenterLng = -1;
    private String mDateInfo;

    private double[] mFencePolygonPointLats;
    private double[] mFencePolygonPointLngs;


    public Sate7Fence(String name) {
        mFenceName = name;
    }

    public Sate7Fence(String name, int monitorMode, int startMonth, int startDay, int startHour, int startMinute, int endHour, int endMinute, int endMonth, int endDay,int shape, int radio, double centerLat, double centerLng, int polygonPoints) {
        mFenceName = name;
        mMonitorMode = monitorMode;
        mMonitorStartMonth = startMonth;
        mMonitorStartDay = startDay;
        mMonitorStartHour = startHour;
        mMonitorStartMinute = startMinute;
        mMonitorEndHour = endHour;
        mMonitorEndMinute = endMinute;
        mMonitorEndMonth = endMonth;
        mMonitorEndDay = endDay;
        mFenceShape = shape;
        mFenceCircleRadius = radio;
        mFenceCenterLat = centerLat;
        mFenceCenterLng = centerLng;
        mFencePolygonPoints = polygonPoints;
    }

    protected Sate7Fence(Parcel in) {
        mFenceName = in.readString();
        mMonitorMode = in.readInt();
        mMonitorStartMonth = in.readInt();
        mMonitorStartDay = in.readInt();
        mMonitorStartHour = in.readInt();
        mMonitorStartMinute = in.readInt();
        mMonitorEndMonth = in.readInt();
        mMonitorEndDay = in.readInt();
        mMonitorEndHour = in.readInt();
        mMonitorEndMinute = in.readInt();
        mFenceShape = in.readInt();
        mFenceCircleRadius = in.readInt();
        mFencePolygonPoints = in.readInt();
        mFenceCenterLat = in.readDouble();
        mFenceCenterLng = in.readDouble();
        if (mFenceShape == Sate7Fence.FENCE_TYPE_POLYGON && mFencePolygonPoints >= 3) {
            mFencePolygonPointLats = new double[mFencePolygonPoints];
            mFencePolygonPointLngs = new double[mFencePolygonPoints];
            in.readDoubleArray(mFencePolygonPointLats);
            in.readDoubleArray(mFencePolygonPointLngs);
            XLog.d("Sate7Fence Parcel 333 ... " + mFencePolygonPointLngs + "," + mFencePolygonPointLats);
        }
    }

    public String getFenceName() {
        return mFenceName;
    }

    public void setFenceName(String mFenceName) {
        this.mFenceName = mFenceName;
    }

    public int getMonitorMode() {
        return mMonitorMode;
    }

    public void setMonitorMode(int mMonitorMode) {
        this.mMonitorMode = mMonitorMode;
    }

    public void setMonitorStartMonth(int startMonth) {
        this.mMonitorStartMonth = startMonth;
    }

    public void setMonitorStartDay(int startDay) {
        this.mMonitorStartMonth = startDay;
    }

    public int getMonitorStartMonth() {
        return mMonitorStartMonth;
    }

    public int getMonitorStartDay() {
        return mMonitorStartDay;
    }

    public void setMonitorEndMonth(int endMonth) {
        this.mMonitorEndMonth = endMonth;
    }

    public void setMonitorEndDay(int endDay) {
        this.mMonitorEndDay = endDay;
    }

    public int getMonitorEndMonth() {
        return mMonitorEndMonth;
    }

    public int getMonitorEndDay() {
        return mMonitorEndDay;
    }

    public int getMonitorStartHour() {
        return mMonitorStartHour;
    }

    public void setMonitorStartHour(int mMonitorStartHour) {
        this.mMonitorStartHour = mMonitorStartHour;
    }

    public void setDateInfo(String date) {
        XLog.dFenceDB("setTimeStamp ... ");
        this.mDateInfo = date;
    }

    public String getDateInfo() {
        return mDateInfo;
    }

    public int getMonitorStartMinute() {
        return mMonitorStartMinute;
    }

    public void setMonitorStartMinute(int mMonitorStartMinute) {
        this.mMonitorStartMinute = mMonitorStartMinute;
    }

    public int getMonitorEndHour() {
        return mMonitorEndHour;
    }

    public void setMonitorEndHour(int mMonitorEndHour) {
        this.mMonitorEndHour = mMonitorEndHour;
    }

    public int getMonitorEndMinute() {
        return mMonitorEndMinute;
    }

    public void setMonitorEndMinute(int mMonitorEndMinute) {
        this.mMonitorEndMinute = mMonitorEndMinute;
    }

    public int getFenceShape() {
        return mFenceShape;
    }

    public void setFenceShape(int mFenceShape) {
        this.mFenceShape = mFenceShape;
    }

    public int getFenceCircleRadius() {
        return mFenceCircleRadius;
    }

    public void setFenceCircleRadius(int mFenceCircleRadius) {
        this.mFenceCircleRadius = mFenceCircleRadius;
    }

    public void setFenceCenterLat(double centerLat) {
        mFenceCenterLat = centerLat;
    }

    public double getFenceCenterLat() {
        return mFenceCenterLat;
    }

    public void setFenceCenterLng(double centerLng) {
        mFenceCenterLng = centerLng;
    }

    public double getFenceCenterLng() {
        return mFenceCenterLng;
    }

    public int getFencePolygonPoints() {
        return mFencePolygonPoints;
    }

    public void setFencePolygonPoints(int mFencePolygonPoints) {
        this.mFencePolygonPoints = mFencePolygonPoints;
        mFencePolygonPointLats = new double[mFencePolygonPoints];
        mFencePolygonPointLngs = new double[mFencePolygonPoints];
        mIndex = 0;
    }

    private int mIndex = 0;

    public void addPolygonPointLatLng(LatLng points) {
        if (mFencePolygonPoints >= 3 &&
                (mFencePolygonPointLats == null || mFencePolygonPointLngs == null)) {
            mFencePolygonPointLats = new double[mFencePolygonPoints];
            mFencePolygonPointLngs = new double[mFencePolygonPoints];
        }
        mFencePolygonPointLats[mIndex] = points.latitude;
        mFencePolygonPointLngs[mIndex] = points.longitude;
        mIndex++;
    }

    public double[] getPolygonPointLats() {
        return mFencePolygonPointLats;
    }

    public double[] getPolygonPointLngs() {
        return mFencePolygonPointLngs;
    }

    public static final Creator<Sate7Fence> CREATOR = new Creator<Sate7Fence>() {
        @Override
        public Sate7Fence createFromParcel(Parcel in) {
            return new Sate7Fence(in);
        }

        @Override
        public Sate7Fence[] newArray(int size) {
            return new Sate7Fence[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFenceName);
        dest.writeInt(mMonitorMode);
        dest.writeInt(mMonitorStartMonth);
        dest.writeInt(mMonitorStartDay);
        dest.writeInt(mMonitorStartHour);
        dest.writeInt(mMonitorStartMinute);
        dest.writeInt(mMonitorEndMonth);
        dest.writeInt(mMonitorEndHour);
        dest.writeInt(mMonitorEndHour);
        dest.writeInt(mMonitorEndMinute);
        dest.writeInt(mFenceShape);
        dest.writeInt(mFenceCircleRadius);
        dest.writeInt(mFencePolygonPoints);
        dest.writeDouble(mFenceCenterLat);
        dest.writeDouble(mFenceCenterLng);
        if (mFenceShape == Sate7Fence.FENCE_TYPE_POLYGON &&
                mFencePolygonPointLngs != null && mFencePolygonPointLats != null &&
                mFencePolygonPointLngs.length >= 3 && mFencePolygonPointLats.length >= 3) {
            dest.writeDoubleArray(mFencePolygonPointLats);
            dest.writeDoubleArray(mFencePolygonPointLngs);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[").
                append("name=" + mFenceName).append(",monitorMode=" + mMonitorMode).append(",mMonitorStartMonth=" + mMonitorStartMonth).
                append(",mMonitorStartDay=" + mMonitorStartDay).
                append(",mMonitorStartHour=" + mMonitorStartHour).append(",mMonitorStartMinute=" + mMonitorStartMinute).
                append(",mMonitorEndMonth=" + mMonitorEndMonth).append(",mMonitorEndDay=" + mMonitorEndDay).
                append(",mMonitorEndHour=" + mMonitorEndHour).append(",mMonitorEndMinute=" + mMonitorEndMinute).
                append(",mFenceShape=" + mFenceShape).append(",mFenceCircleRadius=" + mFenceCircleRadius).append(",mFenceCenterLat=" + mFenceCenterLat).
                append(",mFenceCenterLng=" + mFenceCenterLng).append(",mFencePolygonPoints=" + mFencePolygonPoints).
                append(",mFencePolygonPointLngs=" + Arrays.toString(mFencePolygonPointLngs)).append(",mFencePolygonPointLats=" + Arrays.toString(mFencePolygonPointLats)).
                append("]");
        return builder.toString();
    }
}
