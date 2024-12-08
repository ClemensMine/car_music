package com.example.carmusic;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carmusic.enums.BroadcastStatus;
import com.example.carmusic.enums.MusicStatus;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity {

    private ImageView lastMusicBtn;
    private ImageView playBtn;
    private ImageView pauseBtn;
    private ImageView nextMusicBtn;
    private ImageView volumeBtn;
    private TextView musicTitleTextview;
    private BroadcastReceiver infoReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastMusicBtn = findViewById(R.id.last_music_btn);
        playBtn = findViewById(R.id.music_play_btn);
        pauseBtn = findViewById(R.id.music_pause_btn);
        nextMusicBtn = findViewById(R.id.next_music_btn);
        volumeBtn = findViewById(R.id.volume_btn);
        musicTitleTextview = findViewById(R.id.music_title_textview);


        // 注册音乐信息同步广播
        infoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_TITLE_UPDATE.getStatus()))){
                    String title = intent.getStringExtra("title");
                    musicTitleTextview.setText("当前正在播放："+title);
                }
            }
        };

        IntentFilter infoFilter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_TITLE_UPDATE.getStatus()));
        registerReceiver(infoReceiver, infoFilter);

        // 注册音乐播放service
        Intent i = new Intent(this, MusicService.class);
        startService(i);

        playBtn.setOnClickListener(v -> onPlayBtnClick());
        pauseBtn.setOnClickListener(v -> onPauseBtnClick());
    }

    private void onPlayBtnClick(){
        sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus())).putExtra("status", MusicStatus.START.getStatus()));
    }

    private void onPauseBtnClick(){
        sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus())).putExtra("status", MusicStatus.PAUSE.getStatus()));
    }


}