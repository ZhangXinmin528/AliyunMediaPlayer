package com.example.aliyunlivedemo.vod.vodplayer.view;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.util.DisplayUtils;
import com.example.aliyunlivedemo.util.NetWorkUtils;
import com.example.aliyunlivedemo.util.StringUtils;
import com.example.aliyunlivedemo.vod.vodplayer.bean.IVideoEntity;
import com.example.aliyunlivedemo.vod.vodplayer.listener.AliPlayerControlImpl;
import com.example.aliyunlivedemo.vod.vodplayer.listener.OnAliPlayerControlListener;
import com.example.aliyunlivedemo.vod.vodplayer.video._AliPlayer;


/**
 * 视频控制器，可替换或自定义样式
 */
public class VideoControllerView extends FrameLayout {

    // 默认显示时间3秒
    public static final int DEFAULT_SHOW_TIME = 3000;

    private View mControllerTitle;
    private View mControllerBottom;

    private View mControllerBack;
    private TextView mVideoTitle;
    private SeekBar mPlayerSeekBar;
    private ImageView mVideoPlayState;
    private TextView mVideoProgress;
    private TextView mVideoDuration;
    private ImageView mVideoFullScreen;
    private ImageView mScreenLock;

    private RelativeLayout rl_pre;
    private ImageView iv_pre_play;

    private VideoErrorView mErrorView;

    private boolean isScreenLock;
    private boolean mShowing;
    private boolean mAllowUnWifiPlay;

    private boolean mDragging;
    private long mDraggingProgress;

    private _AliPlayer mPlayer;
    private IVideoEntity videoInfo;
    private OnAliPlayerControlListener onAliPlayerControlListener;

    public void setOnAliPlayerControlListener(OnAliPlayerControlListener onAliPlayerControlListener) {
        this.onAliPlayerControlListener = onAliPlayerControlListener;
    }

    public VideoControllerView(Context context) {
        super(context);
        init();
    }

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.video_media_controller, this);

        initControllerPanel();
    }

    private void initControllerPanel() {
        // back
        mControllerBack = findViewById(R.id.video_back);
        mControllerBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onAliPlayerControlListener != null) {
                    onAliPlayerControlListener.onBack();
                }
            }
        });
        // top
        mControllerTitle = findViewById(R.id.video_controller_title);
        mVideoTitle = (TextView) mControllerTitle.findViewById(R.id.video_title);
        // bottom
        mControllerBottom = findViewById(R.id.video_controller_bottom);
        mPlayerSeekBar = (SeekBar) mControllerBottom.findViewById(R.id.player_seek_bar);
        mVideoPlayState = (ImageView) mControllerBottom.findViewById(R.id.player_pause);
        mVideoProgress = (TextView) mControllerBottom.findViewById(R.id.player_progress);
        mVideoDuration = (TextView) mControllerBottom.findViewById(R.id.player_duration);
        mVideoFullScreen = (ImageView) mControllerBottom.findViewById(R.id.video_full_screen);

        rl_pre = (RelativeLayout) findViewById(R.id.rl_pre);
        iv_pre_play = (ImageView) findViewById(R.id.iv_pre_play);
        iv_pre_play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onAliPlayerControlListener != null) {
                    onAliPlayerControlListener.onStartPlay();
                    rl_pre.setVisibility(View.GONE);
                }
            }
        });

        mVideoPlayState.setOnClickListener(mOnPlayerPauseClick);
        mVideoPlayState.setImageResource(R.drawable.ic_player_video_pause);
        mPlayerSeekBar.setOnSeekBarChangeListener(mSeekListener);


        mVideoFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onAliPlayerControlListener != null) {
                    onAliPlayerControlListener.onFullScreen();
                }
            }
        });

        // lock
        mScreenLock = (ImageView) findViewById(R.id.player_lock_screen);
        mScreenLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScreenLock) unlock();
                else lock();
                show();
            }
        });

        // error
        mErrorView = (VideoErrorView) findViewById(R.id.video_controller_error);
        mErrorView.setOnAliPlayerControlListener(new AliPlayerControlImpl() {
            @Override
            public void onRetry(int errorStatus) {
                retry(errorStatus);
            }
        });

        mPlayerSeekBar.setMax(1000);
    }

    /**
     * 设置MediaPlayer
     *
     * @param player
     */
    public void setMediaPlayer(_AliPlayer player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * 设置视频信息实体类
     *
     * @param videoInfo
     */
    public void setVideoInfo(IVideoEntity videoInfo) {
        this.videoInfo = videoInfo;
        String title = videoInfo.getVideoTitle();
        if (title != null && !TextUtils.isEmpty(title))
            mVideoTitle.setText(title);
    }

    /**
     * 控制隐藏显示
     */
    public void toggleDisplay() {
        if (mShowing) {
            hide();
        } else {
            show();
        }
    }

    /**
     * 显示控制器
     */
    public void show() {
        show(DEFAULT_SHOW_TIME);
    }

    /**
     * 显示控制器
     *
     * @param timeout 显示时长
     */
    public void show(int timeout) {
        setProgress();

        if (!isScreenLock) {
            mControllerBack.setVisibility(VISIBLE);
            mControllerTitle.setVisibility(VISIBLE);
            mControllerBottom.setVisibility(VISIBLE);
        } else {
            if (!DisplayUtils.isPortrait(getContext())) {
                mControllerBack.setVisibility(GONE);
            }
            mControllerTitle.setVisibility(GONE);
            mControllerBottom.setVisibility(GONE);
        }

        if (!DisplayUtils.isPortrait(getContext())) {
            mScreenLock.setVisibility(VISIBLE);
        }

        mShowing = true;

        updatePausePlay();

        // 开始显示
        post(mShowProgress);

        if (timeout > 0) {
            // 先移除之前的隐藏异步操作
            removeCallbacks(mFadeOut);
            //timeout后隐藏
            postDelayed(mFadeOut, timeout);
        }
    }

    /**
     * 隐藏控制器
     */
    private void hide() {
        if (!mShowing) {
            return;
        }

        if (!DisplayUtils.isPortrait(getContext())) {
            // 横屏才消失
            mControllerBack.setVisibility(GONE);
        }
        mControllerTitle.setVisibility(GONE);
        mControllerBottom.setVisibility(GONE);
        mScreenLock.setVisibility(GONE);

        removeCallbacks(mShowProgress);

        mShowing = false;
    }

    /**
     * 异步操作隐藏
     */
    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * 异步操作显示
     */
    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
                // 解决1秒之内的误差，使得发送消息正好卡在整秒
                Log.e("TAG", "run: " + (1000 - (pos % 1000)));
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    /**
     * 设置进度，同时也返回进度
     *
     * @return
     */
    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mPlayerSeekBar != null) {
            if (duration > 0) {
                    // use long to avoid overflow
                    long pos = 1000L * position / duration;
                mPlayerSeekBar.setProgress((int) pos);
            }
            // 设置缓冲进度
            int percent = mPlayer.getBufferPercentage();
            mPlayerSeekBar.setSecondaryProgress(percent * 10);
        }

        mVideoProgress.setText(StringUtils.stringForTime(position));
        mVideoDuration.setText(StringUtils.stringForTime(duration));

        return position;
    }

    /**
     * 判断显示错误类型
     */
    public void checkShowError(boolean isNetChanged) {
        boolean isConnect = NetWorkUtils.isNetworkConnected(getContext());
        boolean isMobileNet = NetWorkUtils.isMobileConnected(getContext());
        boolean isWifiNet = NetWorkUtils.isWifiConnected(getContext());

        if (isConnect) {
            // 如果已经联网
            if (mErrorView.getCurStatus() == VideoErrorView.STATUS_NO_NETWORK_ERROR && !(isMobileNet && !isWifiNet)) {
                // 如果之前是无网络
                mErrorView.setVisibility(View.GONE);
            } else if (videoInfo == null) {
                // 优先判断是否有video数据
                showError(VideoErrorView.STATUS_VIDEO_DETAIL_ERROR);
            } else if (isMobileNet && !isWifiNet && !mAllowUnWifiPlay) {
                // 如果是手机流量，且未同意过播放，且非本地视频，则提示错误
                mErrorView.showError(VideoErrorView.STATUS_UN_WIFI_ERROR);
                mPlayer._pausePlayVideo();
            } else if (isWifiNet && isNetChanged && mErrorView.getCurStatus() == VideoErrorView.STATUS_UN_WIFI_ERROR) {
                // 如果是wifi流量，且之前是非wifi错误，则恢复播放
                playFromUnWifiError();
            } else if (!isNetChanged) {
                showError(VideoErrorView.STATUS_VIDEO_SRC_ERROR);
            }
        } else {
            // 无网，暂停播放并提示
            mPlayer._pausePlayVideo();
            showError(VideoErrorView.STATUS_NO_NETWORK_ERROR);
        }
    }

    /**
     * 隐藏错误提示
     */
    public void hideErrorView() {
        mErrorView.hideError();
    }

    /**
     * 重新播放
     */
    private void reload() {
        mPlayer._replayVideo();
    }

    /**
     * 重置
     */
    public void release() {
        removeCallbacks(mShowProgress);
        removeCallbacks(mFadeOut);
    }

    /**
     * 出错重试
     *
     * @param status
     */
    private void retry(int status) {
        Log.i("DDD", "retry " + status);

        switch (status) {
            case VideoErrorView.STATUS_VIDEO_DETAIL_ERROR:
                // 传递给activity
                if (onAliPlayerControlListener != null) {
                    onAliPlayerControlListener.onRetry(status);
                }
                break;
            case VideoErrorView.STATUS_VIDEO_SRC_ERROR:
                reload();
                break;
            case VideoErrorView.STATUS_UN_WIFI_ERROR:
                allowUnWifiPlay();
                break;
            case VideoErrorView.STATUS_NO_NETWORK_ERROR:
                // 无网络时
                if (NetWorkUtils.isNetworkConnected(getContext())) {
                    if (videoInfo == null) {
                        // 如果video为空，重新请求详情
                        retry(VideoErrorView.STATUS_VIDEO_DETAIL_ERROR);
                    } else if (mPlayer.isInPlaybackState()) {
                        // 如果有video，可以直接播放的直接恢复
                        mPlayer._resumePlayVideo();
                    } else {
                        // 视频未准备好，重新加载
                        reload();
                    }
                } else {
                    Toast.makeText(getContext(), "网络未连接", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            removeCallbacks(mShowProgress);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }

            long duration = mPlayer.getDuration();
            mDraggingProgress = (duration * progress) / 1000L;

            if (mVideoProgress != null) {
                mVideoProgress.setText(StringUtils.stringForTime((int) mDraggingProgress));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mPlayer.seekTo((int) mDraggingProgress);
            play();
            mDragging = false;
            mDraggingProgress = 0;

            post(mShowProgress);
        }
    };

    /**
     * 提示错误
     *
     * @param status
     */
    private void showError(int status) {
        mErrorView.showError(status);
        hide();

        // 如果提示了错误，则看需要解锁
        if (isScreenLock) {
            unlock();
        }
    }

    /**
     * 是否锁屏
     *
     * @return
     */
    public boolean isLock() {
        return isScreenLock;
    }

    /**
     * 锁屏
     */
    private void lock() {
        Log.i("DDD", "lock");
        isScreenLock = true;
        mScreenLock.setImageResource(R.drawable.player_video_locked);
    }

    /**
     * 解锁
     */
    private void unlock() {
        Log.i("DDD", "unlock");
        isScreenLock = false;
        mScreenLock.setImageResource(R.drawable.player_video_unlock);
    }

    /**
     * 允许非wifi播放
     */
    private void allowUnWifiPlay() {
        Log.i("DDD", "allowUnWifiPlay");

        mAllowUnWifiPlay = true;

        playFromUnWifiError();
    }

    /**
     * 从WIFI错误异常状态下播放
     */
    private void playFromUnWifiError() {
        Log.i("DDD", "playFromUnWifiError");

        // TODO: 2017/6/19 check me
        if (mPlayer.isInPlaybackState()) {
            mPlayer._resumePlayVideo();
        } else {
            mPlayer._replayVideo();
        }
    }

    private OnClickListener mOnPlayerPauseClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
        }
    };

    /**
     * 切换播放按钮图片
     */
    public void updatePausePlay() {
        if (mPlayer.isPlaying()) {
            mVideoPlayState.setImageResource(R.drawable.ic_player_video_pause);
        } else {
            mVideoPlayState.setImageResource(R.drawable.ic_player_video_play);
        }
    }

    /**
     * 切换播放暂停
     */
    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    /**
     * 暂停
     */
    private void pause() {
        mPlayer._pausePlayVideo();
        updatePausePlay();
        show();
    }

    /**
     * 播放
     */
    private void play() {
        mPlayer._resumePlayVideo();
        show();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleVideoLayoutParams();
    }

    /**
     * 横竖屏切换时按钮的隐蔽与显示
     */
    void toggleVideoLayoutParams() {
        final boolean isPortrait = DisplayUtils.isPortrait(getContext());

        if (isPortrait) {
            mControllerBack.setVisibility(VISIBLE);
            mVideoFullScreen.setVisibility(View.VISIBLE);
            mScreenLock.setVisibility(GONE);
        } else {
            mVideoFullScreen.setVisibility(View.GONE);
            if (mShowing) {
                mScreenLock.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 控制默认背景隐藏和显示
     *
     * @param flag
     */
    public void showBg(boolean flag) {
        if (flag) {
            rl_pre.setVisibility(View.VISIBLE);
        } else {
            rl_pre.setVisibility(View.GONE);
        }
    }
}
