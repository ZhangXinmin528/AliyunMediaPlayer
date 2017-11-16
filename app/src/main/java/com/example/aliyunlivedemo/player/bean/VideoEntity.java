package com.example.aliyunlivedemo.player.bean;

/**
 * 视频信息类
 */

public final class VideoEntity implements IVideoEntity {

    private String videoTitle;

    private String videoPath;

    public VideoEntity() {
    }

    public VideoEntity(String videoPath) {
        this.videoPath = videoPath;
    }

    public VideoEntity(String videoTitle, String videoPath) {
        this.videoTitle = videoTitle;
        this.videoPath = videoPath;
    }


    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public String getVideoTitle() {
        return videoTitle;
    }

    @Override
    public String getVideoPath() {
        return videoPath;
    }

    @Override
    public String toString() {
        return "VideoEntity{" +
                "videoTitle='" + videoTitle + '\'' +
                ", videoPath='" + videoPath + '\'' +
                '}';
    }
}
