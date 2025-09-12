package com.surg.scp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

public class IncrementDecrementSlider extends LinearLayout {

    private Slider slider;
    private Button btnIncrement, btnDecrement;

    public IncrementDecrementSlider(Context context) {
        super(context);
        init(context);
    }
    public void configureSteps(int steps, float stepSize, float startValue) {
        float valueTo = startValue + (steps * stepSize);
        slider.setValueFrom(startValue);
        slider.setValueTo(valueTo);
        slider.setStepSize(stepSize);
        slider.setValue(startValue);
    }

    public IncrementDecrementSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IncrementDecrementSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_increment_decrement_slider, this, true);

        slider = findViewById(R.id.slider2);
        btnIncrement = findViewById(R.id.btnIncrement);
        btnDecrement = findViewById(R.id.btnDecrement);

        btnIncrement.setOnClickListener(v -> increment());
        btnDecrement.setOnClickListener(v -> decrement());
    }

    public void increment() {
        float step = slider.getStepSize() > 0 ? slider.getStepSize() : 1;
        if (slider.getValue() + step <= slider.getValueTo()) {
            slider.setValue(slider.getValue() + step);
        }
    }

    public void decrement() {
        float step = slider.getStepSize() > 0 ? slider.getStepSize() : 1;
        if (slider.getValue() - step >= slider.getValueFrom()) {
            slider.setValue(slider.getValue() - step);
        }
    }

    public float getValue() {
        return slider.getValue();
    }

    public void setValue(float value) {
        if (value >= slider.getValueFrom() && value <= slider.getValueTo()) {
            slider.setValue(value);
        }
    }

    public Slider getSlider() {
        return slider;
    }
}