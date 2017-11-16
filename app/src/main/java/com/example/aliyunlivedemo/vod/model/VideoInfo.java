package com.example.aliyunlivedemo.vod.model;

import java.io.Serializable;

/**
 * Created by ZhangXinmin on 2017/11/13.
 * Copyright (c) 2017 . All rights reserved.
 */

public class VideoInfo implements Serializable {
    private int id;
    private String title;//title
    private String author;//作者
    private String link;//连接

    public VideoInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
