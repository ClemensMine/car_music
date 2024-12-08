package com.example.carmusic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.carmusic.enums.BroadcastStatus;
import com.example.carmusic.enums.MusicStatus;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private BroadcastReceiver receiver;
    private Timer progressUpdateTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.oceanside);
        initMusicStatusReceiver();
        initMusicProgressReceiver();
    }

    /***
     * 广播发送音乐名
     * @param title 音乐名
     */
    private void sendMusicInfoBroadcast(String title){
        Intent intent = new Intent(String.valueOf(BroadcastStatus.MUSIC_TITLE_UPDATE.getStatus()));
        intent.putExtra("title", title);
        sendBroadcast(intent);
    }

    /***
     * 初始化音乐状态接收广播
     */
    private void initMusicStatusReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus()))){
                    int status = intent.getIntExtra("status",-1);
                    switch (status){
                        case 0:
                            mediaPlayer.start();
                            startUpdateTimer();
                            break;

                        case 1:
                            mediaPlayer.pause();
                            break;

                        case 2:
                            mediaPlayer.stop();
                            break;
                    }
                    sendMusicInfoBroadcast("Ocean");
                }
            }
        };

        IntentFilter filter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus()));
        registerReceiver(receiver, filter);
    }

    /***
     * 初始化音乐进度接收广播
     */
    private void initMusicProgressReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_UPDATE.getStatus()))){
                    sendMusicProgressInfo();
                }
            }
        };

        IntentFilter filter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_UPDATE.getStatus()));
        registerReceiver(receiver, filter);
    }

    /***
     * 启动进度条更新时钟
     */
    private void startUpdateTimer(){
        progressUpdateTimer = new Timer();
        progressUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMusicProgressInfo();
            }
        },0,100);
    }

    /***
     * 发送音乐播放数据
     */
    private void sendMusicProgressInfo(){
        Intent i = new Intent(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_UPDATE.getStatus()));
        Bundle bundle = new Bundle();
        bundle.putInt("total", mediaPlayer.getDuration());
        bundle.putInt("current", mediaPlayer.getCurrentPosition());
        i.putExtras(bundle);
        sendBroadcast(i);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }
}