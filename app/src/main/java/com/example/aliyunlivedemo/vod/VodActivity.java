package com.example.aliyunlivedemo.vod;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.listener.SimpleOnSeekBarChangeListener;
import com.example.aliyunlivedemo.util.Formatter;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by ZhangXinmin on 2017/11/13.
 * Copyright (c) 2017 . All rights reserved.
 * 视频点播页面
 */

public class VodActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = VodActivity.class.getSimpleName();

    public static final String PARAMS_URL = "url";
    private static final int MSG_UPDATE = 100;

    private Context mContext;
    private SurfaceView mSurfaceView;
    private AliVcMediaPlayer mAliVcMediaPlayer;
    private SeekBar mVolumeSeekBar;
    private SeekBar mBrightSeekBar;
    private String mPlayUrl;//播放地址
    //静音按钮
    private RadioButton mOnRb;
    private RadioButton mOffRb;
    private float mSpeedValue;//播放速率
    private Button mPauseBtn;//暂停按钮
    //播放进度条
    private TextView mPositionTv;
    private TextView mDurationTv;
    private SeekBar mPlayProgress;
    private SimpleDateFormat simpleDateFormat;
    //更新进度
    private Handler mProgressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE) {
                showVideoProgressInfo();
            }
            return false;
        }
    });

    @Override
    protected Object setLayout() {
        return R.layout.activity_vod;
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
        mSurfaceView = findViewById(R.id.surfaceview_vod);
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

        //播放进度条
        mPlayProgress = findViewById(R.id.progress_vod);
        mPlayProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mPositionTv = findViewById(R.id.tv_vod_currentPosition);
        mDurationTv = findViewById(R.id.tv_vod_totalDuration);

        //操作按钮
        findViewById(R.id.btn_vod_start).setOnClickListener(this);
        findViewById(R.id.btn_vod_stop).setOnClickListener(this);
        mPauseBtn = findViewById(R.id.btn_vod_pause);
        mPauseBtn.setOnClickListener(this);
        findViewById(R.id.btn_vod_replay).setOnClickListener(this);

        //静音设置
        RadioGroup muteRg = findViewById(R.id.rg_mute_vod);
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
        mVolumeSeekBar = findViewById(R.id.seekbar_volume_vod);
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
        RadioGroup scaleRg = findViewById(R.id.rg_scaling_vod);
        scaleRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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

        //播放速率
        RadioGroup speedRg = findViewById(R.id.rg_vod_speed);
        speedRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.speed05:
                        mSpeedValue = 0.5f;
                        break;
                    case R.id.speed10:
                        mSpeedValue = 1.0f;
                        break;
                    case R.id.speed15:
                        mSpeedValue = 1.5f;
                        break;
                    case R.id.speed20:
                        mSpeedValue = 2.0f;
                        break;
                    default:
                        break;
                }
                if (mAliVcMediaPlayer != null && mSpeedValue != 0) {
                    mAliVcMediaPlayer.setPlaySpeed(mSpeedValue);
                }
            }
        });

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
        //设置播放进度
        mAliVcMediaPlayer.setPlaySpeed(1.0f);
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
                showVideoProgressInfo();
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
                showVideoProgressInfo();
                stopUpdateProgress();
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
//                updateBufferProgress(percent);
//                Log.e(TAG, "MediaPlayerBufferingUpdateListener..视频缓冲进度：" + percent);
            }
        });

        //播放器拖动监听
        mAliVcMediaPlayer.setSeekCompleteListener(new MediaPlayer.MediaPlayerSeekCompleteListener() {
            @Override
            public void onSeekCompleted() {
                Log.e(TAG, "MediaPlayerSeekCompleteListener..onSeekCompleted");
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_vod_start:
                startPlayVideo();
                break;
            case R.id.btn_vod_stop:
                stopPlayVideo();
                mPauseBtn.setText(getString(R.string.all_video_pause));
                break;
            case R.id.btn_vod_pause:
                if (mAliVcMediaPlayer.isPlaying()) {
                    mPauseBtn.setText(getString(R.string.all_video_resume));
                    pausePlayVideo();
                } else {
                    mPauseBtn.setText(getString(R.string.all_video_pause));
                    resumePlayVideo();
                }
                break;
            case R.id.btn_vod_replay:
                replay();
                showVideoProgressInfo();
                break;
            default:
                break;
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
     * 暂停播放
     */
    private void pausePlayVideo() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.pause();
            mPauseBtn.setText(getString(R.string.all_video_resume));
        }
    }

    /**
     * 继续播放
     */
    private void resumePlayVideo() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.play();
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

    /**
     * 展示进度信息
     *
     * @hide
     */
    private void showVideoProgressInfo() {
        final int currentProgress = mAliVcMediaPlayer.getCurrentPosition();
        final int duration = mAliVcMediaPlayer.getDuration();
        final int bufferProgress = mAliVcMediaPlayer.getBufferPosition();
        if (mAliVcMediaPlayer.isPlaying()) {
            mPositionTv.setText(Formatter.formatTime(currentProgress));
            mDurationTv.setText(Formatter.formatTime(duration));
            mPlayProgress.setMax(duration);
            mPlayProgress.setProgress(currentProgress);
            Log.e(TAG, "currentProgress:" + currentProgress +
                    "..bufferProgress:" + bufferProgress +
                    "..duration:" + duration);
            if (bufferProgress != 0) {
                mPlayProgress.setSecondaryProgress(bufferProgress);
            }
        }
        startUpdateProgress();
    }

    /**
     * 更新缓冲进度
     *
     * @param percent
     * @hide
     */
    private void updateBufferProgress(int percent) {
        if (mAliVcMediaPlayer != null) {
            final int duration = mAliVcMediaPlayer.getDuration();
            final int secondaryProgress = (int) (duration * percent * 1.0f / 100);
            mPlayProgress.setSecondaryProgress(secondaryProgress);
        }
    }

    /**
     * 开始更新
     *
     * @hide
     */
    private void startUpdateProgress() {
        if (mProgressHandler != null) {
            mProgressHandler.removeMessages(MSG_UPDATE);
            mProgressHandler.sendEmptyMessageDelayed(MSG_UPDATE, 1000);
        }
    }

    /**
     * 停止更新
     *
     * @hide
     */
    private void stopUpdateProgress() {
        if (mProgressHandler != null) {
            mProgressHandler.removeMessages(MSG_UPDATE);
        }
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

    @Override
    protected void onStop() {
        pausePlayVideo();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        destory();
        stopUpdateProgress();
        super.onDestroy();
    }
}
