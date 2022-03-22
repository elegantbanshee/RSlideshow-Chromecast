package com.frozenironsoftware.rslideshow_chromecast;

import android.content.Context;
import android.content.SharedPreferences;

public class Storage {
    public static String getSubreddits() {
        SharedPreferences sharedPref = PlayThread.activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString("SUBREDDITS", "");
    }

    public static void setSubreddits(String text) {
        SharedPreferences sharedPref = PlayThread.activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("SUBREDDITS", text);
        editor.apply();
    }
}
