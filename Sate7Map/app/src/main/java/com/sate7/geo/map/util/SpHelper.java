package com.sate7.geo.map.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Map;

public class SpHelper {
    private static final String ShowFenceHint = "showFenceHint";
    private static final String CreateFenceLastSure = "last_sure";

    private static Context context;

    public static void init(Context ctx) {
        context = ctx;
    }

    public static boolean setNoHintFence() {
        if (context == null) {
            throw new RuntimeException("You should init SpHelper in you Application.onCreate()");
        }
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit().putBoolean(ShowFenceHint, false).commit();
    }

    public static boolean needShowFenceHint() {
        if (context == null) {
            throw new RuntimeException("You should init SpHelper in you Application.onCreate()");
        }
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getBoolean(ShowFenceHint, true);
    }

    public static boolean needLastSure() {
        if (context == null) {
            throw new RuntimeException("You should init SpHelper in you Application.onCreate()");
        }
        Map test = PreferenceManager.getDefaultSharedPreferences(context).getAll();
        XLog.d("test == " + test);
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(CreateFenceLastSure, false);
    }

}
