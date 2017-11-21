package com.example.aliyunlivedemo.app;

import android.app.Application;

import com.alivc.player.AliVcMediaPlayer;
import com.duanqu.qupai.jni.ApplicationGlue;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 *
 * @author ZhangXinmin
 * @date 2017/11/10
 * Copyright (c) 2017 . All rights reserved.
 */

public class VideoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //init log
        Logger.addLogAdapter(new AndroidLogAdapter());

        //init aliMediaPlayer
        final String businessId = "";
        AliVcMediaPlayer.init(this, businessId);

//        //初始化推流
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("qupai-media-thirdparty");
        System.loadLibrary("qupai-media-jni");
        ApplicationGlue.initialize(this);
    }
}
