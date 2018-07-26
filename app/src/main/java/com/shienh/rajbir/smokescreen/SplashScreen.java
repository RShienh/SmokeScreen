package com.shienh.rajbir.smokescreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class SplashScreen extends AppCompatActivity {

    int progress = 0;
    private RingProgressBar mRingProgressBar;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (progress < 100) {
                    progress = progress + 2;
                    mRingProgressBar.setProgress(progress);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomFont.replaceDefaultFont(this, "DEFAULT", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "MONOSPACE", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SERIF", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SANS_SERIF", "fonts/custom.ttf");
        setContentView(R.layout.activity_splash_screen);
        blink();

        mRingProgressBar = findViewById(R.id.progress_bar_1);
        mRingProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                Intent loginIntent = new Intent(SplashScreen.this, MainActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(100);
                        handler.sendEmptyMessage(0);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void blink() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeToBlink = 500;
                try {
                    Thread.sleep(timeToBlink);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView txt = findViewById(R.id.usage);
                        if (txt.getVisibility() == View.VISIBLE) {
                            txt.setVisibility(View.INVISIBLE);
                        } else {
                            txt.setVisibility(View.VISIBLE);
                        }
                        blink();
                    }
                });
            }
        }).start();
    }
}
