package com.example.melody;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MelodyPlayerActivity extends AppCompatActivity {

    private AudioPlaybackService.LocalBinder binderRef;
    private boolean boundFlag = false;

    private TextView labelNow;
    private Button btnPlay, btnPause, btnStop, btnNext, btnPrev;

    private ServiceConnection svcConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binderRef = (AudioPlaybackService.LocalBinder) service;
            boundFlag = true;
            refreshUi();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundFlag = false;
            binderRef = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_melody);

        labelNow = findViewById(R.id.nowPlayingLabel);
        btnPlay = findViewById(R.id.playBtn);
        btnPause = findViewById(R.id.pauseBtn);
        btnStop = findViewById(R.id.stopBtn);
        btnNext = findViewById(R.id.nextBtn);
        btnPrev = findViewById(R.id.prevBtn);

        btnPlay.setOnClickListener(v -> {
            if (boundFlag) binderRef.getService().playCurrent();
            refreshUi();
        });

        btnPause.setOnClickListener(v -> {
            if (boundFlag) binderRef.getService().pausePlayback();
            refreshUi();
        });

        btnStop.setOnClickListener(v -> {
            if (boundFlag) binderRef.getService().stopPlayback();
            refreshUi();
        });

        btnNext.setOnClickListener(v -> {
            if (boundFlag) binderRef.getService().playNext();
            refreshUi();
        });

        btnPrev.setOnClickListener(v -> {
            if (boundFlag) binderRef.getService().playPrevious();
            refreshUi();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudioPlaybackService.class);
        startService(intent);
        bindService(intent, svcConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (boundFlag) {
            unbindService(svcConn);
            boundFlag = false;
        }
    }

    private void refreshUi() {
        if (!boundFlag || binderRef == null) {
            labelNow.setText("No service");
            return;
        }
        AudioPlaybackService svc = binderRef.getService();
        labelNow.setText("Now: " + svc.getCurrentTitle());
        btnPlay.setEnabled(!svc.isPlaying());
        btnPause.setEnabled(svc.isPlaying());
        btnStop.setEnabled(svc.isPrepared());
    }
}
