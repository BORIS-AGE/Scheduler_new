package com.example.boris.sheduler.Managers;

import android.content.Context;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.Toast;

import com.example.boris.sheduler.MainActivity;
import com.example.boris.sheduler.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class TimerOnMain{
    private Context context;
    private long time;
    private View view;
    private TextSwitcher h1, h2, m1, m2, s1, s2;
    private CompositeDisposable compositeDisposable;
    private List<Long> longs = new ArrayList<>();
    private MainActivity mainActivity;

    public TimerOnMain(Context context, long time, View view, CompositeDisposable compositeDisposable, MainActivity mainActivity) {
        this.context = context;
        this.time = time;
        this.view = view;
        this.compositeDisposable = compositeDisposable;
        this.mainActivity = mainActivity;
        setSwitchers();
    }

    private void setSwitchers(){
        h1 = view.findViewById(R.id.hour1);
        h2 = view.findViewById(R.id.hour2);
        m1 = view.findViewById(R.id.min1);
        m2 = view.findViewById(R.id.min2);
        s1 = view.findViewById(R.id.sec1);
        s2 = view.findViewById(R.id.sec2);
    }

    public void start(){ // here is error with time
        //Toast.makeText(context, TimeUnit.MILLISECONDS.toHours((time - new Date().getTime())) + "", Toast.LENGTH_SHORT).show();
        long count = TimeUnit.MILLISECONDS.toSeconds(time - System.currentTimeMillis());
        longs.add(-1L);
        longs.add(-1L);
        longs.add(-1L);
        longs.add(-1L);
        longs.add(-1L);
        longs.add(-1L);

        resetTimer();
        compositeDisposable.add(
                Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .repeat((int)count)
                .doOnComplete(() -> {
                    mainActivity.updateSheduleAndRecycleItems();
                })
                .subscribe(v -> {
                    resetTimer();
                })
        );
    }
    private void resetTimer(){
        long days = TimeUnit.MILLISECONDS.toDays((time - System.currentTimeMillis()));
        long hours = TimeUnit.MILLISECONDS.toHours((time - System.currentTimeMillis())) - TimeUnit.DAYS.toHours(days);
        long mins = TimeUnit.MILLISECONDS.toMinutes((time - System.currentTimeMillis())) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((time - System.currentTimeMillis())));
        //long sec = TimeUnit.MILLISECONDS.toSeconds((time - new Date().getTime())) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(mins);

        setDate(h1,h2, days, 0,1);

        setDate(m1, m2, hours, 2,3);

        setDate(s1, s2, mins, 4,5);
    }
    //set validated date
    private void setDate(TextSwitcher ts1, TextSwitcher ts2, long t, int a, int b){
        if (t > 9){
            long leftCeil = t / 10;
            long rightCeil = t - (t / 10)*10;
            if (!longs.get(a).equals(leftCeil)){
                ts1.setText(leftCeil + "");
                longs.remove(a); longs.add(a, leftCeil);
               // Toast.makeText(context, def1 + " - " + leftCeil, Toast.LENGTH_SHORT).show();
            }
            if (!longs.get(b).equals(rightCeil)){
                ts2.setText(rightCeil + "");
                longs.remove(b); longs.add(b, rightCeil);
            }

        }else{
            if (!longs.get(a).equals(0L)) {
                ts1.setText("0");
                longs.remove(a); longs.add(a, 0L);
            }
            if (!longs.get(b).equals(t)){
                ts2.setText(t + "");
                longs.remove(b); longs.add(b, t);
            }
        }
    }
}

//textSwircher.setText(...) - animation