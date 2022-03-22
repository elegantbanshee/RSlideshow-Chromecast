package com.frozenironsoftware.rslideshow_chromecast;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RSlideshowAPI {
    private static final String API_URL = "https://rslideshow.rolando.org";
    public static JSONArray images = new JSONArray();
    public static int index = 0;
    private static JSONArray pages = new JSONArray();
    private static JSONArray subreddits = new JSONArray();

    public static void getImages() throws JSONException {
        Webb webb = Webb.create();
        Response<JSONObject> json;

        setSubreddits();

        if (subreddits.length() == 0)
            subreddits.put("popular");
        StringBuilder subredditsString = new StringBuilder();
        JSONArray newPages = new JSONArray();
        for (int subredditIndex = 0; subredditIndex < subreddits.length(); subredditIndex++) {
            if (subredditIndex > 0)
                subredditsString.append("+");
            subredditsString.append(subreddits.getString(subredditIndex));
            newPages.put("");
        }
        if (pages.length() == 0)
            pages = newPages;
        String pagesString = pages.toString();
        if (pages.length() == 0)
            pagesString = "";

        String body = String.format("%s;%s", subredditsString, pagesString);

        try {
            json = webb.post(String.format("%s/api/data", API_URL))
                    .body(body)
                    .asJsonObject();
        }
        catch (WebbException e) {
            e.printStackTrace();
            return;
        }
        JSONObject data = json.getBody();
        JSONArray images = data.getJSONArray("data");
        JSONArray pages = data.getJSONArray("after");

        for (int imageIndex = 0; imageIndex < images.length(); imageIndex++)
            RSlideshowAPI.images.put(images.getJSONObject(imageIndex));

        RSlideshowAPI.pages = pages;
    }

    private static void setSubreddits() {
        String[] subreddits = Storage.getSubreddits().split("[\\s+]");
        RSlideshowAPI.subreddits = new JSONArray();
        for (String subreddit : subreddits)
            RSlideshowAPI.subreddits.put(subreddit);
    }
}
