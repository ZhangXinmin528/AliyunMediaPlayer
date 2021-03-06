package com.example.aliyunlivedemo.app;

import android.app.Application;

import com.alivc.player.AliVcMediaPlayer;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
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
//        System.loadLibrary("gnustl_shared");
//        System.loadLibrary("qupai-media-thirdparty");
//        System.loadLibrary("qupai-media-jni");
//        ApplicationGlue.initialize(this);

        setupLeakCanary();
    }

    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }
}
