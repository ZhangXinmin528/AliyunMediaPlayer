package com.example.aliyunlivedemo.live.push.push_3_0;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.alivc.live.pusher.AlivcFpsEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.alivc.live.pusher.SurfaceStatus;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.adapter.LivePushFragmentAdapter;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.listener.LivePauseStateListener;
import com.example.aliyunlivedemo.listener.SimpleOnGestureListener;
import com.example.aliyunlivedemo.listener.SimpleOnScaleGestureListener;
import com.example.aliyunlivedemo.live.push.push_3_0.fragment.LivePushFragment;
import com.example.aliyunlivedemo.live.push.push_3_0.fragment.PushDiagramStatsFragment;
import com.example.aliyunlivedemo.live.push.push_3_0.fragment.PushTextStatsFragment;
import com.example.aliyunlivedemo.util.DialogUtils;
import com.example.aliyunlivedemo.util.LiveConfig;

import java.util.ArrayList;
import java.util.List;

import static com.alivc.live.pusher.AlivcLivePushConstants.DEFAULT_VALUE_INT_AUDIO_RETRY_COUNT;
import static com.alivc.live.pusher.AlivcLivePushConstants.DEFAULT_VALUE_INT_RETRY_INTERVAL;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_LEFT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT;

/**
 * Created by ZhangXinmin on 2017/11/22.
 * Copyright (c) 2017 . All rights reserved.
 * 封装播放页面：v3.0
 */

public class LivePushFlowActivity extends BaseActivity {
    private static final String TAG = LivePushFlowActivity.class.getSimpleName();

    private static final int FLING_MIN_DISTANCE = 50;
    private final long REFRESH_INTERVAL = 1000;

    private Context mContext;
    private SurfaceView mSurfaceView;
    private SurfaceStatus mSurfaceStatus;

    //直播控件
    private AlivcLivePusher mAlivcLivePusher;
    private AlivcLivePushConfig mAlivcLivePushConfig;

    //直播参数
    private String mRtmpUrl;//推流地址
    private boolean mAsync;//是否异步
    private boolean mAudioOnly;//是否纯音频
    private int mOrientation;//屏幕方向
    private int mCameraId;//摄像头
    private boolean mFlashOn;//是否打开闪光灯

    //直播蒙版界面
    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;
    private LivePushFragmentAdapter mFragmentAdapter;
    private LivePushFragment mLivePushFragment;
    private PushTextStatsFragment mPushTextStatsFragment;
    private PushDiagramStatsFragment mPushDiagramStatsFragment;
    private Handler mHandler;

    //手势监听
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private LivePauseStateListener mPauseStateListener;
    //直播推流状态
    private boolean isPause;
    private AlivcLivePushStatsInfo livePushStatsInfo;

    @Override
    protected Object setLayout() {
        return R.layout.activity_live_push_new;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;
        mSurfaceStatus = SurfaceStatus.UNINITED;
        mHandler = new Handler();
        //init params
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey(LiveConfig.LIVE_URL.name())) {
                    mRtmpUrl = bundle.getString(LiveConfig.LIVE_URL.name());
                }
                if (bundle.containsKey(LiveConfig.IS_ANSYC.name())) {
                    mAsync = bundle.getBoolean(LiveConfig.IS_ANSYC.name(), false);//不开启异步
                }
                if (bundle.containsKey(LiveConfig.IS_AUDIO_ONLY.name())) {
                    mAudioOnly = bundle.getBoolean(LiveConfig.IS_AUDIO_ONLY.name(), false);//默认非纯音频
                }
                if (bundle.containsKey(LiveConfig.SCREEN_ORIENTATION.name())) {
                    mOrientation = bundle.getInt(LiveConfig.SCREEN_ORIENTATION.name(), 0);//默认竖屏
                }
                if (bundle.containsKey(LiveConfig.CAMERA_ID.name())) {
                    mCameraId = bundle.getInt(LiveConfig.CAMERA_ID.name(), 1);//默认前置摄像头
                }
                if (bundle.containsKey(LiveConfig.IS_FLASH_ON.name())) {
                    mFlashOn = bundle.getBoolean(LiveConfig.IS_FLASH_ON.name(), false);//默认关闭闪光灯
                }
            }
        }

        //设置横竖屏
        setOrientation(mOrientation);
        //配置直播参数
        initLiveConfigures();
        //初始化直播推流对象
        initLivePusher();

        //init live fragment
        mFragmentList = new ArrayList<>();
        mFragmentAdapter = new LivePushFragmentAdapter(getSupportFragmentManager(), mFragmentList);
        mLivePushFragment = LivePushFragment.newInstance(mRtmpUrl, mAsync, mAudioOnly, mCameraId, mFlashOn);
        mLivePushFragment.setAlivcLivePusher(mAlivcLivePusher);
        /*mLivePushFragment.setStateListener(new LivePauseStateListener() {
            @Override
            public void onPause(boolean state) {
                isPause = state;
            }
        });*/

        //日志信息
        mPushTextStatsFragment = new PushTextStatsFragment();
        mPushDiagramStatsFragment = new PushDiagramStatsFragment();

        //添加手势
        mGestureDetector = new GestureDetector(mContext, mGestureListener);
        mScaleDetector = new ScaleGestureDetector(mContext, mScaleGestureListener);
    }

    /**
     * 直播参数配置
     *
     * @hide
     */
    private void initLiveConfigures() {
        //直播配置
        mAlivcLivePushConfig = new AlivcLivePushConfig();
        //设置分辨率
        mAlivcLivePushConfig.setResolution(AlivcResolutionEnum.RESOLUTION_540P);
        //设置初始码率
        mAlivcLivePushConfig.setInitialVideoBitrate(800);
        //设置目标码率
        mAlivcLivePushConfig.setTargetVideoBitrate(800);
        //设置最小码率
        mAlivcLivePushConfig.setMinVideoBitrate(400);
        //设置采集帧率：
        mAlivcLivePushConfig.setFps(AlivcFpsEnum.FPS_25);
        //设置解码方式：硬解
//        mAlivcLivePushConfig.setVideoEncodeMode(AlivcEncodeModeEnum.Encode_MODE_HARD);
        //设置重连次数:5次
        mAlivcLivePushConfig.setConnectRetryCount(DEFAULT_VALUE_INT_AUDIO_RETRY_COUNT);
        //重连时长:1000ms
        mAlivcLivePushConfig.setConnectRetryInterval(DEFAULT_VALUE_INT_RETRY_INTERVAL);
        //美颜开关
        mAlivcLivePushConfig.setBeautyOn(true);
        //设置美颜参数
        mAlivcLivePushConfig.setBeautyBrightness(60); //亮度60
        mAlivcLivePushConfig.setBeautyRuddy(40); //红润 40
        mAlivcLivePushConfig.setBeautyBuffing(50); //磨皮 50
        mAlivcLivePushConfig.setBeautyWhite(20); //美白 20
        mAlivcLivePushConfig.setBeautySaturation(10); //饱和度 10
    }

    @Override
    protected void initViews() {
        super.initViews();

        mSurfaceView = findViewById(R.id.surfaceview_live_push_new);
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);

        //init viewpager
        initViewPager();
    }

    /**
     * 初始化直播推流对象
     *
     * @hide
     */
    private void initLivePusher() {
        mAlivcLivePusher = new AlivcLivePusher();
        if (mAlivcLivePushConfig != null) {
            try {
                mAlivcLivePusher.init(mContext, mAlivcLivePushConfig);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                DialogUtils.showErrorDialog(mContext, e.getMessage());
            } catch (IllegalStateException e) {
                e.printStackTrace();
                DialogUtils.showErrorDialog(mContext, e.getMessage());
            }
        }
    }

    /**
     * init viewpager
     *
     * @hide
     */
    private void initViewPager() {
        mViewPager = findViewById(R.id.viewpager_live_push_new);
        mFragmentList.add(mPushTextStatsFragment);
        mFragmentList.add(mLivePushFragment);
        mFragmentList.add(mPushDiagramStatsFragment);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setCurrentItem(1);//指定显示页面
        //添加手势监听，进行传递
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mViewPager.getCurrentItem() == 1) {//直播界面
                    if (motionEvent.getPointerCount() >= 2) {
                        mScaleDetector.onTouchEvent(motionEvent);
                    } else if (motionEvent.getPointerCount() == 1) {
                        mGestureDetector.onTouchEvent(motionEvent);
                    }
                }
                return false;
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    mHandler.removeCallbacks(mRunnable);
                } else {// 开启线程，推流信息
                    mHandler.post(mRunnable);
                }
                super.onPageSelected(position);
            }
        });
    }

    /**
     * 进行横竖屏的设置
     *
     * @param orientation
     * @hide
     */
    private void setOrientation(int orientation) {
        if (orientation == ORIENTATION_PORTRAIT.ordinal()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (orientation == ORIENTATION_LANDSCAPE_HOME_RIGHT.ordinal()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (orientation == ORIENTATION_LANDSCAPE_HOME_LEFT.ordinal()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    //回调
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.i(TAG, "surfaceCreated");
            if (mSurfaceStatus == SurfaceStatus.UNINITED) {
                mSurfaceStatus = SurfaceStatus.CREATED;
                if (mAlivcLivePusher != null) {
                    try {
                        if (mAsync) {
                            mAlivcLivePusher.startPreviewAysnc(mSurfaceView);
                        } else {
                            mAlivcLivePusher.startPreview(mSurfaceView);
                        }
                    } catch (IllegalArgumentException e) {
                        e.toString();
                    } catch (IllegalStateException e) {
                        e.toString();
                    }
                }
            } else if (mSurfaceStatus == SurfaceStatus.DESTROYED) {
                mSurfaceStatus = SurfaceStatus.RECREATED;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Log.i(TAG, "surfaceChanged");
            mSurfaceStatus = SurfaceStatus.CHANGED;
            if (mLivePushFragment != null) {
                mLivePushFragment.setSurfaceView(mSurfaceView);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.i(TAG, "surfaceDestroyed");
            mSurfaceStatus = SurfaceStatus.DESTROYED;
        }
    };


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        AlivcPreviewOrientationEnum orientationEnum;
        if (mAlivcLivePusher != null) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientationEnum = ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientationEnum = ORIENTATION_LANDSCAPE_HOME_RIGHT;
                    break;
                case Surface.ROTATION_270:
                    orientationEnum = ORIENTATION_LANDSCAPE_HOME_LEFT;
                    break;
                default:
                    orientationEnum = ORIENTATION_PORTRAIT;
                    break;
            }
            mAlivcLivePusher.setPreviewOrientation(orientationEnum);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAlivcLivePusher != null) {
            try {
                if (!isPause) {
                    if (mAsync) {
                        mAlivcLivePusher.resumeAsync();
                    } else {
                        mAlivcLivePusher.resume();
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (mViewPager.getCurrentItem() != 1) {
            mHandler.post(mRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAlivcLivePusher != null) {
            try {
                mAlivcLivePusher.pause();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        if (mAlivcLivePusher != null) {
            try {
                mAlivcLivePusher.destroy();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        mFragmentList = null;
        mSurfaceView = null;
        mViewPager = null;
        mFragmentAdapter = null;
        mGestureDetector = null;
        mScaleDetector = null;
        mLivePushFragment = null;
        mPushTextStatsFragment = null;
        mPushDiagramStatsFragment = null;
        mAlivcLivePushConfig = null;
        mAlivcLivePusher = null;
        livePushStatsInfo = null;
        super.onDestroy();
    }

    //=====================================2.推流日志信息==================================================
    private Runnable mRunnable = new Runnable() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void run() {
            new AsyncTask<AlivcLivePushStatsInfo, Void, AlivcLivePushStatsInfo>() {
                @Override
                protected AlivcLivePushStatsInfo doInBackground(AlivcLivePushStatsInfo... alivcLivePushStatsInfos) {
                    try {
                        livePushStatsInfo = mAlivcLivePusher.getLivePushStatsInfo();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    return livePushStatsInfo;
                }

                @Override
                protected void onPostExecute(AlivcLivePushStatsInfo alivcLivePushStatsInfo) {
                    super.onPostExecute(alivcLivePushStatsInfo);
                    if (mPushTextStatsFragment != null && mViewPager.getCurrentItem() == 0) {
                        mPushTextStatsFragment.updateValue(alivcLivePushStatsInfo);
                    } else if (mPushDiagramStatsFragment != null && mViewPager.getCurrentItem() == 2) {
                        mPushDiagramStatsFragment.updateValue(alivcLivePushStatsInfo);
                    }
                    mHandler.postDelayed(mRunnable, REFRESH_INTERVAL);
                }
            }.execute();
        }
    };
    //==============================================================================================

    //=====================================3.手势监听==================================================
    //单击监听:对焦
    private GestureDetector.OnGestureListener mGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if (mSurfaceView.getWidth() > 0 && mSurfaceView.getHeight() > 0) {
                float x = motionEvent.getX() / mSurfaceView.getWidth();
                float y = motionEvent.getY() / mSurfaceView.getHeight();
                try {
                    mAlivcLivePusher.focusCameraAtAdjustedPoint(x, y, true);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            if (motionEvent == null || motionEvent1 == null) {
                return false;
            }
            if (motionEvent.getX() - motionEvent1.getX() > FLING_MIN_DISTANCE
                    && Math.abs(v) > 0) {
                // Fling left
            } else if (motionEvent1.getX() - motionEvent.getX() > FLING_MIN_DISTANCE
                    && Math.abs(v) > 0) {
                // Fling right
            }
            return false;
        }
    };

    private float scaleFactor = 1.0f;
    //缩放监听器
    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if (scaleGestureDetector.getScaleFactor() > 1) {
                scaleFactor += 0.5;
            } else {
                scaleFactor -= 2;
            }
            if (scaleFactor <= 1) {
                scaleFactor = 1;
            }
            try {
                if (scaleFactor >= mAlivcLivePusher.getMaxZoom()) {
                    scaleFactor = mAlivcLivePusher.getMaxZoom();
                }
                mAlivcLivePusher.setZoom((int) scaleFactor);

            } catch (IllegalStateException e) {

            }
            return false;
        }
    };

    //==============================================================================================

}
