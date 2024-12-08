package com.example.carmusic.enums;

public enum MusicStatus {
    START(0,"播放"),
    PAUSE(1,"暂停"),
    END(2,"中止");

    private int status;
    private String des;

    MusicStatus(int status, String des) {
        this.status = status;
        this.des = des;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
