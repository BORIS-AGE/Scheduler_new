package com.example.boris.sheduler.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.boris.sheduler.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RepeatDate extends Fragment implements CheckBox.OnCheckedChangeListener {

    public CheckBox mn, tu, we, th, fr, st, sn;
    EditText editText;

    public RepeatDate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repeat_date, container, false);
        setValues(view);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mn.setChecked(false);
                tu.setChecked(false);
                we.setChecked(false);
                th.setChecked(false);
                fr.setChecked(false);
                st.setChecked(false);
                sn.setChecked(false);
            }
        });

        return view;
    }

    private void setValues(View view){
        mn = view.findViewById(R.id.checkBox);
        tu = view.findViewById(R.id.checkBox2);
        we = view.findViewById(R.id.checkBox3);
        th = view.findViewById(R.id.checkBox4);
        fr = view.findViewById(R.id.checkBox5);
        st = view.findViewById(R.id.checkBox6);
        sn = view.findViewById(R.id.checkBox7);
        editText = view.findViewById(R.id.numOfDays);

        mn.setOnCheckedChangeListener(this);
        tu.setOnCheckedChangeListener(this);
        we.setOnCheckedChangeListener(this);
        th.setOnCheckedChangeListener(this);
        fr.setOnCheckedChangeListener(this);
        st.setOnCheckedChangeListener(this);
        sn.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!editText.getText().toString().equals("")){
            editText.setText("");
            if (isChecked){
                buttonView.setChecked(true);
            }
        }
    }

    public int getRepeat(){
        if (!editText.getText().toString().equals("")){
            return Integer.parseInt(editText.getText().toString());
        }else{
            return -1;
        }
    }
}
