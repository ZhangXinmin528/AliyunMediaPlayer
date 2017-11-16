package com.example.aliyunlivedemo.app;

import android.app.Application;

import com.alivc.player.AliVcMediaPlayer;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by ZhangXinmin on 2017/11/10.
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
    }
}
