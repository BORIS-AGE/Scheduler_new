package com.example.boris.sheduler;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boris.sheduler.Adapters.RecyclerAdapter;
import com.example.boris.sheduler.Managers.Animations;
import com.example.boris.sheduler.Managers.DataBaseHelper;
import com.example.boris.sheduler.Managers.TimerOnMain;
import com.example.boris.sheduler.Managers.services.BackgroungBrain;
import com.example.boris.sheduler.Models.SheduleItem;

import java.util.List;

import androidx.work.WorkManager;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity  {

    private TextView title;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<SheduleItem> sheduleItems;
    private static final int MY_PERMISSION = 371;
    private TextSwitcher h1, h2, m1, m2, s1, s2;
    private BackgroungBrain backgroungBrain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDefaults();

        startMyService();

    }

    public void updateSheduleAndRecycleItems(){
        compositeDisposable.clear(); // delete last
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
        compositeDisposable.add(Observable.just(dataBaseHelper)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> setRecycler())
                .subscribe(v -> {
                    sheduleItems = v.getSchedulers();
                    if (sheduleItems.size() > 0) {
                        backgroungBrain.setTimeOut(sheduleItems.get(0));
                        title.setText(new String(sheduleItems.get(0).title).toUpperCase() + "\n" + sheduleItems.get(0).body);
                    } else {
                        title.setText("title");
                        h1.setText("0"); h2.setText("0"); m1.setText("0"); m2.setText("0"); s1.setText("0"); s2.setText("0");
                    }
                })
        );
    }

    private void setRecycler() {
        //add observers to CompositeDisposable
        compositeDisposable.add(Observable.fromArray(sheduleItems)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    if (sheduleItems.size() > 0)
                    new TimerOnMain(this,sheduleItems.get(0).time,getWindow().getDecorView().getRootView(), compositeDisposable, this).start();
                }) // start timer
                .subscribe(
                        v -> {
                            RecyclerAdapter adapter = new RecyclerAdapter(v,getApplicationContext(), this);
                            recyclerView.setAdapter(adapter);
                        }
                ));
    }

    private void setDefaults(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.titleText);
        recyclerView = findViewById(R.id.recycler);

        h1 = findViewById(R.id.hour1);
        h2 = findViewById(R.id.hour2);
        m1 = findViewById(R.id.min1);
        m2 = findViewById(R.id.min2);
        s1 = findViewById(R.id.sec1);
        s2 = findViewById(R.id.sec2);

        Animations animations = new Animations();
        animations.animateText(h1, this);
        animations.animateText(h2, this);
        animations.animateText(m1, this);
        animations.animateText(m2, this);
        animations.animateText(s1, this);
        animations.animateText(s2, this);

        //hide navigationBar
        /*int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //add menu to top
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.addNewItem:
                Intent intent = new Intent(getApplicationContext(), AddSheduler.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent2);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        compositeDisposable.clear();
        unbindService(serviceConnection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if ( sheduleItems != null && sheduleItems.size() > 0){
            Intent backgroundIntent = new Intent(this, BackgroungBrain.class);
            bindService(backgroundIntent, serviceConnection, 0);
            startService(backgroundIntent);
        }
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission garanted", Toast.LENGTH_SHORT).show();
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    updateSheduleAndRecycleItems();
                }else{
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public void startMyService(){
        Intent backgroundIntent = new Intent(this, BackgroungBrain.class);
        bindService(backgroundIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(backgroundIntent);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroungBrain.MyLocalBinder binder = (BackgroungBrain.MyLocalBinder)service;
            backgroungBrain = binder.getService();

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION);
            }else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.VIBRATE}, MY_PERMISSION);
            }else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION);
            }else{
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    updateSheduleAndRecycleItems();
                }
            }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            backgroungBrain = null;
        }
    };



}
