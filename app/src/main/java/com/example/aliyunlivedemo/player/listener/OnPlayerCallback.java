package com.example.aliyunlivedemo.player.listener;

import android.media.MediaPlayer;

/**
 * 视频播放相关的回调
 */
public interface OnPlayerCallback {

    /**
     * 准备完成
     */
    void onPrepared();

    /**
     * 视频size变化
     */
    void onVideoSizeChanged(int width, int height);

    /**
     * 缓存更新变化
     *
     * @param percent 缓冲百分比
     */
    void onBufferingUpdate(int percent);

    /**
     * 播放完成
     */
    void onCompletion();

    /**
     * 视频错误
     *
     * @param what    错误类型
     *                <ul>
     *                <li>{@link MediaPlayer#MEDIA_ERROR_UNKNOWN}
     *                <li>{@link MediaPlayer#MEDIA_ERROR_SERVER_DIED}
     *                </ul>
     * @param message 特殊错误码
     *                <ul>
     *                <li>{@link MediaPlayer#MEDIA_ERROR_IO}
     *                <li>{@link MediaPlayer#MEDIA_ERROR_MALFORMED}
     *                <li>{@link MediaPlayer#MEDIA_ERROR_UNSUPPORTED}
     *                <li>{@link MediaPlayer#MEDIA_ERROR_TIMED_OUT}
     *                <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
     *                </ul>
     */
    void onError(int what, String message);

    /**
     * 视频加载状态变化
     *
     * @param isShow 是否显示loading
     */
    void onLoadingChanged(boolean isShow);

    /**
     * 视频状态变化
     */
    void onStateChanged(int curState);
}
