package com.example.boris.sheduler.Models;

public class SheduleItem {
        public long time;
        public String title, body;
        public String image;
        public boolean mn = false, tu = false, we = false, th = false, fr = false, st = false, sn = false, repeat;
        public int frequency = -1;
        public int id;

    public SheduleItem(int id, long time, String title, String body, String image) {
        this.time = time;
        this.title = title;
        this.body = body;
        this.image = image;
        this.id = id;
    }

    public void setDayOfWeek(boolean monday, boolean tuesday, boolean wednesday, boolean thurthday, boolean friday, boolean saturday, boolean sunday){
        mn = monday;
        tu = tuesday;
        we = wednesday;
        th = thurthday;
        fr = friday;
        st = saturday;
        sn = sunday;
    }
}
