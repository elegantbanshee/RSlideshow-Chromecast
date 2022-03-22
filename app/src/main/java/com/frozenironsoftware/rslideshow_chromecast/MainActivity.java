package com.frozenironsoftware.rslideshow_chromecast;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Thread
        Thread playThread = new PlayThread(this);
        playThread.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                PlayThread.moveLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                PlayThread.moveRight();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                PlayThread.handleSettingsButton();
                return true;
        }
        return false;
    }
}