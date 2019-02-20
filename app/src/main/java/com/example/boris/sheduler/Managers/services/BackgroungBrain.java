package com.example.boris.sheduler.Managers.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.example.boris.sheduler.Managers.DataBaseHelper;
import com.example.boris.sheduler.Managers.Notifier;
import com.example.boris.sheduler.Models.SheduleItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class BackgroungBrain extends IntentService {

    private final IBinder iBinder = new MyLocalBinder();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    public BackgroungBrain(String name) {
        super(name);
    }
    public BackgroungBrain() {
        super("scheduleService");
    }
    private Notifier notifier;

    @Override
    protected void onHandleIntent(Intent intent) {
        notifier = new Notifier(getApplicationContext());
        if (intent == null){
            continueTheShedule();
        }
        //for service not to close
        ((Runnable) () -> {
            while (true) {
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    //getting example of service
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }
    public class MyLocalBinder extends Binder {
        public BackgroungBrain getService() {
            return BackgroungBrain.this;
        }
    }

    public void setTimeOut(SheduleItem sheduleItem){
        cancelNotification(); // refresh value
        compositeDisposable.clear(); // refresh value

        long timeDelay = sheduleItem.time - new Date().getTime() - 1000;
        if (timeDelay < 2000){ // for errors
            DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
            dataBaseHelper.delete(sheduleItem.id);
            notifier.run(sheduleItem);
            return;
        }

        if (sheduleItem.repeat){ // it will happen once or not
            if (sheduleItem.frequency == -1){ // repeat by day of week
                repeatByDayOFWeek(sheduleItem, timeDelay);
            }else{ // repeat by frequent
                repeatByFrequency(sheduleItem, timeDelay);
            }
        }else{ // don't repeat
            happenOnlyOnce(sheduleItem, timeDelay);
        }
    }

    private void happenOnlyOnce(SheduleItem sheduleItem, long timeDelay){
        compositeDisposable.add(Observable.timer(timeDelay, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> {
                    DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
                    dataBaseHelper.delete(sheduleItem.id);
                    notifier.run(sheduleItem);
                })
                .subscribe());
        runNotification(sheduleItem);
    }
    private void repeatByFrequency(SheduleItem sheduleItem, long timeDelay){
        compositeDisposable.add(Observable.timer(timeDelay, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> {
                    DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
                    dataBaseHelper.delete(sheduleItem.id);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(sheduleItem.time);
                    calendar.add(calendar.DATE, sheduleItem.frequency);
                    addToDb(calendar.getTimeInMillis(), sheduleItem.title, sheduleItem.body, sheduleItem.image, sheduleItem.frequency, sheduleItem.repeat, false, false, false, false, false, false, false);
                    continueTheShedule();
                    notifier.run(sheduleItem);
                })
                .subscribe());
        runNotification(sheduleItem);
        }
    private void repeatByDayOFWeek(SheduleItem sheduleItem, long timeDelay){
        compositeDisposable.add(Observable.timer(timeDelay, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> {
                    DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
                    dataBaseHelper.delete(sheduleItem.id);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(sheduleItem.time);
                    List<Integer> booleans = new ArrayList<>();

                    if (sheduleItem.mn) booleans.add(1);
                    if (sheduleItem.tu) booleans.add(2);
                    if (sheduleItem.we) booleans.add(3);
                    if (sheduleItem.th) booleans.add(4);
                    if (sheduleItem.fr) booleans.add(5);
                    if (sheduleItem.st) booleans.add(6);
                    if (sheduleItem.sn) booleans.add(7);

                    boolean exit = true;
                    //add day from this week
                    for (int val : booleans) {
                        if (calendar.get(calendar.DAY_OF_WEEK) - 1 < val && exit) {
                            calendar.add(calendar.DATE, val - calendar.get(calendar.DAY_OF_WEEK) + 1);
                            exit = false;
                        }
                    }
                    //add day from next week
                    if (exit) {
                        calendar.add(calendar.DATE, 7 - calendar.get(calendar.DAY_OF_WEEK) + 1 + booleans.get(0));
                    }
                    addToDb(calendar.getTimeInMillis(), sheduleItem.title, sheduleItem.body, sheduleItem.image, -1, sheduleItem.repeat, sheduleItem.mn, sheduleItem.tu, sheduleItem.we, sheduleItem.th, sheduleItem.fr, sheduleItem.st, sheduleItem.sn);
                    continueTheShedule();
                    notifier.run(sheduleItem);
                })
                .subscribe());
        runNotification(sheduleItem);
    }

    private void addToDb(long time, String title, String body, String image, int frequency, boolean repeat, boolean mn, boolean tu, boolean we, boolean th, boolean fr, boolean st, boolean sn) {
        DataBaseHelper db = new DataBaseHelper(this);
        db.addContact(time, title, body, image, frequency, repeat, mn, tu, we, th, fr, st, sn);
    }
    private void runNotification(SheduleItem sheduleItem){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, sheduleItem.time, pendingIntent);
        }else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, sheduleItem.time, pendingIntent);
        }
    }
    private void cancelNotification(){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }
    public void continueTheShedule(){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
        List<SheduleItem> sheduleItems = dataBaseHelper.getSchedulers();
        if (sheduleItems.size() > 0){
            setTimeOut(sheduleItems.get(0));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
