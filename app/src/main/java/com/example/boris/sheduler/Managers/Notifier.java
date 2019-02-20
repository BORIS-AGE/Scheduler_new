package com.example.boris.sheduler.Managers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.boris.sheduler.MainActivity;
import com.example.boris.sheduler.Managers.services.NotificationListener;
import com.example.boris.sheduler.Models.SheduleItem;
import com.example.boris.sheduler.R;

public class Notifier {
    private final String CHANNEL_ID = "personal_notifications";  // - create first constant
    private int iterator = 0;
    private Context context;
    private NotificationCompat.Builder builder;

    public Notifier(Context context) {
        this.context = context;
    }

    private void prepare(){
        createNotification();

        Intent landing = new Intent(context, MainActivity.class);  // - creating intent for new activity after click
        landing.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // - set flags

        PendingIntent pending = PendingIntent.getActivity(context, 0, landing, PendingIntent.FLAG_ONE_SHOT); // - creating new activity on clicking notification

        builder = new NotificationCompat.Builder(context, CHANNEL_ID); //  - create main bilder
        builder.setSmallIcon(R.drawable.ic_feedback_black_24dp); //  - set img
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true); //  - close after click
        builder.setContentIntent(pending); // - set activity on clicking notification


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        builder.setSound(Uri.parse(prefs.getString("notifications_new_message_ringtone","content://settings/system/notification_sound")));
        builder.setLights(Color.RED, 300, 100); //doesn't work
        if (prefs.getBoolean("notifications_new_message_vibrate",true)){
            Vibrator v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 400 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
        }

        builder.setOngoing(true);
    }

    public void run(SheduleItem sheduleItem){
        prepare();

        Intent intent = new Intent(context, NotificationListener.class);
        intent.putExtra("id", sheduleItem.id);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0,intent,0);
        builder.addAction(R.drawable.ic_ok, "Done", pendingIntent2); // - add button with activity and image, image doesn't work on emulator

        builder.setContentTitle(sheduleItem.title);
        builder.setContentText(sheduleItem.body);
        NotificationManagerCompat compat = NotificationManagerCompat.from(context);
        compat.notify(sheduleItem.id, builder.build());  //  - run notification ( has to be applied channel if version is > 8.0)
    }


    private void createNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //  - if android version > 8.0
            CharSequence name = "name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name,importance);
            channel.setDescription(description);
            NotificationManager manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

}
