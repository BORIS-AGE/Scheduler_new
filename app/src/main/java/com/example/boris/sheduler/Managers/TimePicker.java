package com.example.boris.sheduler.Managers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;

import com.example.boris.sheduler.Interfaces.PickTime;

import java.util.Calendar;

public class TimePicker extends DialogFragment  implements TimePickerDialog.OnTimeSetListener {
        private Bundle bundle = null;
        private PickTime pickTime;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bundle = savedInstanceState;
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        pickTime.pickData(hourOfDay,minute);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            pickTime = (PickTime) context;
        }
        catch (ClassCastException e) {
            Log.d("MyDialog", "Activity doesn't implement the PickTime interface");
        }
    }
}
