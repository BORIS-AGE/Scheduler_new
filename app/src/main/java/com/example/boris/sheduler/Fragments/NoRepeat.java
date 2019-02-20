package com.example.boris.sheduler.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.boris.sheduler.Managers.DatePicker;
import com.example.boris.sheduler.R;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoRepeat extends Fragment {

    private int year, month, dayOfMonth;
    private TextView textView;
    private FragmentManager fragmentManager;
    Button button;
    private static final int REQUEST_CODE_DATE = 002;

    public NoRepeat() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_repeat, container, false);

        textView = view.findViewById(R.id.calendarDate);
        button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker datePicker = new DatePicker();
                datePicker.setTargetFragment(NoRepeat.this, REQUEST_CODE_DATE);
                datePicker.show(getFragmentManager(), "datePicker");
            }
        });

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        textView.setText(year + "/" + (month + 1) + "/" + dayOfMonth);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DATE && resultCode == Activity.RESULT_OK) {
            year = data.getExtras().getInt("year");
            month = data.getExtras().getInt("month");
            dayOfMonth = data.getExtras().getInt("dayOfMonth");

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            textView.setText(year + "/" + (month + 1) + "/" + dayOfMonth);
        }
    }

    public int getYear() {
        return year;
    }
    public int getMonth(){return month;}
    public int getDayOfMonth(){return dayOfMonth;}

}
