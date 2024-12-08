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

import com.example.carmusic.enums.BroadcastStatus;
import com.example.carmusic.enums.MusicStatus;

import java.io.File;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private BroadcastReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.oceanside);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(String.valueOf(BroadcastStatus.MUSIC_STATUS_UPDATE.getStatus()))){
                    int status = intent.getIntExtra("status",-1);
                    switch (status){
                        case 0:
                            mediaPlayer.start();
                            break;

                        case 1:
                            mediaPlayer.pause();
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendMusicInfoBroadcast("Ocean");
        return START_NOT_STICKY;
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

    private String getMusicTitle(){
        try(MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource("");
            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }
}