package com.example.boris.sheduler.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.boris.sheduler.MainActivity;
import com.example.boris.sheduler.Managers.DataBaseHelper;
import com.example.boris.sheduler.Models.SheduleItem;
import com.example.boris.sheduler.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyHolder> {

    List<SheduleItem> sheduleItems;
    MainActivity mainActivity;

    public RecyclerAdapter(List<SheduleItem> sheduleItems, Context context, MainActivity mainActivity) {
        this.sheduleItems = sheduleItems;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    Context context;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        SheduleItem sheduleItem = sheduleItems.get(i);

        holder.title.setText(sheduleItem.title.toUpperCase());
        holder.description.setText(sheduleItem.body);

        Date date = new Date();
        date.setTime(sheduleItem.time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        holder.date.setText(sdf.format(date));

        holder.days.setText(getDays(sheduleItem.mn, sheduleItem.tu, sheduleItem.we, sheduleItem.th, sheduleItem.fr, sheduleItem.st, sheduleItem.sn));
        if (sheduleItem.frequency != -1){
            holder.days.setText("frequency: " + sheduleItem.frequency);
        }

        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(Uri.parse(sheduleItem.image).toString()),
                500, 500);
        holder.imageView.setImageBitmap(thumbnail);
    }

    @Override
    public int getItemCount() {
        return sheduleItems.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView title, description, date, days;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recycler_image);
            title = itemView.findViewById(R.id.recycler_title);
            description = itemView.findViewById(R.id.recycler_description);
            date = itemView.findViewById(R.id.recycler_date);
            days = itemView.findViewById(R.id.recycler_days);

            itemView.setOnLongClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, itemView);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()){
                        case R.id.deleteRecycler:
                            deleteRecycler(sheduleItems.get(getAdapterPosition()).id);
                            sheduleItems.remove(getAdapterPosition());
                            mainActivity.updateSheduleAndRecycleItems();
                            return true;
                    }
                    return false;
                });
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.recycler_menu, popupMenu.getMenu());
                popupMenu.show();
                return true;
            });
        }
    }

    private String getDays(boolean monday, boolean tuesday, boolean wednesday, boolean thurthday, boolean friday, boolean saturday, boolean sunday){
        String str = "";
        if (monday) str += "mn, ";
        if (tuesday) str += "tu, ";
        if (wednesday) str += "we, ";
        if (thurthday) str += "th, ";
        if (friday) str += "fr, ";
        if (saturday) str += "st, ";
        if (sunday) str += "sn, ";

        return str;
    }

    private void deleteRecycler(int id){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
        dataBaseHelper.delete(id);
    }
}
