package com.example.carmusic;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carmusic.enums.BroadcastStatus;
import com.example.carmusic.enums.MusicStatus;

import java.util.Timer;
import java.util.TimerTask;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity {

    private ImageView lastMusicBtn;
    private ImageView playBtn;
    private ImageView pauseBtn;
    private ImageView nextMusicBtn;
    private ImageView volumeBtn;
    private TextView musicTitleTextview;
    private SeekBar timeSeekBar;
    private SeekBar volumeBar;
    private BroadcastReceiver infoReceiver;

    private Boolean changeProgress = false;
    private int tempVolume = 0;
    private Boolean mute = false;

    private AudioManager audioManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        registerMusicProgressReceiver();
        registerMusicTitleReceiver();

        // 注册音乐播放service
        Intent i = new Intent(this, MusicService.class);
        startService(i);
    }

    private void onPlayBtnClick(){
        sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus())).putExtra("status", MusicStatus.START.getStatus()));
    }

    private void onPauseBtnClick(){
        sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus())).putExtra("status", MusicStatus.PAUSE.getStatus()));
    }


    private void onMuteBtnClick(){
        if (mute){
            // 已静音后处理
            volumeBtn.setImageResource(R.drawable.volume);
            volumeBar.setProgress(tempVolume);
        }else {
            // 未静音处理
            volumeBtn.setImageResource(R.drawable.volume_mute);
            tempVolume = volumeBar.getProgress();
            volumeBar.setProgress(0);
        }

        mute = !mute;
    }

    /***
     * 初始化控件资源
     */
    private void initViews(){
        lastMusicBtn = findViewById(R.id.last_music_btn);
        playBtn = findViewById(R.id.music_play_btn);
        pauseBtn = findViewById(R.id.music_pause_btn);
        nextMusicBtn = findViewById(R.id.next_music_btn);
        volumeBtn = findViewById(R.id.volume_btn);
        timeSeekBar = findViewById(R.id.time_seekbar);
        volumeBar = findViewById(R.id.volume_bar);
        musicTitleTextview = findViewById(R.id.music_title_textview);

        playBtn.setOnClickListener(v -> onPlayBtnClick());
        pauseBtn.setOnClickListener(v -> onPauseBtnClick());
        volumeBtn.setOnClickListener(v -> onMuteBtnClick());

        initMusicProgressBar();
        initVolumeBar();
    }

    /***
     * 初始化音乐进度条
     */
    private void initMusicProgressBar(){
        // 进度条拖动功能
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changeProgress = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_TO.getStatus())).putExtra("position",seekBar.getProgress()));
                changeProgress = false;
            }
        });
    }

    /***
     * 初始化音量条
     */
    private void initVolumeBar(){

        audioManager = ((AudioManager) getSystemService(AUDIO_SERVICE));

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeBar.setMax(maxVolume);
        volumeBar.setProgress(currentVolume);

        // 监听音量条
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /***
     * 注册音乐进度同步广播
     */
    private void registerMusicProgressReceiver(){
        infoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_UPDATE.getStatus()))){
                    if(changeProgress){
                        return;
                    }
                    Bundle bundle = intent.getExtras();
                    timeSeekBar.setMax(bundle.getInt("total"));
                    timeSeekBar.setProgress(bundle.getInt("current"));
                }
            }
        };

        IntentFilter filter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_UPDATE.getStatus()));
        registerReceiver(infoReceiver, filter);
    }

    /***
     * 注册音乐标题信息同步广播
     */
    private void registerMusicTitleReceiver(){
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
    }


}