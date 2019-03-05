package com.example.boris.sheduler.Managers.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.example.boris.sheduler.Managers.Notifier;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationReceiver extends Worker {
    private Context context;

    public NotificationReceiver(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
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

    @NonNull
    @Override
    public Result doWork() {
        System.out.println("2 - done");
        Data data = getInputData();

        String title = data.getString("title");
        String body = data.getString("body");
        int id = data.getInt("id",0);
        new Notifier(context).run(title, body, id);

        if (isMyServiceRunning(BackgroungBrain.class)){
            Intent backgroundIntent = new Intent(context, BackgroungBrain.class);
            context.startService(backgroundIntent);
        }

        return Result.SUCCESS;
    }
}
