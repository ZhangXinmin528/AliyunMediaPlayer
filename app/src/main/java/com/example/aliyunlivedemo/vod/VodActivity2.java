package com.example.aliyunlivedemo.vod;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.WindowManager;

import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.player.listener.OnAliPlayerControlListener;
import com.example.aliyunlivedemo.player.video.AliPlayerView;
import com.example.aliyunlivedemo.util.DisplayUtils;

/**
 * Created by ZhangXinmin on 2017/11/13.
 * Copyright (c) 2017 . All rights reserved.
 * 视频点播页面
 */

public class VodActivity2 extends BaseActivity implements OnAliPlayerControlListener {
    private static final String TAG = VodActivity2.class.getSimpleName();

    public static final String PARAMS_URL = "url";

    private Context mContext;
    private String mPlayUrl;//播放地址
    private AliPlayerView mAliPlayer;

    @Override
    protected Object setLayout() {
        return R.layout.activity_vod2;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;

        //获取参数
        Intent intent = getIntent();
        if (intent != null) {
            mPlayUrl = intent.getStringExtra(PARAMS_URL);
        }
    }

    @Override
    protected void initViews() {
        super.initViews();
        mAliPlayer = findViewById(R.id.aliplayer_vod);
        mAliPlayer.setOnPlayerControlListener(this);
        mAliPlayer.setVideoUrl(mPlayUrl);
    }

    @Override
    public void onStartPlay() {
        if (mAliPlayer != null) {
            mAliPlayer.startPlayVideo();
        }
    }

    @Override
    public void onBack() {

    }

    @Override
    public void onFullScreen() {
        DisplayUtils.toggleScreenOrientation(VodActivity2.this);
    }

    @Override
    public void onRetry(int errorStatus) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onBackPressed() {
        if (!DisplayUtils.isPortrait(this)) {
            if (!mAliPlayer.isLock()) {
                DisplayUtils.toggleScreenOrientation(this);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAliPlayer != null) {
            mAliPlayer.onDestroy();
        }
        super.onDestroy();
    }
}
