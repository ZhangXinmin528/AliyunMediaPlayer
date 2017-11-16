package com.example.aliyunlivedemo.player.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.player.bean.IVideoInfo;
import com.example.aliyunlivedemo.player.listener.OnPlayerCallback;
import com.example.aliyunlivedemo.player.listener.OnVideoControlListener;
import com.example.aliyunlivedemo.player.view.VideoBehaviorView;
import com.example.aliyunlivedemo.player.view.VideoControllerView;
import com.example.aliyunlivedemo.player.view.VideoProgressOverlay;
import com.example.aliyunlivedemo.player.view.VideoSystemOverlay;
import com.example.aliyunlivedemo.util.NetworkUtils;

/**
 * 视频播放器的封装类
 */
public final class CustomMediaPlayer extends VideoBehaviorView {
    private static final String TAG = "JsPlayer";
    private SurfaceView surfaceView;
    private View loadingView;
    private VideoProgressOverlay progressView;
    private VideoSystemOverlay systemView;
    private VideoControllerView mediaController;

    private AliPlayerBuilder mMediaPlayer;

    private int initWidth;
    private int initHeight;

    private NetChangedReceiver netChangedReceiver;

    //是否切换到后台暂停
    private boolean isBackgroundPause;

    public CustomMediaPlayer(Context context) {
        super(context);
        init();
    }

    public CustomMediaPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomMediaPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.video_view, this);
//        View rootView = View.inflate(activity, R.layout.video_view, null);

        surfaceView = (SurfaceView) findViewById(R.id.video_surface);
        loadingView = findViewById(R.id.video_loading);
        progressView = (VideoProgressOverlay) findViewById(R.id.video_progress_overlay);
        systemView = (VideoSystemOverlay) findViewById(R.id.video_system_overlay);
        mediaController = (VideoControllerView) findViewById(R.id.video_controller);


        initPlayer();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.e(TAG, "surfaceCreated: ");
                initWidth = getWidth();
                initHeight = getHeight();

                if (mMediaPlayer != null) {
                    mMediaPlayer.setSurfaceHolder(holder);
                }

                holder.setKeepScreenOn(true);

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setSurfaceChanged();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        // 注册网络状态变化广播
        registerNetChangedReceiver();
    }

    private void initPlayer() {
        mMediaPlayer = new AliPlayerBuilder(activity, surfaceView);

        // todo 这里可以优化，将这些回调全部暴露出去
        mMediaPlayer.setOnPlayerCallback(new OnPlayerCallback() {
            @Override
            public void onPrepared() {
                Log.e(TAG, "onPrepared: ");
                mMediaPlayer._resumePlayVideo();
                mediaController.show();
                mediaController.hideErrorView();
            }

            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

            }

            @Override
            public void onBufferingUpdate(int percent) {

            }

            @Override
            public void onCompletion() {
                mediaController.updatePausePlay();
            }

            @Override
            public void onError(int what, String extra) {
                mediaController.checkShowError(false);
            }

            @Override
            public void onLoadingChanged(boolean isShow) {
                if (isShow) showLoading();
                else hideLoading();
            }

            @Override
            public void onStateChanged(int curState) {
                switch (curState) {
                    case AliPlayerBuilder.STATE_IDLE:
                        am.abandonAudioFocus(null);
                        break;
                    case AliPlayerBuilder.STATE_PREPARING:
                        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        break;
                }
            }
        });

        mediaController.setMediaPlayer(mMediaPlayer);
    }

    /**
     * 开始播放
     */
    public void setPath(final IVideoInfo video) {
        if (video == null) {
            return;
        }

        mMediaPlayer.reset();

        String videoPath = video.getVideoPath();
        mediaController.setVideoInfo(video);
        mMediaPlayer.setmUrl(videoPath);

    }

    public void startPlay() {
        mMediaPlayer._startPlayVideo();
    }

    public void setPathAndPlay(final IVideoInfo video) {
        if (video == null) {
            return;
        }

        mMediaPlayer.reset();

        String videoPath = video.getVideoPath();
        mediaController.setVideoInfo(video);
        mMediaPlayer.setmUrl(videoPath);
        mMediaPlayer._startPlayVideo();

        mediaController.showBg(false);
    }

    public void onStop() {
        if (mMediaPlayer.isPlaying()) {
            // 如果已经开始且在播放，则暂停同时记录状态
            isBackgroundPause = true;
            mMediaPlayer._pausePlayVideo();
        }
    }

    public void onStart() {
        if (isBackgroundPause) {
            // 如果切换到后台暂停，后又切回来，则继续播放
            isBackgroundPause = false;
            mMediaPlayer._resumePlayVideo();
        }
    }

    public void onDestroy() {
        mMediaPlayer._stopPlayVideo();
        mediaController.release();
        unRegisterNetChangedReceiver();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mediaController.toggleDisplay();
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
                systemView.hide();
                break;
            case VideoBehaviorView.FINGER_BEHAVIOR_PROGRESS:
                Log.i("DDD", "endGesture: bottom");
                mMediaPlayer.seekTo(progressView.getTargetProgress());
                progressView.hide();
                break;
        }
    }

    @Override
    protected void updateSeekUI(int delProgress) {
        progressView.show(delProgress, mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
    }

    @Override
    protected void updateVolumeUI(int max, int progress) {
        systemView.show(VideoSystemOverlay.SystemType.VOLUME, max, progress);
    }

    @Override
    protected void updateLightUI(int max, int progress) {
        systemView.show(VideoSystemOverlay.SystemType.BRIGHTNESS, max, progress);
    }

    /**
     * 设置视频控制监听，有全屏监听、返回监听、重试监听
     * MediaController中对重试监听已经进行处理
     * 后期可以进行扩展：分享、收藏等
     *
     * @param onVideoControlListener
     */
    public void setOnVideoControlListener(OnVideoControlListener onVideoControlListener) {
        mediaController.setOnVideoControlListener(onVideoControlListener);
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

    /**
     * 显示加载中
     */
    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏加载中
     */
    private void hideLoading() {
        loadingView.setVisibility(View.GONE);
    }

    /**
     * 是否锁屏
     *
     * @return
     */
    public boolean isLock() {
        return mediaController.isLock();
    }

    public VideoControllerView getMediaController() {
        return mediaController;
    }

    public void setMediaController(VideoControllerView mediaController) {
        this.mediaController = mediaController;
    }

    /**
     * 注册网络广播
     */
    public void registerNetChangedReceiver() {
        if (netChangedReceiver == null) {
            netChangedReceiver = new NetChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            activity.registerReceiver(netChangedReceiver, filter);
        }
    }

    /**
     * 解注册广播
     */
    public void unRegisterNetChangedReceiver() {
        if (netChangedReceiver != null) {
            activity.unregisterReceiver(netChangedReceiver);
        }
    }

    /**
     * 网络变化广播接收器
     */
    private class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable extra = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (extra != null && extra instanceof NetworkInfo) {
                NetworkInfo netInfo = (NetworkInfo) extra;

                if (NetworkUtils.isNetworkConnected(context) && netInfo.getState() != NetworkInfo.State.CONNECTED) {
                    // 网络连接的情况下只处理连接完成状态
                    return;
                }

                mediaController.checkShowError(true);
            }
        }
    }


}
