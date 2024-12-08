package com.example.carmusic.enums;

public enum BroadcastStatus {
    MUSIC_TITLE_UPDATE(1000),
    MUSIC_STATUS_UPDATE(1001),
    MUSIC_PROGRESS_UPDATE(1002),
    MUSIC_PROGRESS_TO(1003),
    MUSIC_VOLUME_TO(1004);

    private int status;

    BroadcastStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
