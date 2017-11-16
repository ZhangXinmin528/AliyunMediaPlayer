package com.example.aliyunlivedemo.player.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;
import com.example.aliyunlivedemo.player.listener.OnPlayerCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * AliVcMediaPlayer的初始化工作，监听器的配置
 *
 * @author zhangxinmin
 */
public final class AliPlayerBuilder {
    private static final String TAG = AliPlayerBuilder.class.getSimpleName();

    //出错状态
    public static final int STATE_ERROR = -1;
    //通常状态
    public static final int STATE_IDLE = 0;
    //视频正在准备
    public static final int STATE_PREPARING = 1;
    //视频已经准备好
    public static final int STATE_PREPARED = 2;
    //视频正在播放
    public static final int STATE_PLAYING = 3;
    //视频暂停
    public static final int STATE_PAUSED = 4;
    //视频播放完成
    public static final int STATE_PLAYBACK_COMPLETED = 5;

    //AliVcMediaPlayer
    private AliVcMediaPlayer mAliVcMediaPlayer;
    // 当前状态
    private int curState = STATE_IDLE;
    // 当前缓冲进度
    private int currentBufferPercentage;
    // 视频的播放路径
    private String mUrl;
    // 播放监听
    private OnPlayerCallback onPlayerCallback;
    // 播放视频承载的view
    private SurfaceHolder surfaceHolder;

    private Context mContext;
    private SurfaceView mSurfaceView;
    private SimpleDateFormat mSimpleDateFormat;

    public AliPlayerBuilder(Context context, SurfaceView view) {
        this.mContext = context;
        this.mSurfaceView = view;
        initParams();

    }

    //init params
    private void initParams() {
        mUrl = "";
        mAliVcMediaPlayer = new AliVcMediaPlayer(mContext, mSurfaceView);
        //设置解码方式:移动设备建议硬解
        mAliVcMediaPlayer.setDefaultDecoder(1);
        //设置缩放模式
        mAliVcMediaPlayer.setVideoScalingMode(
                MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        //配置监听器
        reset();
        //播放器就绪监听
        mAliVcMediaPlayer.setPreparedListener(new MediaPlayer.MediaPlayerPreparedListener() {
            @Override
            public void onPrepared() {
                setCurrentState(STATE_PREPARED);
                if (onPlayerCallback != null) {
                    onPlayerCallback.onPrepared();
                    Log.e(TAG, "MediaPlayerPreparedListener..onPrepared");
                }
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

        //视频播放信息监听接口
        mAliVcMediaPlayer.setInfoListener(new MediaPlayer.MediaPlayerInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                Log.e(TAG, "MediaPlayerInfoListener..视频播放信息：" + what + "..额外信息：" + extra);
            }
        });

        // 出错监听
        mAliVcMediaPlayer.setErrorListener(new MediaPlayer.MediaPlayerErrorListener() {
            @Override
            public void onError(int i, String msg) {
                Log.e(TAG, "MediaPlayerErrorListener..播放器错误：" + msg);
                _stopPlayVideo();//停止播放
                setCurrentState(STATE_ERROR);
                if (onPlayerCallback != null) {
                    onPlayerCallback.onError(i, msg);
                }
            }
        });

        //播放完成监听
        mAliVcMediaPlayer.setCompletedListener(new MediaPlayer.MediaPlayerCompletedListener() {
            @Override
            public void onCompleted() {
                Log.e(TAG, "MediaPlayerCompletedListener..播放完成");
                if (onPlayerCallback != null) {
                    onPlayerCallback.onCompletion();
                }
                setCurrentState(STATE_PLAYBACK_COMPLETED);
            }
        });

        //视频缓冲监听接口
        mAliVcMediaPlayer.setBufferingUpdateListener(new MediaPlayer.MediaPlayerBufferingUpdateListener() {
            @Override
            public void onBufferingUpdateListener(int percent) {
                if (onPlayerCallback != null) {
                    onPlayerCallback.onBufferingUpdate(percent);
                }
                currentBufferPercentage = percent;
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

    }

    /**
     * 开始播放视频
     */
    public void _startPlayVideo() {
        if (surfaceHolder != null) {
            if (TextUtils.isEmpty(mUrl)) {
                try {
                    mAliVcMediaPlayer.prepareAndPlay(mUrl);
                    setCurrentState(STATE_PREPARING);
                } catch (Exception e) {
                    e.printStackTrace();
                    setCurrentState(STATE_ERROR);
                    onPlayerCallback.onError(0, "视频播放出错");
                }
            }
        }
    }

    /**
     * 继续播放视频
     */
    public void _resumePlayVideo() {
        if (isInPlaybackState()) {
            mAliVcMediaPlayer.play();
            setCurrentState(STATE_PLAYING);
        }
    }

    /**
     * 暂停播放
     */
    public void _pausePlayVideo() {
        if (isInPlaybackState()) {
            if (mAliVcMediaPlayer.isPlaying()) {
                mAliVcMediaPlayer.pause();
                setCurrentState(STATE_PAUSED);
            }
        }
    }

    /**
     * 停止播放
     */
    public void _stopPlayVideo() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.stop();
            setCurrentState(STATE_IDLE);
        }
    }

    /**
     * 重新播放视频
     */
    public void _replayVideo() {
        _stopPlayVideo();
        _startPlayVideo();
    }


    /**
     * 销毁
     */
    public void _destory() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.stop();
            mAliVcMediaPlayer.releaseVideoSurface();
            mAliVcMediaPlayer.destroy();

            mAliVcMediaPlayer = null;
            surfaceHolder = null;
            setCurrentState(STATE_IDLE);
        }
    }

    /**
     * 对MediaPlayer进行重置
     */
    public void reset() {
        if (mAliVcMediaPlayer != null) {
            mAliVcMediaPlayer.reset();
            mAliVcMediaPlayer.releaseVideoSurface();
            setCurrentState(STATE_IDLE);
        }
    }

    /**
     * 设置当前播放状态
     *
     * @param state
     * @hide
     */
    private void setCurrentState(int state) {
        curState = state;
        if (onPlayerCallback != null) {
            onPlayerCallback.onStateChanged(state);
            switch (state) {
                case STATE_IDLE:
                case STATE_ERROR:
                case STATE_PREPARED:
                    onPlayerCallback.onLoadingChanged(false);
                    break;
                case STATE_PREPARING:
                    onPlayerCallback.onLoadingChanged(true);
                    break;
            }
        }
    }

    /**
     * 判断是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return (isInPlaybackState() && mAliVcMediaPlayer.isPlaying());
    }

    public boolean isInPlaybackState() {
        return (mAliVcMediaPlayer != null &&
                curState != STATE_ERROR &&
                curState != STATE_IDLE &&
                curState != STATE_PREPARING);
    }

    /**
     * 定位到
     *
     * @param progress
     */
    public void seekTo(int progress) {
        if (isInPlaybackState()) {
            mAliVcMediaPlayer.seekTo(progress);
        }
    }

    /**
     * 获取视频总时长
     *
     * @return
     */
    public int getDuration() {
        if (isInPlaybackState()) {
            return mAliVcMediaPlayer.getDuration();
        }

        return -1;
    }

    /**
     * 获得当前播放位置
     *
     * @return
     */
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mAliVcMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public AliVcMediaPlayer getmAliVcMediaPlayer() {
        return mAliVcMediaPlayer;
    }

    public void setmAliVcMediaPlayer(AliVcMediaPlayer mAliVcMediaPlayer) {
        this.mAliVcMediaPlayer = mAliVcMediaPlayer;
    }

    public int getCurState() {
        return curState;
    }

    /**
     * 获得当前缓冲进度
     *
     * @return
     */
    public int getBufferPercentage() {
        if (mAliVcMediaPlayer != null) {
            return currentBufferPercentage;
        }
        return 0;
    }

    public void setCurrentBufferPercentage(int currentBufferPercentage) {
        this.currentBufferPercentage = currentBufferPercentage;
    }

    public String getUrl() {
        return mUrl;
    }

    /**
     * 设置播放路径，开始播放
     *
     * @param url
     */
    public void setmUrl(String url) {
        this.mUrl = url;
//        _startPlayVideo();
    }

    public OnPlayerCallback getOnPlayerCallback() {
        return onPlayerCallback;
    }

    public void setOnPlayerCallback(OnPlayerCallback onPlayerCallback) {
        this.onPlayerCallback = onPlayerCallback;
    }

    public SurfaceHolder getSurfaceHolder() {
        return surfaceHolder;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        mAliVcMediaPlayer.setVideoSurface(surfaceHolder.getSurface());
    }

    public void setSurfaceChanged() {
        mAliVcMediaPlayer.setSurfaceChanged();
    }

    /**
     * 获取时间字符串
     *
     * @param time
     * @return
     * @hide
     */
    private String getTimeString(long time) {
        if (mSimpleDateFormat == null) {
            mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss.SS");
        }

        return mSimpleDateFormat.format(new Date(time));
    }
}
