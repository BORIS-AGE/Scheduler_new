package com.example.boris.sheduler.Managers.services;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class NotificationReceiver extends BroadcastReceiver {
Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if (!isMyServiceRunning(BackgroungBrain.class)){
            context.startService(new Intent(context, BackgroungBrain.class));
        }
        IBinder binder = peekService(context, new Intent(context, BackgroungBrain.class));
        BackgroungBrain backgroungBrain = ((BackgroungBrain.MyLocalBinder) binder).getService();

        backgroungBrain.continueTheShedule();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
