package com.example.boris.sheduler;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boris.sheduler.Fragments.NoRepeat;
import com.example.boris.sheduler.Fragments.RepeatDate;
import com.example.boris.sheduler.Interfaces.PickTime;
import com.example.boris.sheduler.Managers.DataBaseHelper;
import com.example.boris.sheduler.Managers.TimePicker;
import com.example.boris.sheduler.Models.SheduleItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class AddSheduler extends AppCompatActivity implements PickTime, CompoundButton.OnCheckedChangeListener {

    Toolbar toolbar;
    private static final int PICK_IMAGE = 174;
    private Uri imageUri;
    private ImageView imageView;
    private TextView time_not, title_not, body_not;
    private int hour, min, year, month, dayOfMonth;
    private long date;
    private FrameLayout frameLayout;
    private Bundle savedInstanceState;
    private Switch mySwitch;
    private CompositeDisposable compositeDisposable = new CompositeDisposable(); // can add all observables to it and then delete all at once
    private Fragment currentFragment = null;
    private SheduleItem sheduleItem = null;
    private boolean mn = false, tu = false, we = false, th = false, fr = false, st = false, sn = false, repeat;
    private InputStream fls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sheduler);
        setDefaults();
        this.savedInstanceState = savedInstanceState;

        compositeDisposable.add(Observable.just(NoRepeat.class)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> setFragment(v)));


        date = new Date().getTime();
    }

    private void setDefaults() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        imageView = findViewById(R.id.select_image);
        time_not = findViewById(R.id.time_not);
        frameLayout = findViewById(R.id.fragment_repeat);
        mySwitch = findViewById(R.id.repeat);
        mySwitch.setOnCheckedChangeListener(this);
        title_not = findViewById(R.id.title_not);
        body_not = findViewById(R.id.description);
        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getApplicationContext().getResources().getResourcePackageName(R.drawable.pic3)
                + '/' + getApplicationContext().getResources().getResourceTypeName(R.drawable.pic3)
                + '/' + getApplicationContext().getResources().getResourceEntryName(R.drawable.pic3));

        //hide navigationBar
        hideBar();
    }

    private void hideBar() { // don't want to use
        /*int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }*/
    }

    private void setFragment(Class c) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (frameLayout != null) { // - if it is container is free
            if (savedInstanceState != null) {  // - must enter for it not to be destroyed
                return;
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            //NoRepeat b = new NoRepeat(); // - example of needed class
            Constructor<?> cons = null;
            try {
                cons = c.getConstructor();
                currentFragment = (Fragment) cons.newInstance();
                fragmentTransaction.replace(R.id.fragment_repeat, currentFragment, null); // - set class
                fragmentTransaction.commit(); // - push it
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void selectImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    public void changeTime(View view) {
        TimePicker timePicker = new TimePicker();
        timePicker.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //add menu to top
        getMenuInflater().inflate(R.menu.top_manu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //return result
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.subscribeNewItem:
                new myThread().run();
                return true;
        }

        return false;
    }

    private String recordImage() {
        Bitmap img = null;
        try {
            fls = getContentResolver().openInputStream(imageUri);
            byte[] image = new byte[fls.available()];
            fls.read(image);
            fls.close();
            img = BitmapFactory.decodeByteArray(image, 0, image.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/mySavesForScheduler/");
        dir.mkdirs();

        File file = new File(dir, time_not.getText().toString() + "-" + body_not.getText().toString());
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.PNG, 1, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getPath();
    }

    private void addSheduleItem() {
        String title, body, image;
        title = title_not.getText().toString();
        image = recordImage();
        if (title.equals("")) {
            Toast.makeText(this, "Change title   ", Toast.LENGTH_LONG).show();
            return;
        }
        body = body_not.getText().toString();
        repeat = mySwitch.isChecked();
        fls = null;


        if (!mySwitch.isChecked()) {
            NoRepeat noRepeat = (NoRepeat) currentFragment;
            Calendar calendar = Calendar.getInstance();

            year = noRepeat.getYear();
            month = noRepeat.getMonth();
            dayOfMonth = noRepeat.getDayOfMonth();

            calendar.set(year, month, dayOfMonth, hour, min);
            calendar.set(calendar.SECOND, 0);
            date = calendar.getTimeInMillis();
            if (!validateData(date)) {
                return;
            }
            addToDb(date, title, body, image, -1, repeat, false, false, false, false, false, false, false);
        } else {
            RepeatDate repeatDate = (RepeatDate) currentFragment;

            int repeatTimes = repeatDate.getRepeat();
            if (repeatTimes > 0) {
                date = getDateForRepating(repeatTimes);
                addToDb(date, title, body, image, repeatTimes, repeat, false, false, false, false, false, false, false);
            } else {
                mn = repeatDate.mn.isChecked();
                tu = repeatDate.tu.isChecked();
                we = repeatDate.we.isChecked();
                th = repeatDate.th.isChecked();
                fr = repeatDate.fr.isChecked();
                st = repeatDate.st.isChecked();
                sn = repeatDate.sn.isChecked();

                date = getDateForRepating(mn, tu, we, th, fr, st, sn);
                addToDb(date, title, body, image, -1, repeat, mn, tu, we, th, fr, st, sn);
            }
        }
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    private void addToDb(long time, String title, String body, String image, int frequency, boolean repeat, boolean mn, boolean tu, boolean we, boolean th, boolean fr, boolean st, boolean sn) {

        DataBaseHelper db = new DataBaseHelper(this);

        db.addContact(time, title, body, image, frequency, repeat, mn, tu, we, th, fr, st, sn);

    }

    private boolean validateData(long time) {
        if (new Date().getTime() > time) {
            Toast.makeText(this, "Change date", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private long getDateForRepating(int frequency) {
        Calendar c = Calendar.getInstance();
        if (c.get(c.HOUR) > hour && c.get(c.MINUTE) + 1 > min) {
            c.add(c.DATE, frequency);
        }
        c.set(c.get(c.YEAR), c.get(c.MONTH), c.get(c.DATE), hour, min, 0);

        return c.getTimeInMillis();
    }

    private long getDateForRepating(boolean mn, boolean tu, boolean we, boolean th, boolean fr, boolean st, boolean sn) {
        Calendar calendar = Calendar.getInstance();
        List<Integer> booleans = new ArrayList<>();

        if (mn) booleans.add(1);
        if (tu) booleans.add(2);
        if (we) booleans.add(3);
        if (th) booleans.add(4);
        if (fr) booleans.add(5);
        if (st) booleans.add(6);
        if (sn) booleans.add(7);

        boolean exit = true;
        //add day from this week
        for (int val : booleans) {
            if (calendar.get(calendar.DAY_OF_WEEK) - 1 < val && exit) {
                calendar.add(calendar.DATE, val - calendar.get(calendar.DAY_OF_WEEK) + 1);
                exit = false;
            }
            //if it is today, but later
            if (calendar.get(calendar.DAY_OF_WEEK) - 1 == val && exit && calendar.get(calendar.HOUR) < hour && calendar.get(calendar.MINUTE) + 1 < min) {
                exit = false;
            }
        }

        //add day from next week
        if (exit) {
            calendar.add(calendar.DATE, 7 - calendar.get(calendar.DAY_OF_WEEK) + 1 + booleans.get(0));
        }
        calendar.set(calendar.get(calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DATE), hour, min, 0);
        return calendar.getTimeInMillis();
    }

    //get data from fragment
    @Override
    public void pickData(int hour, int min) {
        this.hour = hour;
        this.min = min;
        if (min < 10) {
            time_not.setText(hour + " : 0" + min);
        } else {
            time_not.setText(hour + " : " + min);
        }
        //hide bar after calling widget
        hideBar();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();

        super.onDestroy();
    }

    //handle switch
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            compositeDisposable.add(Observable.just(RepeatDate.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v -> setFragment(v)));

        } else {
            compositeDisposable.add(Observable.just(NoRepeat.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v -> setFragment(v)));
        }
    }

    class myThread extends Thread {
        @Override
        public void run() {
            super.run();
            addSheduleItem();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}
