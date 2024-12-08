package com.example.carmusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
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

public class SecondaryDisplayActivity extends AppCompatActivity {

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

    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_display);
        initViews();

        registerMusicProgressReceiver();
        registerMusicVolumeReceiver();
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

    private void onPlayBtnClick(){
        sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus())).putExtra("status", MusicStatus.START.getStatus()));
    }

    private void onPauseBtnClick(){
        sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus())).putExtra("status", MusicStatus.PAUSE.getStatus()));
    }


    private void onMuteBtnClick(){
        sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_VOLUME_TO.getStatus())).putExtra("volume", 0));
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
                sendBroadcast(new Intent(String.valueOf(BroadcastStatus.MUSIC_VOLUME_TO.getStatus())).putExtra("volume", progress));
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
     * 注册音乐音量同步广播
     */
    private void registerMusicVolumeReceiver(){
        infoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(String.valueOf(BroadcastStatus.MUSIC_VOLUME_TO.getStatus()))){
                    int volume = intent.getIntExtra("volume",0);

                    // 设置图片
                    if(volume == 0){
                        volumeBtn.setImageResource(R.drawable.volume_mute);
                    }else {
                        volumeBtn.setImageResource(R.drawable.volume);
                    }

                    volumeBar.setProgress(volume);
                }
            }
        };

        IntentFilter filter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_VOLUME_TO.getStatus()));
        registerReceiver(infoReceiver, filter);
    }

}