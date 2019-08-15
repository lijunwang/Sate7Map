package com.sate7.geo.map.util;

import android.util.Log;

public class XLog {
    private static final boolean debugPermission = true;
    private static final boolean debugLocation = true;
    private static final boolean debugDatabase = true;
    private static final boolean debugOffline = true;
    private static final boolean d = true;
    private static final String TagDebugPermission = "DebugPermission";
    private static final String TagDebugLocation = "DebugLocation";
    private static final String TagDebugD = "XLogNormal";
    private static final String TagDebugDatabase = "DebugDatabase";
    private static final String TagDebugOffline = "DebugOffline";

    public static void dPermission(String msg) {
        if (debugPermission) {
            Log.d(TagDebugPermission, "" + msg);
        }
    }

    public static void dLocation(String msg) {
        if (debugLocation) {
            Log.d(TagDebugLocation, "" + msg);
        }
    }

    public static void d(String msg) {
        if (d) {
            Log.d(TagDebugD, "" + msg);
        }
    }

    public static void e(String msg) {
        Log.d("Error", "" + msg);
    }

    public static void dFenceDB(String msg) {
        if (debugDatabase) {
            Log.d(TagDebugDatabase, "" + msg);
        }
    }

    public static void dOffline(String msg) {
        if (debugOffline) {
            Log.d(TagDebugOffline, "" + msg);
        }
    }
}
