package com.example.aliyunlivedemo.live.push.push_3_0;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.aliyunlivedemo.util.LiveConfig;

/**
 * Created by ZhangXinmin on 2017/11/23.
 * Copyright (c) 2017 . All rights reserved.
 * 阿里推流SDKv3.0版本参数配置工具
 */

public final class PushConfigBuilder {
    //推流地址
    private String rtmpUrl;
    //是否异步
    private boolean isAsync;
    //是否纯音频
    private boolean isAudioOnly;
    //屏幕方向
    private int screenOrientation;
    //摄像头
    private int cameraId;
    //是否打开闪光灯
    private boolean isFlashOn;

    /**
     * 设置推流地址
     *
     * @param rtmpUrl
     * @return
     */
    public PushConfigBuilder setRtmpUrl(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
        return this;
    }

    /**
     * 是否异步接口
     *
     * @param async
     * @return
     */
    public PushConfigBuilder setAsync(boolean async) {
        isAsync = async;
        return this;
    }

    /**
     * 是否纯音频
     *
     * @param audioOnly
     * @return
     */
    public PushConfigBuilder setAudioOnly(boolean audioOnly) {
        isAudioOnly = audioOnly;
        return this;
    }

    /**
     * 设置屏幕方向：横竖屏
     *
     * @param screenOrientation
     * @return
     */
    public PushConfigBuilder setScreenOrientation(int screenOrientation) {
        this.screenOrientation = screenOrientation;
        return this;
    }

    /**
     * 设置前后摄像头
     *
     * @param cameraId
     * @return
     */
    public PushConfigBuilder setCameraId(int cameraId) {
        this.cameraId = cameraId;
        return this;
    }

    /**
     * 是否打开闪光灯
     *
     * @param flashOn
     * @return
     */
    public PushConfigBuilder setFlashOn(boolean flashOn) {
        isFlashOn = flashOn;
        return this;
    }

    /**
     * create live push intent
     *
     * @param context
     * @return
     */
    public Intent createIntent(@Nullable Context context) {
        Intent intent = new Intent(context, LivePushFlowActivity2.class);
        Bundle bundle = new Bundle();
        bundle.putString(LiveConfig.LIVE_URL.name(), rtmpUrl);//推流地址
        bundle.putBoolean(LiveConfig.IS_ANSYC.name(), isAsync);//异步
        bundle.putBoolean(LiveConfig.IS_AUDIO_ONLY.name(), isAudioOnly);//纯音频
        bundle.putInt(LiveConfig.SCREEN_ORIENTATION.name(), screenOrientation);//屏幕方向
        bundle.putInt(LiveConfig.CAMERA_ID.name(), cameraId);//摄像头
        bundle.putBoolean(LiveConfig.IS_FLASH_ON.name(), isFlashOn);//闪光灯
        intent.putExtras(bundle);
        return intent;
    }


}
