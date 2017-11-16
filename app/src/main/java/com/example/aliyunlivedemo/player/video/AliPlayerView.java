package com.example.aliyunlivedemo.player.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.player.bean.IVideoEntity;
import com.example.aliyunlivedemo.player.bean.VideoEntity;
import com.example.aliyunlivedemo.player.listener.OnAliPlayerControlListener;
import com.example.aliyunlivedemo.player.listener.OnPlayerCallback;
import com.example.aliyunlivedemo.player.view.VideoBehaviorView;
import com.example.aliyunlivedemo.player.view.VideoControllerView;
import com.example.aliyunlivedemo.player.view.VideoProgressOverlay;
import com.example.aliyunlivedemo.player.view.VideoSystemOverlay;
import com.example.aliyunlivedemo.util.NetworkUtils;

/**
 * 视频播放器的封装类
 */
public final class AliPlayerView extends VideoBehaviorView {
    private static final String TAG = AliPlayerView.class.getSimpleName();

    private Context mContext;
    private int initWidth;
    private int initHeight;
    private SurfaceView mSurfaceView;
    private View mLoadingView;
    private VideoProgressOverlay mProgressView;
    private VideoSystemOverlay mSystemView;
    private VideoControllerView mMediaController;

    //阿里云播放器
    private _AliPlayer mAliPlayer;
    private String mVideoUrl;//播放地址

    private NetChangedReceiver mNetChangedReceiver;

    //是否切换到后台暂停
    private boolean isBackgroundPause;

    public AliPlayerView(Context context) {
        this(context, null, 0);
    }

    public AliPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(R.layout.layout_video_container, this);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview_video);
        mLoadingView = findViewById(R.id.video_loading);
        mProgressView = (VideoProgressOverlay) findViewById(R.id.video_progress_overlay);
        mSystemView = (VideoSystemOverlay) findViewById(R.id.video_system_overlay);
        mMediaController = (VideoControllerView) findViewById(R.id.video_controller);

        initPlayer();

        //为SurfaceHolder添加回调
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initWidth = getWidth();
                initHeight = getHeight();
                if (mAliPlayer != null) {
                    mAliPlayer._setSurfaceHolder(holder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mAliPlayer != null) {
                    mAliPlayer._setSurfaceChanged();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mAliPlayer != null) {
                    mAliPlayer._setSurfaceDestroyed(holder);
                }
            }
        });

        // 注册网络状态变化广播
        registerNetChangedReceiver();
    }

    private void initPlayer() {
        mAliPlayer = new _AliPlayer(mContext, mSurfaceView);

        // todo 这里可以优化，将这些回调全部暴露出去
        mAliPlayer.setOnPlayerCallback(new OnPlayerCallback() {
            @Override
            public void onPrepared() {
                Log.e(TAG, "onPrepared: ");
                mAliPlayer._resumePlayVideo();
                mMediaController.show();
                mMediaController.hideErrorView();
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {

            }

            @Override
            public void onBufferingUpdate(int percent) {

            }

            @Override
            public void onCompletion() {
                mMediaController.updatePausePlay();
            }

            @Override
            public void onError(int what, String extra) {
                mMediaController.checkShowError(false);
            }

            @Override
            public void onLoadingChanged(boolean isShow) {
                if (isShow) showLoading();
                else hideLoading();
            }

            @Override
            public void onStateChanged(int curState) {
                switch (curState) {
                    case _AliPlayer.STATE_IDLE:
                        am.abandonAudioFocus(null);
                        break;
                    case _AliPlayer.STATE_PREPARING:
                        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        break;
                }
            }
        });

        mMediaController.setMediaPlayer(mAliPlayer);
    }

    /**
     * 设置视频播放信息
     *
     * @param videoUrl 视频播放地址
     */
    public void setVideoUrl(String videoUrl) {
        if (!TextUtils.isEmpty(videoUrl)) {
            mMediaController.setVideoInfo(new VideoEntity(videoUrl));
            mAliPlayer.setUrl(videoUrl);
        }
    }

    /**
     * 设置视频播放信息
     *
     * @param entity 视频信息类
     */
    public void setVideoEntity(@Nullable IVideoEntity entity) {
        mMediaController.setVideoInfo(entity);
        mAliPlayer.setUrl(entity.getVideoPath());
    }

    /**
     * 设置视频播放地址，并播放
     *
     * @param videoUrl
     */
    public void setVideoPathAndStartPlay(String videoUrl) {

        if (!TextUtils.isEmpty(videoUrl)) {
            mMediaController.setVideoInfo(new VideoEntity(videoUrl));
            mAliPlayer.setUrl(videoUrl);
            mAliPlayer._startPlayVideo();
            mMediaController.showBg(false);
        }
    }

    /**
     * 开始播放视频
     */
    public void startPlayVideo() {
        mAliPlayer._startPlayVideo();
        mMediaController.showBg(false);
    }

    /**
     * 暂停播放并记录播放状态
     */
    public void onPause() {
        if (mAliPlayer.isPlaying()) {
            isBackgroundPause = true;
            mAliPlayer._pausePlayVideo();
        }
    }

    /**
     * 停止播放视频
     */
    public void onStop() {
        if (mAliPlayer.isPlaying()) {
            mAliPlayer._stopPlayVideo();
        }
    }

    /**
     * 开始播放
     * 当前页面重新获取焦点时，继续播放
     */
    public void onResume() {
        if (isBackgroundPause) {
            // 如果切换到后台暂停，后又切回来，则继续播放
            isBackgroundPause = false;
            mAliPlayer._resumePlayVideo();
        }
    }

    /**
     * 销毁并释放资源
     */
    public void onDestroy() {
        mAliPlayer._destoryPlayer();
        mMediaController.release();
        unRegisterNetChangedReceiver();
    }

    /**
     * 进行视频的重播
     */
    public void replay() {
        if (mAliPlayer != null) {
            mAliPlayer._replayVideo();
        }
    }


    /**
     * 设置视频控制监听，有全屏监听、返回监听、重试监听
     * MediaController中对重试监听已经进行处理
     * 后期可以进行扩展：分享、收藏等
     *
     * @param onAliPlayerControlListener
     */
    public void setOnPlayerControlListener(OnAliPlayerControlListener onAliPlayerControlListener) {
        mMediaController.setOnAliPlayerControlListener(onAliPlayerControlListener);
    }

    /**
     * 显示加载中
     *
     * @hide
     */
    private void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏加载中
     *
     * @hide
     */
    private void hideLoading() {
        mLoadingView.setVisibility(View.GONE);
    }

    /**
     * 是否锁屏
     *
     * @return
     */
    public boolean isLock() {
        return mMediaController.isLock();
    }

    public VideoControllerView getMediaController() {
        return mMediaController;
    }

    public void setMediaController(VideoControllerView mMediaController) {
        this.mMediaController = mMediaController;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mMediaController.toggleDisplay();
        return super.onSingleTapUp(e);
    }

    // 对锁屏情况下进行处理
    @Override
    public boolean onDown(MotionEvent e) {
        if (isLock()) {
            return false;
        }
        return super.onDown(e);
    }


    /**
     * 在屏幕横竖屏切换时执行，
     * 全屏时转为横屏，播放器横纵向填充全屏
     * 竖屏时，播放器大小就是布局中设置的大小
     *
     * @param newConfig
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getLayoutParams().width = initWidth;
            getLayoutParams().height = initHeight;
        } else {
            getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
            getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
        }

    }

    // 对锁屏情况下进行处理
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isLock()) {
            return false;
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    protected void endGesture(int behaviorType) {
        switch (behaviorType) {
            case VideoBehaviorView.FINGER_BEHAVIOR_BRIGHTNESS:
            case VideoBehaviorView.FINGER_BEHAVIOR_VOLUME:
                Log.i("DDD", "endGesture: left right");
                mSystemView.hide();
                break;
            case VideoBehaviorView.FINGER_BEHAVIOR_PROGRESS:
                Log.i("DDD", "endGesture: bottom");
                mAliPlayer.seekTo(mProgressView.getTargetProgress());
                mProgressView.hide();
                break;
        }
    }

    @Override
    protected void updateSeekUI(int delProgress) {
        mProgressView.show(delProgress, mAliPlayer.getCurrentPosition(), mAliPlayer.getDuration());
    }

    @Override
    protected void updateVolumeUI(int max, int progress) {
        mSystemView.show(VideoSystemOverlay.SystemType.VOLUME, max, progress);
    }

    @Override
    protected void updateLightUI(int max, int progress) {
        mSystemView.show(VideoSystemOverlay.SystemType.BRIGHTNESS, max, progress);
    }

    //=====================================监听网络变化==========================================

    /**
     * 网络变化广播接收器
     */
    private class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable extra = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (extra != null && extra instanceof NetworkInfo) {
                NetworkInfo netInfo = (NetworkInfo) extra;

                if (NetworkUtils.isNetworkConnected(context)
                        && netInfo.getState() != NetworkInfo.State.CONNECTED) {
                    // 网络连接的情况下只处理连接完成状态
                    return;
                }

                mMediaController.checkShowError(true);
            }
        }
    }

    /**
     * 注册网络广播
     */
    public void registerNetChangedReceiver() {
        if (mNetChangedReceiver == null) {
            mNetChangedReceiver = new NetChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            activity.registerReceiver(mNetChangedReceiver, filter);
        }
    }

    /**
     * 解注册广播
     */
    public void unRegisterNetChangedReceiver() {
        if (mNetChangedReceiver != null) {
            activity.unregisterReceiver(mNetChangedReceiver);
        }
    }
    //==============================================================================================

}
