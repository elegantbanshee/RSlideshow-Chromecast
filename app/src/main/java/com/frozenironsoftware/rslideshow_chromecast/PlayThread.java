package com.frozenironsoftware.rslideshow_chromecast;

import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayThread extends Thread {
    public static MainActivity activity;
    public static double lastRun = 0;
    private boolean getImages = false;

    public PlayThread(MainActivity activity) {
        PlayThread.activity = activity;
        setName("Play-Thread");
    }

    public static void moveRight() {
        lastRun = 0;
    }

    public static void moveLeft() {
        RSlideshowAPI.index -= 2;
        if (RSlideshowAPI.index < 0)
            RSlideshowAPI.index = 0;
        PlayThread.lastRun = 0;
    }

    public static void handleSettingsButton() {
        VideoView videoView = activity.findViewById(R.id.videoView);
        ImageView imageView = activity.findViewById(R.id.imageView);
        TextView titleView = activity.findViewById(R.id.titleView);
        EditText editTextSubredditsView = activity.findViewById(R.id.editTextSubreddits);

        if (editTextSubredditsView.getVisibility() == View.INVISIBLE) {
            videoView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            titleView.setVisibility(View.INVISIBLE);
            editTextSubredditsView.setVisibility(View.VISIBLE);
            editTextSubredditsView.setText(Storage.getSubreddits());
            editTextSubredditsView.bringToFront();
            lastRun = -1;
        }
        else {
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.VISIBLE);
            editTextSubredditsView.setVisibility(View.INVISIBLE);
            Storage.setSubreddits(editTextSubredditsView.getText().toString());
            RSlideshowAPI.index = 0;
            Thread thread = new Thread(() -> {
                try {
                    RSlideshowAPI.getImages();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            lastRun = 0;
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            RSlideshowAPI.getImages();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        activity.runOnUiThread(this::initViews);

        while (true) {
            if (System.currentTimeMillis() - lastRun >=  5000 && lastRun != -1) {
                activity.runOnUiThread(() -> {
                    try {
                        play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                lastRun = System.currentTimeMillis();
                if (getImages) {
                    try {
                        RSlideshowAPI.getImages();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getImages = false;
                }
            }
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
        VideoView videoView = activity.findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Point point = new Point();
                activity.getWindowManager().getDefaultDisplay().getRealSize(point);
                //videoView.getLayoutParams().width = mp.getVideoWidth() * point.x / point.y;
                //videoView.getLayoutParams().height = point.y;

                //videoView.setX(point.x / 2f - mp.getVideoWidth() / 2f);
                //videoView.setY(point.y / 2f - mp.getVideoHeight() / 2f);
            }
        });
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                lastRun = 0;
            }
        });

        EditText editTextSubredditsView = activity.findViewById(R.id.editTextSubreddits);
        editTextSubredditsView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER || i == KeyEvent.KEYCODE_BACK) {
                    handleSettingsButton();
                    return true;
                }
                return false;
            }
        });
    }

    private void play() throws JSONException {
        VideoView videoView = activity.findViewById(R.id.videoView);
        ImageView imageView = activity.findViewById(R.id.imageView);
        TextView titleView = activity.findViewById(R.id.titleView);

        if (RSlideshowAPI.images.length() == 0 ||
                RSlideshowAPI.images.length() == RSlideshowAPI.index)
            return;
        if (videoView.isPlaying())
            return;

        JSONObject image = RSlideshowAPI.images.getJSONObject(RSlideshowAPI.index);
        String imageUrl = image.getString("url");
        boolean isImage = !imageUrl.contains(".mp4");

        if (isImage) {
            videoView.stopPlayback();
            videoView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            Picasso.with(activity).load(imageUrl).into(imageView);
            imageView.bringToFront();
            titleView.bringToFront();
        }
        else {
            imageView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(imageUrl));
            videoView.start();

            //videoView.getLayoutParams().width = 1920;
            //videoView.getLayoutParams().height = 1080;
            //updateVideoPosition(imageUrl);

            videoView.bringToFront();
        }

        titleView.setText(String.format("r/%s | %s",
                image.getString("subreddit"),
                image.getString("title")));

        RSlideshowAPI.index++;
        if (RSlideshowAPI.index == RSlideshowAPI.images.length())
            getImages = true;
    }

    private void updateVideoPosition(String imageUrl) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(imageUrl);
        int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        VideoView videoView = activity.findViewById(R.id.videoView);
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        videoView.setX(point.x / 2f - width / 2f);
        retriever.release();
    }
}