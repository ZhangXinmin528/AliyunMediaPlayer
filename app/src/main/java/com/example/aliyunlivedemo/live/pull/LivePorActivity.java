package com.example.aliyunlivedemo.live.pull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by ZhangXinmin on 2017/11/9.
 * Copyright (c) 2017 . All rights reserved.
 * 竖版全屏直播界面
 */

public class LivePorActivity extends BaseActivity {
    private static final String TAG = LivePorActivity.class.getSimpleName();

    public static final String PARAMS_URL = "url";
    private Context mContext;
    private SurfaceView mSurfaceView;
    private AliVcMediaPlayer mAliVcMediaPlayer;
    private String mPlayUrl;//播放地址
    private SimpleDateFormat simpleDateFormat;

    @Override
    protected Object setLayout() {
        return R.layout.activity_live_por;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void initParamsAndValues() {
        mContext = this;

        //获取参数
        Intent intent = getIntent();
        if (intent != null) {
            mPlayUrl = intent.getStringExtra(PARAMS_URL);
        }

        simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SS");
    }

    @Override
    protected void initViews() {
        super.initViews();

        mSurfaceView = findViewById(R.id.surfaceview_live_por);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.e(TAG, "SurfaceView..surfaceCreated");
                holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
                holder.setKeepScreenOn(true);

                //前后台切换需要重新设置Surface;部分手机锁屏也会做前后台切换
                if (mAliVcMediaPlayer != null) {
                    mAliVcMediaPlayer.setVideoSurface(holder.getSurface());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e(TAG, "SurfaceView..surfaceChanged");
                if (mAliVcMediaPlayer != null) {
                    mAliVcMediaPlayer.setSurfaceChanged();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e(TAG, "SurfaceView..surfaceDestroyed");
            }
        });


        //初始化player
        initMediaPlayer();

    }

    /**
     * init player
     *
     * @hide
     */
    private void initMediaPlayer() {

        mAliVcMediaPlayer = new AliVcMediaPlayer(mContext, mSurfaceView);
        //设置解码方式:移动设备建议硬解
        mAliVcMediaPlayer.setDefaultDecoder(1);
        //设置进度条
        //原比例填充
        mAliVcMediaPlayer.setVideoScalingMode(
                MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        //播放器就绪监听
        mAliVcMediaPlayer.setPreparedListener(new MediaPlayer.MediaPlayerPreparedListener() {
            @Override
            public void onPrepared() {
                Log.e(TAG, "MediaPlayerPreparedListener..onPrepared");
                Logger.d(TAG, "视频宽度：" + mAliVcMediaPlayer.getVideoWidth() +
                        "..视频高度：" + mAliVcMediaPlayer.getVideoHeight());
            }
        });

        //播放器调试接口
        mAliVcMediaPlayer.setFrameInfoListener(new MediaPlayer.MediaPlayerFrameInfoListener() {
            @Override
            public void onFrameInfoListener() {
                Map<String, String> debugInfo = mAliVcMediaPlayer.getAllDebugInfo();
                long createPts = 0;
                if (debugInfo.get("create_player") != null) {
                    String time = debugInfo.get("create_player");
                    createPts = (long) Double.parseDouble(time);
                    Log.e(TAG, "播放器创建成功：" + getTimeString(createPts));
                }
                if (debugInfo.get("open-url") != null) {
                    String time = debugInfo.get("open-url");
                    long openPts = (long) Double.parseDouble(time) + createPts;
                    Log.e(TAG, "Url请求成功：" + getTimeString(openPts));
                }
                if (debugInfo.get("find-stream") != null) {
                    String time = debugInfo.get("find-stream");
                    long findPts = (long) Double.parseDouble(time) + createPts;
                    Log.e(TAG, "数据流请求成功：" + getTimeString(findPts));
                }
                if (debugInfo.get("open-stream") != null) {
                    String time = debugInfo.get("open-stream");
                    long openPts = (long) Double.parseDouble(time) + createPts;
                    Log.e(TAG, "开始传输码流：" + getTimeString(openPts));
                }
                Log.e(TAG, "第一帧播放完成：" + getTimeString(System.currentTimeMillis()));
            }
        });

        //异常错误监听器
        mAliVcMediaPlayer.setErrorListener(new MediaPlayer.MediaPlayerErrorListener() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "MediaPlayerErrorListener..播放器错误：" + s);
                stopPlayVideo();
            }
        });

        //播放完成监听
        mAliVcMediaPlayer.setCompletedListener(new MediaPlayer.MediaPlayerCompletedListener() {
            @Override
            public void onCompleted() {
                Log.e(TAG, "MediaPlayerCompletedListener..播放完成");
            }
        });

        //视频大小改变监听接口
        mAliVcMediaPlayer.setVideoSizeChangeListener(new MediaPlayer.MediaPlayerVideoSizeChangeListener() {
            @Override
            public void onVideoSizeChange(int width, int height) {
                Log.e(TAG, "MediaPlayerVideoSizeChangeListener..画面大小变化");
            }
        });

        //视频缓冲监听接口
        mAliVcMediaPlayer.setBufferingUpdateListener(new MediaPlayer.MediaPlayerBufferingUpdateListener() {
            @Override
            public void onBufferingUpdateListener(int percent) {
//                Log.e(TAG, "MediaPlayerBufferingUpdateListener..视频缓冲进度：" + percent);
            }
        });

        //暂停播放监听接口
        mAliVcMediaPlayer.setStopedListener(new MediaPlayer.MediaPlayerStopedListener() {
            @Override
            public void onStopped() {
                Log.e(TAG, "MediaPlayerStopedListener..视频暂停播放");
            }
        });

        //视频播放信息监听接口
        mAliVcMediaPlayer.setInfoListener(new MediaPlayer.MediaPlayerInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                Log.e(TAG, "MediaPlayerInfoListener..视频播放信息：" + what + "..额外信息：" + extra);
            }
        });

        //打开底层开发，开发时使用
//        mAliVcMediaPlayer.enableNativeLog();

    }

    @Override
    protected void initData() {
        super.initData();
    }


    /**
     * 停止播放
     *
     * @hide
     */
    private void stopPlayVideo() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.stop();
        }
    }

    /**
     * 开始播放
     *
     * @hide
     */
    private void startPlayVideo() {
        if (mAliVcMediaPlayer != null) {
            if (!TextUtils.isEmpty(mPlayUrl)) {
                mAliVcMediaPlayer.prepareAndPlay(mPlayUrl);
            }
        }
    }

    /**
     * 暂停播放
     */
    private void pausePlayVideo() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.pause();
        }
    }

    /**
     * 重新播放
     */
    private void resumePlayVideo() {
        if (mAliVcMediaPlayer != null) {
            if (!mAliVcMediaPlayer.isPlaying()) {
                startPlayVideo();
            }
        }
    }

    /**
     * 销毁
     *
     * @hide
     */
    private void destory() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.stop();
            mAliVcMediaPlayer.destroy();
        }
    }

    /**
     * 重新播放
     */
    private void replay() {
        stopPlayVideo();
        startPlayVideo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPlayVideo();
    }

    @Override
    protected void onStop() {
        pausePlayVideo();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        destory();
        super.onDestroy();
    }


    /**
     * 获取时间字符串
     *
     * @param time
     * @return
     * @hide
     */
    private String getTimeString(long time) {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SS");
        }

        return simpleDateFormat.format(new Date(time));
    }

}
