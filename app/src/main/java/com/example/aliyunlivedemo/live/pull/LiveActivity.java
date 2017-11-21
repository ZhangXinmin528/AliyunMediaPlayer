package com.example.aliyunlivedemo.live.pull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.listener.SimpleOnSeekBarChangeListener;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by ZhangXinmin on 2017/11/9.
 * Copyright (c) 2017 . All rights reserved.
 */

public class LiveActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = LiveActivity.class.getSimpleName();

    public static final String PARAMS_URL = "url";
    private Context mContext;
    private SurfaceView mSurfaceView;
    private AliVcMediaPlayer mAliVcMediaPlayer;
    private SeekBar mVolumeSeekBar;
    private SeekBar mBrightSeekBar;
    private EditText mMaxBufferEt;
    private SimpleTextWatcher mSimpleTextWatcher;
    private String mPlayUrl;//播放地址
    private SimpleDateFormat simpleDateFormat;
    //静音按钮
    private RadioButton mOnRb;
    private RadioButton mOffRb;

    @Override
    protected Object setLayout() {
        return R.layout.activity_live;
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

        mSurfaceView = findViewById(R.id.surfaceview_live);
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

        //开始和暂停
        findViewById(R.id.btn_play_live).setOnClickListener(this);
        findViewById(R.id.btn_stop_live).setOnClickListener(this);

        //静音设置
        RadioGroup muteRg = findViewById(R.id.rg_mute_live);
        mOnRb = findViewById(R.id.muteOn);
        mOffRb = findViewById(R.id.muteOff);
        muteRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.e(TAG, "静音..setOnCheckedChangeListener..id：" + checkedId);
                switch (checkedId) {
                    case R.id.muteOn:
                        if (mAliVcMediaPlayer != null) {
                            mAliVcMediaPlayer.setMuteMode(true);
                            mVolumeSeekBar.setProgress(0);
                        }
                        break;
                    case R.id.muteOff:
                        if (mAliVcMediaPlayer != null) {
                            mAliVcMediaPlayer.setMuteMode(false);
                            mVolumeSeekBar.setProgress(mAliVcMediaPlayer.getVolume());
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        //音量设置
        mVolumeSeekBar = findViewById(R.id.seekbar_live);
        mVolumeSeekBar.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && mAliVcMediaPlayer != null) {
                    mAliVcMediaPlayer.setVolume(progress);
                    if (progress == 0) {//静音
                        mOnRb.setChecked(true);
                    } else {
                        mOffRb.setChecked(true);
                    }
                }
            }
        });

        //亮度调节
        mBrightSeekBar = findViewById(R.id.brightnessProgress);
        mBrightSeekBar.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && mAliVcMediaPlayer != null) {
                    mAliVcMediaPlayer.setScreenBrightness(progress);
                }
            }
        });

        //缩放模式
        RadioGroup scaleRg = findViewById(R.id.rg_scaling_live);
        scaleRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.e(TAG, "缩放模式..setOnCheckedChangeListener..id:" + checkedId);
                switch (checkedId) {
                    case R.id.fit:
                        if (mAliVcMediaPlayer != null) {
                            mAliVcMediaPlayer.setVideoScalingMode(
                                    MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                        }
                        break;
                    case R.id.fill:
                        if (mAliVcMediaPlayer != null) {
                            mAliVcMediaPlayer.setVideoScalingMode(
                                    MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        mMaxBufferEt = findViewById(R.id.max_buff_duration);
        mSimpleTextWatcher = new SimpleTextWatcher(this);
        mMaxBufferEt.addTextChangedListener(mSimpleTextWatcher);

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
        mVolumeSeekBar.setProgress(mAliVcMediaPlayer.getVolume());//音量
        mBrightSeekBar.setProgress(mAliVcMediaPlayer.getScreenBrightness());//亮度
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
     * 进行最大丢帧缓冲区设置
     */
    private void setMaxBufferCacheConfig() {
        final String maxBuffer = mMaxBufferEt.getText().toString().trim();
        try {
            final int maxBufferValue = Integer.parseInt(maxBuffer);
            if (maxBufferValue < 0) {
                Toast.makeText(mContext, getString(R.string.all_buffer_duration_nagtive),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAliVcMediaPlayer != null) {
                mAliVcMediaPlayer.setMaxBufferDuration(maxBufferValue);
            }

        } catch (Exception e) {
            mMaxBufferEt.setText("0");
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //开始
            case R.id.btn_play_live:
                setMaxBufferCacheConfig();
                //开始播放视频
                startPlayVideo();
                break;
            //停止
            case R.id.btn_stop_live:
                //停止播放视频
                stopPlayVideo();
                break;
            default:
                break;
        }
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
    protected void onResume() {
        super.onResume();
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


    private class SimpleTextWatcher implements TextWatcher {

        private WeakReference<LiveActivity> reference;

        public SimpleTextWatcher(LiveActivity activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            LiveActivity liveActivity = reference.get();
            if (liveActivity != null) {
                liveActivity.setMaxBufferCacheConfig();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
