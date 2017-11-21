package com.example.aliyunlivedemo.live.push;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.aliyunlivedemo.util.LiveConfig;

/**
 * Created by ZhangXinmin on 2017/11/18.
 * Copyright (c) 2017 . All rights reserved.
 * 推流设置类
 */

public final class FlowPushRequestBuilder {

    //横竖屏
    public static final int SCREEN_PORTRAIT = 1;
    public static final int SCREEN_LANDSCAPE = 2;

    String rtmpUrl;//推流地址
    int videoResolution;//视频分辨率
    boolean screenOrientation;//是否横屏
    int cameraFacing;//前置摄像头
    String watermarkUrl;//水印地址
    int paddingX;//水印属性
    int paddingY;
    int waterLocation;//水印在画面中位置
    int bestBitrate;//最佳比特率
    int minBitrate;//最小比特率
    int maxBitrate;//最大比特率
    int initBitrate;
    int frameRate;//帧速率

    /**
     * 设置推流地址
     *
     * @param url
     * @return
     */
    public FlowPushRequestBuilder setRtmpUrl(String url) {
        this.rtmpUrl = url;
        return this;
    }

    /**
     * 设置视频分辨率
     *
     * @param resolution exp: AlivcMediaFormat.OUTPUT_RESOLUTION_480P
     * @return
     */
    public FlowPushRequestBuilder setVideoResolution(int resolution) {
        this.videoResolution = resolution;
        return this;
    }

    /**
     * 设置横竖屏
     *
     * @param screenOrientation if SCREEN_LANDSCAPE,横屏，否则 竖屏；
     * @return
     */
    public FlowPushRequestBuilder setScreenOrientation(int screenOrientation) {
        this.screenOrientation = (screenOrientation == SCREEN_LANDSCAPE);
        return this;
    }

    /**
     * 设置使用的摄像头
     *
     * @param cameraFacing exp:AlivcMediaFormat.CAMERA_FACING_FRONT
     * @return
     */
    public FlowPushRequestBuilder setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
        return this;
    }

    /**
     * 设置水印URL
     *
     * @param url
     * @return
     */
    public FlowPushRequestBuilder setWatermarkUrl(String url) {
        this.watermarkUrl = url;
        return this;
    }

    /**
     * 设置水印属性：X
     *
     * @param paddingX
     * @return
     */
    public FlowPushRequestBuilder setPaddingx(int paddingX) {
        this.paddingX = paddingX;
        return this;
    }

    /**
     * 设置水印属性：Y
     *
     * @param paddingy
     * @return
     */
    public FlowPushRequestBuilder setPaddingy(int paddingy) {
        this.paddingY = paddingy;
        return this;
    }

    /**
     * 设置水印在画面中的位置
     *
     * @param location 1:上右；2：上左；3：下右；4：下左
     * @return
     */
    public FlowPushRequestBuilder setWaterLocation(int location) {
        this.waterLocation = location;
        return this;
    }

    /**
     * 设置最佳比特率
     *
     * @param bestBitrate
     * @return
     */
    public FlowPushRequestBuilder setBestBitrate(int bestBitrate) {
        this.bestBitrate = bestBitrate;
        return this;
    }

    /**
     * 设置最小比特率
     *
     * @param minBitrate
     * @return
     */
    public FlowPushRequestBuilder setMinBitrate(int minBitrate) {
        this.minBitrate = minBitrate;
        return this;
    }

    /**
     * 设置最大比特率
     *
     * @param maxBitrate
     * @return
     */
    public FlowPushRequestBuilder setMaxBitrate(int maxBitrate) {
        this.maxBitrate = maxBitrate;
        return this;
    }

    /**
     * 设置初始比特率
     *
     * @param initBitrate
     * @return
     */
    public FlowPushRequestBuilder setInitBitrate(int initBitrate) {
        this.initBitrate = initBitrate;
        return this;
    }

    /**
     * 设置帧速率
     *
     * @param frameRate
     * @return
     */
    public FlowPushRequestBuilder setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        return this;
    }

    /**
     * 创建推流页面的Intent
     *
     * @param context
     * @return
     */
    public Intent createIntent(@Nullable Context context) {
        Intent intent = new Intent(context, LivePushFlowActivity.class);
        intent.putExtra(LiveConfig.URL.name(), rtmpUrl);
        intent.putExtra(LiveConfig.VIDEO_RESOLUTION.name(), videoResolution);
        intent.putExtra(LiveConfig.SCREEN_ORIENTATION.name(), screenOrientation);
        intent.putExtra(LiveConfig.CAMERA_FACING.name(), cameraFacing);
        intent.putExtra(LiveConfig.WATERMARK_URL.name(), watermarkUrl);
        intent.putExtra(LiveConfig.WATERMARK_PADDINGX.name(), paddingX);
        intent.putExtra(LiveConfig.WATERMARK_PADDINGY.name(), paddingY);
        intent.putExtra(LiveConfig.WATERMARK_SITE.name(), waterLocation);
        intent.putExtra(LiveConfig.BEST_BITRATE.name(), bestBitrate);
        intent.putExtra(LiveConfig.MIN_BITRATE.name(), minBitrate);
        intent.putExtra(LiveConfig.MAX_BITRATE.name(), maxBitrate);
        intent.putExtra(LiveConfig.INIT_BITRATE.name(), initBitrate);
        intent.putExtra(LiveConfig.FRAME_RATE.name(), frameRate);
        return intent;
    }
}
