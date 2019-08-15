package com.sate7.geo.map.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.sate7.geo.map.R;

public class NotificationHelper {
    public static void showNotification(Context context, int chanelId, int icon, String tick, String title, String content, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(icon);
        builder.setTicker(tick);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("" + chanelId, title, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("" + chanelId);
            XLog.d("showNotification SDK_INT ... ");
        }
        Notification notification = builder.build();
        notificationManager.notify(chanelId, notification);
        XLog.d("showNotification ... ");
    }

    public static void showIntoFenceNf(Context context, int id, String fenceName, PendingIntent pendingIntent) {
        Resources res = context.getResources();
        showNotification(context, id, R.mipmap.in_fence,
                res.getString(R.string.into_fence_tick, fenceName),
                res.getString(R.string.into_fence_title),
                res.getString(R.string.into_fence_content, fenceName),
                pendingIntent);
    }

    public static void showExitFenceNf(Context context, int id, String fenceName, PendingIntent pendingIntent) {
        Resources res = context.getResources();
        showNotification(context, id, R.mipmap.out_fence,
                res.getString(R.string.out_fence_tick, fenceName),
                res.getString(R.string.out_fence_title),
                res.getString(R.string.out_fence_content, fenceName),
                pendingIntent);
    }
}
