package com.example.boris.sheduler.Managers.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int id = intent.getIntExtra("id", 0);
        NotificationManager manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        manager.cancel(id); // - close notification from another activity
    }
}
