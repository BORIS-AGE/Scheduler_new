package com.example.boris.sheduler.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.boris.sheduler.Models.SheduleItem;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String TABE_NAME = "schedule";

    public DataBaseHelper(Context context){
        super(context, "NAME", null, 1);
        Log.d("database operations", "database created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "time BIGINT, " +
                "title text, " +
                "body text, " +
                "image text, " +
                "frequency INTEGER, " +
                "repeat TINYINT, " +
                "mn TINYINT, " +
                "tu TINYINT, " +
                "we TINYINT, " +
                "th TINYINT, " +
                "fr TINYINT, " +
                "st TINYINT, " +
                "sn TINYINT)");
        Log.d("database operations", "table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABE_NAME);
        onCreate(db);
        Log.d("database operations", "upgrade created");

    }

    public void addContact(long time, String title, String body, String image, int frequency, boolean repeat, boolean mn, boolean tu, boolean we, boolean th, boolean fr, boolean st, boolean sn){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("time", time);
        contentValues.put("title", title);
        contentValues.put("body", body);
        contentValues.put("image", image);
        contentValues.put("frequency", frequency);
        contentValues.put("repeat", repeat? 1 : 0);
        contentValues.put("mn", mn? 1 : 0);
        contentValues.put("tu", tu? 1 : 0);
        contentValues.put("we", we? 1 : 0);
        contentValues.put("th", th? 1 : 0);
        contentValues.put("fr", fr? 1 : 0);
        contentValues.put("st", st? 1 : 0);
        contentValues.put("sn", sn? 1 : 0);

        db.insert(TABE_NAME, null, contentValues);
        db.close();
    }

    synchronized public List<SheduleItem> getSchedulers(){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] required = {"id", "time", "title", "body", "image", "frequency", "repeat", "mn", "tu", "we", "th", "fr", "st", "sn"};
        Cursor cursor = db.query(TABE_NAME,required,null,null,null,null,"time");
        List<SheduleItem> sheduleItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            /*String id = Integer.toString(cursor.getInt(cursor.getColumnIndex("id")));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String email = cursor.getString(cursor.getColumnIndex("email"));
            */
            long time = cursor.getLong(cursor.getColumnIndex("time"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
            String image = cursor.getString(cursor.getColumnIndex("image"));
            int frequency = cursor.getInt(cursor.getColumnIndex("frequency"));
            int repeat = cursor.getInt(cursor.getColumnIndex("repeat"));
            int mn = cursor.getInt(cursor.getColumnIndex("mn"));
            int tu = cursor.getInt(cursor.getColumnIndex("tu"));
            int we = cursor.getInt(cursor.getColumnIndex("we"));
            int th = cursor.getInt(cursor.getColumnIndex("th"));
            int fr = cursor.getInt(cursor.getColumnIndex("fr"));
            int st = cursor.getInt(cursor.getColumnIndex("st"));
            int sn = cursor.getInt(cursor.getColumnIndex("sn"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));

            SheduleItem sheduleItem = new SheduleItem(id, time, title, body, image);
            sheduleItem.frequency = frequency;
            sheduleItem.repeat = repeat == 1;
            sheduleItem.setDayOfWeek(mn == 1, tu == 1, we == 1, th == 1, fr == 1, st == 1, sn == 1);
            sheduleItems.add(sheduleItem);
        }
        db.close();
        return sheduleItems;
    }

    public void update(int id, String name, String email){
        SQLiteDatabase database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email",email);

        database.update(TABE_NAME,contentValues,"id = " + id,null);
        database.close();
    }

    public void delete(int id){
        SQLiteDatabase database = this.getReadableDatabase();
        database.delete(TABE_NAME, "id = " + id, null);
        database.close();
    }
    public void deleteAll(){
        SQLiteDatabase database = this.getReadableDatabase();
        database.delete(TABE_NAME, null, null);
        database.close();
    }
}
