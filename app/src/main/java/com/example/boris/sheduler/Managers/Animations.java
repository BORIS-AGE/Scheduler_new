package com.example.boris.sheduler.Managers;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.boris.sheduler.R;

public class Animations {

    public void animateText(TextSwitcher textSwitcher, Context context){
        //creating new text
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(context);
                textView.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
                textView.setTextSize(30);
                return textView;
        }});

        //setting animations ot hte text
        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(context,
                R.anim.side_up));
        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_down));
    }
}
