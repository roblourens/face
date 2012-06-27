package com.quail.face;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quail.face.activity.MainActivity;

public class OnAlarmReceiver extends BroadcastReceiver
{
    // don't have Notification.Builder until API 11
    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context c, Intent i)
    {
        Log.d("OnAlarmReceiver", "onReceive");
        NotificationManager nm = (NotificationManager) c
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification();
        n.icon = R.drawable.ic_refresh;
        n.tickerText = "Reminder: Take a picture with Face";
        n.when = System.currentTimeMillis();
        n.flags = Notification.FLAG_AUTO_CANCEL;

        Intent notificationIntent = new Intent(c, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
                notificationIntent, 0);

        n.setLatestEventInfo(c, "Face",
                "Take a picture with Face",
                contentIntent);

        nm.notify(10, n);
    }
}