package com.example.myapplication.musicplayer;

public class SongBean {
    private int resourceId;
    private String artist;
    private String title;
    private String filePath;

    // 用于raw资源的构造函数
    public SongBean(int resourceId, String artist, String title) {
        this.resourceId = resourceId;
        this.artist = artist;
        this.title = title;
    }

    // 用于文件路径的构造函数
    public SongBean(String filePath, String artist, String title) {
        this.filePath = filePath;
        this.artist = artist;
        this.title = title;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}
