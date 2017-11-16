package com.example.aliyunlivedemo.player.bean;

import java.io.Serializable;

/**
 * 视频数据类
 * 请实现本接口
 */
public interface IVideoEntity extends Serializable {

    /**
     * 视频标题
     */
    String getVideoTitle();

    /**
     * 视频播放路径（本地或网络）
     */
    String getVideoPath();

}
