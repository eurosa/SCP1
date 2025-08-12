package com.surg.scp;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.surg.scp.databinding.ActivitySplashScreenBinding;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PEACE on 3/30/2016.
 */
public class SplashScreen extends AppCompatActivity {
    private ActivitySplashScreenBinding binding;
    LinearLayout descimage, desctxt;
    Animation uptodown, downtoup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        descimage = binding.titleimage;
        desctxt = binding.titletxt;
        ImageView mLogo = binding.imageView2;

        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
        downtoup = AnimationUtils.loadAnimation(this, R.anim.downtoup);

        descimage.setAnimation(downtoup);
        desctxt.setAnimation(uptodown);

        RotateAnimation rotate = new RotateAnimation(0, 720,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(3000);
        rotate.setInterpolator(new LinearInterpolator());
        mLogo.startAnimation(rotate);

        Thread myThread = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(6000);
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }
}