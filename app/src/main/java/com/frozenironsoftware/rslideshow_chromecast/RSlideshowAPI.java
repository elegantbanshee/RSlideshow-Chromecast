package com.frozenironsoftware.rslideshow_chromecast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RSlideshowAPI {
    private static final String API_URL = "https://rslideshow.rolando.org";
    private static final String USER_AGENT = "com.frozenironsoftware.RSlideshow/1.0";
    public static JSONArray images = new JSONArray();
    public static int index = 0;
    public static JSONArray pages = new JSONArray();
    public static JSONArray subreddits = new JSONArray();

    public static void getImages() throws JSONException {

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

        String response;
        try {
            response = post(String.format("%s/api/data", API_URL), body);

        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        JSONObject data = new JSONObject(response);
        JSONArray images = data.getJSONArray("data");
        JSONArray pages = data.getJSONArray("after");

        for (int imageIndex = 0; imageIndex < images.length(); imageIndex++)
            RSlideshowAPI.images.put(images.getJSONObject(imageIndex));

        RSlideshowAPI.pages = pages;
    }

    private static String post(String url, String json) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, MediaType.get("text/plain; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("User-Agent", USER_AGENT)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private static void setSubreddits() {
        String[] subreddits = Storage.getSubreddits().split("[\\s+]");
        RSlideshowAPI.subreddits = new JSONArray();
        for (String subreddit : subreddits)
            RSlideshowAPI.subreddits.put(subreddit);
    }
}
