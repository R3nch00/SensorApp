package com.example.melody;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AudioPlaybackService extends Service {

    public class LocalBinder extends Binder {
        AudioPlaybackService getService() {
            return AudioPlaybackService.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    private MediaPlayer player;
    private List<Integer> playlistIds = new ArrayList<>();
    private List<String> playlistTitles = new ArrayList<>();
    private int currentPos = 0;
    private boolean preparedFlag = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Predefined playlist using raw resources
        playlistIds.add(R.raw.track_a);
        playlistTitles.add("Track A");

        playlistIds.add(R.raw.track_b);
        playlistTitles.add("Track B");

        playlistIds.add(R.raw.track_c);
        playlistTitles.add("Track C");

        initPlayerFor(currentPos);
    }

    private void initPlayerFor(int index) {
        releasePlayer();
        player = MediaPlayer.create(this, playlistIds.get(index));
        preparedFlag = (player != null);
        if (player != null) {
            player.setOnCompletionListener(mp -> {
                // auto-advance on completion
                playNext();
            });
        }
    }

    public void playCurrent() {
        if (player == null && preparedFlag) initPlayerFor(currentPos);
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    public void pausePlayback() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    public void stopPlayback() {
        if (player != null) {
            player.stop();
            preparedFlag = false;
            // reinitialize so it can be played again
            initPlayerFor(currentPos);
        }
    }

    public void playNext() {
        currentPos = (currentPos + 1) % playlistIds.size();
        initPlayerFor(currentPos);
        playCurrent();
    }

    public void playPrevious() {
        currentPos = (currentPos - 1 + playlistIds.size()) % playlistIds.size();
        initPlayerFor(currentPos);
        playCurrent();
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public boolean isPrepared() {
        return preparedFlag;
    }

    public String getCurrentTitle() {
        return playlistTitles.get(currentPos);
    }

    private void releasePlayer() {
        if (player != null) {
            try {
                player.reset();
                player.release();
            } catch (Exception ignored) {}
            player = null;
            preparedFlag = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
