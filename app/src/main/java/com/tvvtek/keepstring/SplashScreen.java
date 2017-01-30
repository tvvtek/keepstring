package com.tvvtek.keepstring;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class SplashScreen extends Activity {

    private volatile boolean trigger = false;
    private Thread splashTread;
    StaticSettings staticSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        final SplashScreen sPlashScreen = this;

        // thread for displaying the SplashScreen
        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized(this){
                        wait(staticSettings.getTimeSplashScreen());
                    }

                } catch(InterruptedException e) {}
                finally {
                    if (!trigger){
                        finish();
                        Intent i = new Intent();
                        i.setClass(sPlashScreen, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else{}
                }
            }
        };
        splashTread.start();
    }

    //Function that will handle the touch
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            synchronized(splashTread){
                splashTread.notifyAll();
            }
        }
        return true;
    }

    public void splash_screen_close(View view) {
        trigger = true;
        Intent i = new Intent();
        i.setClass(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
