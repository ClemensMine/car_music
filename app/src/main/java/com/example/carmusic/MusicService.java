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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.carmusic.enums.BroadcastStatus;
import com.example.carmusic.enums.MusicStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private BroadcastReceiver receiver;
    private Timer progressUpdateTimer;

    private int currentIndex = 0;

    // 歌名+路径
    private final List<MusicEntity> musics = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        getAllMusic();
        initMusicStatusReceiver();
        initMusicProgressReceiver();
        initPlayNextReceiver();
        initPlayLastReceiver();
    }

    private void playNext(){
        if (currentIndex >= musics.size()) {
            Toast.makeText(this, "已全部播放完毕", Toast.LENGTH_SHORT).show();
            if(progressUpdateTimer != null){
                progressUpdateTimer.cancel();
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            currentIndex = 0; // 重置索引
            return;
        }

        MusicEntity music = musics.get(currentIndex);
        playMedia(music.getUri());
        sendMusicInfoBroadcast(music.getTitle());
        currentIndex++;
    }

    private void playPrevious() {
        if (currentIndex > 0) {
            currentIndex--; // 回退索引到上一首
        } else {
            currentIndex = musics.size() - 1; // 如果是第一首，则跳转到最后一首
        }

        MusicEntity music = musics.get(currentIndex);
        playMedia(music.getUri());
        sendMusicInfoBroadcast(music.getTitle());
    }

    private void playMedia(String uri){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer.setDataSource(this, Uri.parse(uri));
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                startUpdateTimer();
            });

            mediaPlayer.setOnCompletionListener(mp -> playNext());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("MusicService", "播放媒体时出错", e);
        }
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
                            playMedia(musics.get(currentIndex).getUri());
                            sendMusicInfoBroadcast(musics.get(currentIndex).getTitle());
                            break;

                        case 1:
                            mediaPlayer.pause();
                            progressUpdateTimer.cancel();
                            break;

                        case 2:
                            mediaPlayer.stop();
                            break;
                    }
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
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_TO.getStatus()))){
                    mediaPlayer.seekTo(intent.getIntExtra("position",0));
                }
            }
        };

        IntentFilter filter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_TO.getStatus()));
        registerReceiver(receiver, filter);
    }

    /***
     * 接收播放下一个广播
     */
    private void initPlayNextReceiver() {
        BroadcastReceiver playNextReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_PLAY_NEXT.getStatus()))) {
                    playNext();
                }
            }
        };

        IntentFilter filter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_PLAY_NEXT.getStatus()));
        registerReceiver(playNextReceiver, filter);
    }

    /***
     * 接收播放上一个广播
     */
    private void initPlayLastReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_PLAY_LAST.getStatus()))){
                    playPrevious();
                }
            }
        };

        IntentFilter filter = new IntentFilter(String.valueOf(BroadcastStatus.MUSIC_PLAY_LAST.getStatus()));
        registerReceiver(receiver, filter);
    }


    /***
     * 启动进度条更新时钟
     */
    private void startUpdateTimer(){
        if (progressUpdateTimer != null) {
            progressUpdateTimer.cancel();
        }
        progressUpdateTimer = new Timer();
        progressUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMusicProgressInfo();
            }
        }, 0, 1000); // 每秒更新一次
    }

    /***
     * 发送音乐播放数据
     */
    private void sendMusicProgressInfo(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                int duration = mediaPlayer.getDuration();
                int position = mediaPlayer.getCurrentPosition();

                Intent intent = new Intent(String.valueOf(BroadcastStatus.MUSIC_PROGRESS_UPDATE.getStatus()));
                intent.putExtra("total", duration);
                intent.putExtra("current", position);
                sendBroadcast(intent);
            } catch (IllegalStateException e) {
                Log.e("MusicService", "无法获取音乐进度信息", e);
            }
        }
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

    /***
     * 获得所有音乐
     */
    private void getAllMusic(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        );
        if (cursor != null){
            while (cursor.moveToNext()){
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String link = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                musics.add(new MusicEntity(id, name, link));
            }
            cursor.close();
        }
    }
}