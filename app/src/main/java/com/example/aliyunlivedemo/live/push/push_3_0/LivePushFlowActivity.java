package com.example.aliyunlivedemo.live.push.push_3_0;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.LogUtil;
import com.alivc.live.pusher.SurfaceStatus;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.adapter.LivePushFragmentAdapter;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.listener.SimpleOnGestureListener;
import com.example.aliyunlivedemo.listener.SimpleOnScaleGestureListener;
import com.example.aliyunlivedemo.live.push.push_3_0.fragment.LivePushFragment;
import com.example.aliyunlivedemo.live.push.push_3_0.fragment.PushDiagramStatsFragment;
import com.example.aliyunlivedemo.live.push.push_3_0.fragment.PushTextStatsFragment;
import com.example.aliyunlivedemo.util.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_LEFT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT;

/**
 * Created by ZhangXinmin on 2017/11/21.
 * Copyright (c) 2017 . All rights reserved.
 * 视频推流界面V3.0版本
 */

public class LivePushFlowActivity extends BaseActivity {
    private static final String TAG = LivePushFlowActivity.class.getSimpleName();

    private static final int FLING_MIN_DISTANCE = 50;
    private static final int FLING_MIN_VELOCITY = 0;
    private final long REFRESH_INTERVAL = 1000;
    private static final String URL_KEY = "url_key";
    private static final String ASYNC_KEY = "async_key";
    private static final String AUDIO_ONLY_KEY = "audio_only_key";
    private static final String ORIENTATION_KEY = "orientation_key";
    private static final String CAMERA_ID = "camera_id";
    private static final String FLASH_ON = "flash_on";
    public static final int REQ_CODE_PUSH = 0x1112;

    private Context mContext;
    //参数
    private String mPushUrl = null;
    private boolean mAsync = false;
    private boolean mAudioOnly = false;
    private int mOrientation = ORIENTATION_PORTRAIT.ordinal();
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean mFlash = false;

    //控件
    private SurfaceView mSurfaceView;
    private SurfaceStatus mSurfaceStatus;
    //直播
    private AlivcLivePusher mAlivcLivePusher;
    private AlivcLivePushConfig mAlivcLivePushConfig;
    private LivePushFragment mLivePushFragment;
    private PushTextStatsFragment mPushTextStatsFragment;
    private PushDiagramStatsFragment mPushDiagramStatsFragment;
    //直播推流状态信息
    private AlivcLivePushStatsInfo alivcLivePushStatsInfo;
    private boolean isPause;

    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;
    private LivePushFragmentAdapter mLivePushFragmentAdapter;
    //手势监听
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private Handler mHandler;
    private int mNetWorkState;//网络状态

    @Override
    protected Object setLayout() {
        return R.layout.activity_live_push_new;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;
        mHandler = new Handler();
        //获取参数
        Intent intent = getIntent();
        if (intent != null) {
            mPushUrl = intent.getStringExtra(URL_KEY);
            mAsync = intent.getBooleanExtra(ASYNC_KEY, false);
            mAudioOnly = intent.getBooleanExtra(AUDIO_ONLY_KEY, false);
            mOrientation = intent.getIntExtra(ORIENTATION_KEY, ORIENTATION_PORTRAIT.ordinal());
            mCameraId = intent.getIntExtra(CAMERA_ID, Camera.CameraInfo.CAMERA_FACING_FRONT);
            mFlash = intent.getBooleanExtra(FLASH_ON, false);
            mAlivcLivePushConfig = (AlivcLivePushConfig) intent.getSerializableExtra(AlivcLivePushConfig.Config);
        }

        mSurfaceStatus = SurfaceStatus.UNINITED;
        //设置横竖屏
        setOrientation(mOrientation);
        //初始化直播推流对象
        initLivePusher();

        //init live fragment
        mFragmentList = new ArrayList<>();
        mLivePushFragment = LivePushFragment.newInstance(mPushUrl, mAsync, mAudioOnly, mCameraId, mFlash);
        mLivePushFragment.setAlivcLivePusher(mAlivcLivePusher);
        mLivePushFragment.setStateListener(mStateListener);

        //日志信息
        mPushTextStatsFragment = new PushTextStatsFragment();
        mPushDiagramStatsFragment = new PushDiagramStatsFragment();

        //添加手势
        mGestureDetector = new GestureDetector(mContext, mGestureListener);
        mScaleDetector = new ScaleGestureDetector(mContext, mScaleGestureListener);
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

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                showDialog(mContext, e.getMessage());
            } catch (IllegalStateException e) {
                e.printStackTrace();
                showDialog(mContext, e.getMessage());
            }
            mAlivcLivePusher.init(mContext, mAlivcLivePushConfig);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        super.initViews();
        mSurfaceView = findViewById(R.id.surfaceview_live_push_new);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.e(TAG, "surfaceCreated");
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
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e(TAG, "surfaceChanged");
                mSurfaceStatus = SurfaceStatus.CHANGED;
                if (mLivePushFragment != null) {
                    mLivePushFragment.setSurfaceView(mSurfaceView);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e(TAG, "surfaceDestroyed");
                mSurfaceStatus = SurfaceStatus.DESTROYED;
            }
        });

        //init viewpager
        initViewPager();

        //网络状态
        mNetWorkState = NetWorkUtils.getAPNType(mContext);
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
        mLivePushFragmentAdapter = new LivePushFragmentAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mLivePushFragmentAdapter);
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
//        unregisterReceiver(mChangedReceiver);
        mFragmentList = null;
        mSurfaceView = null;
        mViewPager = null;
        mLivePushFragmentAdapter = null;
        mGestureDetector = null;
        mScaleDetector = null;
        mLivePushFragment = null;
        mPushTextStatsFragment = null;
        mPushDiagramStatsFragment = null;
        mAlivcLivePushConfig = null;

        mAlivcLivePusher = null;

        alivcLivePushStatsInfo = null;
        super.onDestroy();
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public AlivcLivePusher getAlivcLivePusher() {
        return mAlivcLivePusher;
    }

    //=====================================1.监听器===================================================
    public interface PauseState {
        void updatePause(boolean state);
    }

    private PauseState mStateListener = new PauseState() {
        @Override
        public void updatePause(boolean state) {
            isPause = state;
        }
    };

    //==============================================================================================

    //=====================================2.推流日志信息==================================================
    private Runnable mRunnable = new Runnable() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void run() {
            LogUtil.d(TAG, "====== 日志信息..mRunnable run ======");

            new AsyncTask<AlivcLivePushStatsInfo, Void, AlivcLivePushStatsInfo>() {
                @Override
                protected AlivcLivePushStatsInfo doInBackground(AlivcLivePushStatsInfo... alivcLivePushStatsInfos) {
                    try {
                        alivcLivePushStatsInfo = mAlivcLivePusher.getLivePushStatsInfo();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    return alivcLivePushStatsInfo;
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
                    && Math.abs(v) > FLING_MIN_VELOCITY) {
                // Fling left
            } else if (motionEvent1.getX() - motionEvent.getX() > FLING_MIN_DISTANCE
                    && Math.abs(v) > FLING_MIN_VELOCITY) {
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

    //=====================================4.提示性方法================================================
    private void showDialog(Context context, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(getString(R.string.dialog_title));
        dialog.setMessage(message);
        dialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialog.show();
    }

    //==============================================================================================
    public static void startActivity(Activity activity, AlivcLivePushConfig alivcLivePushConfig,
                                     String url, boolean async,
                                     boolean audioOnly, AlivcPreviewOrientationEnum orientation,
                                     int cameraId, boolean isFlash) {
        Intent intent = new Intent(activity, LivePushFlowActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AlivcLivePushConfig.Config, alivcLivePushConfig);
        bundle.putString(URL_KEY, url);
        bundle.putBoolean(ASYNC_KEY, async);
        bundle.putBoolean(AUDIO_ONLY_KEY, audioOnly);
        bundle.putInt(ORIENTATION_KEY, orientation.ordinal());
        bundle.putInt(CAMERA_ID, cameraId);
        bundle.putBoolean(FLASH_ON, isFlash);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, REQ_CODE_PUSH);
    }
}
