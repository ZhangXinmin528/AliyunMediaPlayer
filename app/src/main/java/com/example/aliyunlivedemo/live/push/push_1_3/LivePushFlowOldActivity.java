package com.example.aliyunlivedemo.live.push.push_1_3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.livecloud.event.AlivcEvent;
import com.alibaba.livecloud.event.AlivcEventResponse;
import com.alibaba.livecloud.event.AlivcEventSubscriber;
import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.alibaba.livecloud.live.AlivcMediaRecorder;
import com.alibaba.livecloud.live.AlivcMediaRecorderFactory;
import com.alibaba.livecloud.live.AlivcRecordReporter;
import com.alibaba.livecloud.live.OnLiveRecordErrorListener;
import com.alibaba.livecloud.live.OnNetworkStatusListener;
import com.alibaba.livecloud.live.OnRecordStatusListener;
import com.alibaba.livecloud.model.AlivcWatermark;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.listener.ReportListener;
import com.example.aliyunlivedemo.listener.SimpleOnScaleGestureListener;
import com.example.aliyunlivedemo.util.LiveConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhangXinmin on 2017/11/18.
 * Copyright (c) 2017 . All rights reserved.
 * 直播推流界面
 * SDK1.3版本的直播界面
 */

public class LivePushFlowOldActivity extends BaseActivity {
    private static final String TAG = LivePushFlowOldActivity.class.getSimpleName();

    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 1;
    //需要的权限
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    //延迟
    private static final long PERMISSION_DELAY = 100;

    private Context mContext;
    private SurfaceView mCameraSurface;
    private AlivcMediaRecorder mMediaRecorder;
    private AlivcRecordReporter mRecordReporter;
    private boolean mHasPermission;//权限
    private Map<String, Object> mConfigureMap;
    //预览画面尺寸
    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    //跳转参数
    private String pushUrl;//推流地址
    private int videoResolution;//视频分辨率
    private boolean screenOrientation;//是否横屏
    private int cameraFacing;//前置摄像头
    private AlivcWatermark mWatermark;
    private int bestBitrate;//比特率
    private int minBitrate;
    private int maxBitrate;
    private int initBitrate;
    private int frameRate;//帧率

    private GestureDetector mGestureDetector;//手势
    private ScaleGestureDetector mScaleDetector;
    //数据统计
    private DataStatistics mDataStatistics;
    private boolean isRecording;//是否在推流

    //日志打印
    private TextView mPushLogTv;

    @Override
    protected Object setLayout() {
        return R.layout.activity_live_push_old;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;
        //权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            mHasPermission = true;
        }

        //init params
        getExtraParams();

        //设置横竖屏
        setRequestedOrientation(screenOrientation ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mConfigureMap = new HashMap<>();
        mDataStatistics = new DataStatistics(1000);
    }

    /**
     * get extra params
     *
     * @hide
     */
    private void getExtraParams() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                pushUrl = bundle.getString(LiveConfig.URL.name());
                videoResolution = bundle.getInt(LiveConfig.VIDEO_RESOLUTION.name());
                screenOrientation = bundle.getBoolean(LiveConfig.SCREEN_ORIENTATION.name());
                cameraFacing = bundle.getInt(LiveConfig.CAMERA_FACING.name());
                mWatermark = new AlivcWatermark.Builder()
                        .watermarkUrl(bundle.getString(LiveConfig.WATERMARK_URL.name()))
                        .paddingX(bundle.getInt(LiveConfig.WATERMARK_PADDINGX.name()))
                        .paddingY(bundle.getInt(LiveConfig.WATERMARK_PADDINGY.name()))
                        .site(bundle.getInt(LiveConfig.WATERMARK_LOCATION.name()))
                        .build();
                minBitrate = bundle.getInt(LiveConfig.MIN_BITRATE.name());
                maxBitrate = bundle.getInt(LiveConfig.MAX_BITRATE.name());
                bestBitrate = bundle.getInt(LiveConfig.BEST_BITRATE.name());
                initBitrate = bundle.getInt(LiveConfig.INIT_BITRATE.name());
                frameRate = bundle.getInt(LiveConfig.FRAME_RATE.name());
                Log.e(TAG, "getExtraParams:" +
                        "\n..pushUrl:" + pushUrl +
                        "\n..videoResolution:" + videoResolution +
                        "\n..cameraFacing:" + cameraFacing +
                        "\n..screenOrientation:" + screenOrientation +
                        "\n..WATERMARK_URL:" + bundle.getString(LiveConfig.WATERMARK_URL.name()) +
                        "\n..WATERMARK_LOCATION:" + bundle.getInt(LiveConfig.WATERMARK_LOCATION.name()) +
                        "\n..minBitrate:" + minBitrate +
                        "\n..maxBitrate:" + maxBitrate +
                        "\n..bestBitrate:" + bestBitrate +
                        "\n..initBitrate:" + initBitrate +
                        "\n..frameRate:" + frameRate
                );
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        super.initViews();
        //日志信息
        mPushLogTv = findViewById(R.id.tv_push_performance_log);
        //surfaceview
        mCameraSurface = findViewById(R.id.surfaceview_live_push_old);
        mCameraSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.e(TAG, "surfaceCreated");
                holder.setKeepScreenOn(true);
                startPreview(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e(TAG, "surfaceChanged");
                mMediaRecorder.setPreviewSize(width, height);
                mPreviewWidth = width;
                mPreviewHeight = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e(TAG, "surfaceDestroyed");
                mMediaRecorder.stopRecord();
                mMediaRecorder.reset();
            }
        });

        //缩放，对焦
        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            //单击:对焦
            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                if (mPreviewWidth > 0 && mPreviewHeight > 0) {
                    float x = motionEvent.getX() / mPreviewWidth;
                    float y = motionEvent.getY() / mPreviewHeight;
                    mMediaRecorder.focusing(x, y);
                }
                return true;
            }
        });
        //缩放
        mScaleDetector = new ScaleGestureDetector(mContext, new SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mMediaRecorder.setZoom(detector.getScaleFactor());
                return true;
            }
        });
        //添加手势事件
        mCameraSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.e(TAG, "OnTouchListener..onTouch");
                mGestureDetector.onTouchEvent(motionEvent);
                mScaleDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        //初始化示例
        mMediaRecorder = AlivcMediaRecorderFactory.createMediaRecorder();
        mMediaRecorder.init(mContext);
        mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);//开启美颜
        mDataStatistics.setReportListener(new ReportListener() {
            @Override
            public void onInfoReport() {
                runOnUiThread(mLoggerReportRunnable);
            }
        });

        /**
         * this method only can be called after mMediaRecorder.init(),
         * otherwise will return null;
         */
        mRecordReporter = mMediaRecorder.getRecordReporter();
        mDataStatistics.start();//开始统计数据
        //记录状态监听
        mMediaRecorder.setOnRecordStatusListener(mRecordStatusListener);
        //网络状态监听
        mMediaRecorder.setOnNetworkStatusListener(mNetworkStatusListener);
        //记录失败监听
        mMediaRecorder.setOnRecordErrorListener(mRecordErrorListener);

        //进行基本配置
        mConfigureMap.put(AlivcMediaFormat.KEY_CAMERA_FACING, cameraFacing);//摄像头
        mConfigureMap.put(AlivcMediaFormat.KEY_MAX_ZOOM_LEVEL, 3);//缩放比率
        mConfigureMap.put(AlivcMediaFormat.KEY_OUTPUT_RESOLUTION, videoResolution);//视频分辨率
        mConfigureMap.put(AlivcMediaFormat.KEY_MAX_VIDEO_BITRATE, maxBitrate * 1000);//最大比特率
        mConfigureMap.put(AlivcMediaFormat.KEY_BEST_VIDEO_BITRATE, bestBitrate * 1000);//最佳比特率
        mConfigureMap.put(AlivcMediaFormat.KEY_MIN_VIDEO_BITRATE, minBitrate * 1000);//最小比特率
        mConfigureMap.put(AlivcMediaFormat.KEY_INITIAL_VIDEO_BITRATE, initBitrate * 1000);
        mConfigureMap.put(AlivcMediaFormat.KEY_DISPLAY_ROTATION,//横屏还是竖屏录制
                screenOrientation ? AlivcMediaFormat.DISPLAY_ROTATION_90 : AlivcMediaFormat.DISPLAY_ROTATION_0);
        mConfigureMap.put(AlivcMediaFormat.KEY_EXPOSURE_COMPENSATION, -1);//曝光度
        mConfigureMap.put(AlivcMediaFormat.KEY_WATERMARK, mWatermark);//水印
        mConfigureMap.put(AlivcMediaFormat.KEY_FRAME_RATE, frameRate);//帧率

    }

    @Override
    protected void onResume() {
        super.onResume();
        initSubscriberEvent();

        try {
            mMediaRecorder.startRecord(pushUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRecording = true;
    }


    /**
     * 初始化记录观察者
     *
     * @hide
     */
    private void initSubscriberEvent() {
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_BITRATE_DOWN, mBitrateDownRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_BITRATE_RAISE, mBitrateUpRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_AUDIO_CAPTURE_OPEN_SUCC, mAudioCaptureSuccRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_DATA_DISCARD, mDataDiscardRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_INIT_DONE, mInitDoneRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_VIDEO_ENCODER_OPEN_SUCC, mVideoEncoderSuccRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_VIDEO_ENCODER_OPEN_FAILED, mVideoEncoderFailedRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_VIDEO_ENCODED_FRAMES_FAILED, mVideoEncodeFrameFailedRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_AUDIO_ENCODED_FRAMES_FAILED, mAudioEncodeFrameFailedRes));
        mMediaRecorder.subscribeEvent(
                generateEventSubscriber(AlivcEvent.EventType.EVENT_AUDIO_CAPTURE_OPEN_FAILED, mAudioCaptureOpenFailedRes));
        mMediaRecorder.resume();
    }

    @Override
    protected void onPause() {
        unSubscribeEvent();
        super.onPause();
    }

    /**
     * 解除事件监听
     *
     * @hide
     */
    private void unSubscribeEvent() {
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_BITRATE_DOWN);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_BITRATE_RAISE);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_AUDIO_CAPTURE_OPEN_SUCC);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_DATA_DISCARD);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_INIT_DONE);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_VIDEO_ENCODER_OPEN_SUCC);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_VIDEO_ENCODER_OPEN_FAILED);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_VIDEO_ENCODED_FRAMES_FAILED);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_AUDIO_ENCODED_FRAMES_FAILED);
        mMediaRecorder.unSubscribeEvent(AlivcEvent.EventType.EVENT_AUDIO_CAPTURE_OPEN_FAILED);
        mMediaRecorder.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataStatistics.stop();
        mMediaRecorder.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                boolean hasPermission = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        int tipsResId = 0;
                        if (Manifest.permission.CAMERA.equals(permissions[i])) {
                            tipsResId = R.string.no_camera_permission;
                        } else if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {
                            tipsResId = R.string.no_record_audio_permission;
                        }
                        if (tipsResId != 0) {
                            Toast.makeText(mContext, getString(tipsResId), Toast.LENGTH_SHORT).show();
                            hasPermission = false;
                        }
                    }
                }
                mHasPermission = hasPermission;
                break;
        }
    }

    /**
     * 生成阿里指定类型观察者
     *
     * @param eventType
     * @param eventResponse
     * @hide
     */
    private AlivcEventSubscriber generateEventSubscriber(int eventType, AlivcEventResponse eventResponse) {
        return new AlivcEventSubscriber(eventType, eventResponse);
    }

    /**
     * 检查权限
     *
     * @hide
     */
    private void checkPermission() {
        //是否授予权限
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (String permission : PERMISSIONS) {
            if (PermissionChecker.checkSelfPermission(mContext, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionCheck = PackageManager.PERMISSION_DENIED;
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        } else {
            mHasPermission = true;
        }
    }

    /**
     * 开始预览
     *
     * @param holder
     * @hide
     */
    private void startPreview(final SurfaceHolder holder) {
        //没有权限延迟执行该方法
        if (!mHasPermission) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startPreview(holder);
                }
            }, PERMISSION_DELAY);
            return;
        }
        mMediaRecorder.prepare(mConfigureMap, mCameraSurface);
        mMediaRecorder.setPreviewSize(mCameraSurface.getMeasuredWidth(), mCameraSurface.getMeasuredHeight());
        //开启美颜滤镜
        if ((int) mConfigureMap.get(AlivcMediaFormat.KEY_CAMERA_FACING) == AlivcMediaFormat.CAMERA_FACING_FRONT) {
            mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
        }
    }

    //===================================监听器=====================================================

    //推流记录状态监听器
    private OnRecordStatusListener mRecordStatusListener = new OnRecordStatusListener() {
        @Override
        public void onDeviceAttach() {

        }

        @Override
        public void onDeviceAttachFailed(int i) {

        }

        @Override
        public void onSessionAttach() {
            Log.e(TAG, "OnRecordStatusListener..onSessionAttach");
            if (isRecording && !TextUtils.isEmpty(pushUrl)) {
                mMediaRecorder.startRecord(pushUrl);
            }
            mMediaRecorder.focusing(0.5f, 0.5f);
        }

        @Override
        public void onSessionDetach() {

        }

        @Override
        public void onDeviceDetach() {

        }

        @Override
        public void onIllegalOutputResolution() {
            Log.d(TAG, "selected illegal output videoResolution");
            Toast.makeText(mContext, "选择输出的视频分辨率过大", Toast.LENGTH_SHORT).show();
        }
    };

    //网络状态监听
    private OnNetworkStatusListener mNetworkStatusListener = new OnNetworkStatusListener() {
        @Override
        public void onNetworkBusy() {
            Log.d(TAG, "OnNetworkStatusListener..onNetworkBusy");
            Toast.makeText(mContext, "网络状态较差", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNetworkFree() {
            Log.d(TAG, "OnNetworkStatusListener..onNetworkFree");
            Toast.makeText(mContext, "网络空闲状态", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectionStatusChange(int status) {
            Log.d(TAG, "OnNetworkStatusListener..onConnectionStatusChange：" + status);
        }

        @Override
        public boolean onNetworkReconnectFailed() {
            Log.d(TAG, "OnNetworkStatusListener..onNetworkReconnectFailed");
            showNetWorkFailedDialog(getString(R.string.all_push_net_connection_failed));
            return false;
        }
    };

    //记录失败监听
    private OnLiveRecordErrorListener mRecordErrorListener = new OnLiveRecordErrorListener() {
        @Override
        public void onError(int code) {
            Log.d(TAG, "OnLiveRecordErrorListener..onError:" + code);
        }
    };
    //==============================================================================================

    //======================================阿里直播观察者===========================================
    private AlivcEventResponse mBitrateDownRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Bundle bundle = event.getBundle();
            int preBitrate = bundle.getInt(AlivcEvent.EventBundleKey.KEY_PRE_BITRATE);
            int currBitrate = bundle.getInt(AlivcEvent.EventBundleKey.KEY_CURR_BITRATE);
            Log.d(TAG, "event->down bitrate, previous bitrate is " + preBitrate +
                    "current bitrate is " + currBitrate);
        }
    };

    private AlivcEventResponse mBitrateUpRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Bundle bundle = event.getBundle();
            int preBitrate = bundle.getInt(AlivcEvent.EventBundleKey.KEY_PRE_BITRATE);
            int currBitrate = bundle.getInt(AlivcEvent.EventBundleKey.KEY_CURR_BITRATE);
            Log.d(TAG, "event->up bitrate, previous bitrate is " + preBitrate +
                    "current bitrate is " + currBitrate);
        }
    };

    private AlivcEventResponse mAudioCaptureSuccRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Log.d(TAG, "event->audio recorder start success");
        }
    };

    private AlivcEventResponse mDataDiscardRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Bundle bundle = event.getBundle();
            int discardFrames = 0;
            if (bundle != null) {
                discardFrames = bundle.getInt(AlivcEvent.EventBundleKey.KEY_DISCARD_FRAMES);
            }
            Log.d(TAG, "event->data discard, the frames num is " + discardFrames);
        }
    };

    private AlivcEventResponse mInitDoneRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Log.d(TAG, "event->live recorder initialize completely");
        }
    };

    private AlivcEventResponse mVideoEncoderSuccRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Log.d(TAG, "event->video encoder start success");
        }
    };

    private AlivcEventResponse mVideoEncoderFailedRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Log.d(TAG, "event->video encoder start failed");
        }
    };

    private AlivcEventResponse mVideoEncodeFrameFailedRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Log.d(TAG, "event->video encode frame failed");
        }
    };

    private AlivcEventResponse mAudioEncodeFrameFailedRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Log.d(TAG, "event-> audio encode frame failed");
        }
    };

    private AlivcEventResponse mAudioCaptureOpenFailedRes = new AlivcEventResponse() {
        @Override
        public void onEvent(AlivcEvent event) {
            Log.d(TAG, "event-> audio capture device open failed");
        }
    };

    //==============================================================================================

    //======================================直播日志：===============================================
    private Runnable mLoggerReportRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecordReporter != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.VIDEO_CAPTURE_FPS) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_CAPTURE_FPS) + "fps\n");
                sb.append(getString(R.string.AUDIO_ENCODER_FPS) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_ENCODER_FPS) + "fps\n");
                sb.append(getString(R.string.VIDEO_ENCODER_FPS) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_ENCODER_FPS) + "fps\n");
                sb.append(getString(R.string.OUTPUT_BITRATE) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.OUTPUT_BITRATE) * 8 + "bps\n");
                sb.append(getString(R.string.AV_OUTPUT_DIFF) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.AV_OUTPUT_DIFF) + "microseconds\n");
                sb.append(getString(R.string.AUDIO_OUTPUT_FPS) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_OUTPUT_FPS) + "fps\n");
                sb.append(getString(R.string.VIDEO_OUTPUT_FPS) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_OUTPUT_FPS) + "fps\n");
                sb.append(getString(R.string.VIDEO_DELAY_DURATION) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.VIDEO_DELAY_DURATION) + "microseconds\n");
                sb.append(getString(R.string.AUDIO_DELAY_DURATION) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.AUDIO_DELAY_DURATION) + "microseconds\n");
                sb.append(getString(R.string.VIDEO_CACHE_FRAME_CNT) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_CACHE_FRAME_CNT) + "\n");
                sb.append(getString(R.string.AUDIO_CACHE_FRAME_CNT) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_CACHE_FRAME_CNT) + "\n");
                sb.append(getString(R.string.VIDEO_CACHE_BYTE_SIZE) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.VIDEO_CACHE_BYTE_SIZE) + "byte\n");
                sb.append(getString(R.string.AUDIO_CACHE_BYTE_SIZE) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.AUDIO_CACHE_BYTE_SIZE) + "byte\n");
                sb.append(getString(R.string.VIDEO_FRAME_DISCARD_CNT) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_FRAME_DISCARD_CNT) + "\n");
                sb.append(getString(R.string.AUDIO_FRAME_DISCARD_CNT) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_FRAME_DISCARD_CNT) + "\n");
                sb.append(getString(R.string.CUR_VIDEO_BUEATY_DURATION) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.CUR_VIDEO_BEAUTY_DURATION) + "ms\n");
                sb.append(getString(R.string.CUR_VIDEO_ENCODER_DURATION) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.CUR_VIDEO_ENCODER_DURATION) + "ms\n");
                sb.append(getString(R.string.cur_video_encode_bitrate) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.CUR_VIDEO_ENCODE_BITRATE) * 8 + "bps\n");
                sb.append(getString(R.string.VIDEO_OUTPUT_FRAME_COUNT) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_OUTPUT_FRAME_COUNT) + "\n");
                sb.append(getString(R.string.VIDEO_DATA) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.VIDEO_OUTPUT_DATA_SIZE) + "\n");
                sb.append(getString(R.string.VIDEO_BUFFER_COUNT) + "\n");
                sb.append(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_BUFFER_COUNT) + "\n");
                sb.append(getString(R.string.AUDIO_DATA) + "\n");
                sb.append(mRecordReporter.getLong(AlivcRecordReporter.AUDIO_OUTPUT_DATA_SIZE) + "\n");
                mPushLogTv.setText(sb.toString());
            }
        }
    };
    //==============================================================================================

    /**
     * 网络连接失败对话框对话框
     * 停止记录
     *
     * @param tips
     * @hide
     */
    private void showNetWorkFailedDialog(String tips) {
        if (!TextUtils.isEmpty(tips)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            AlertDialog dialog = builder
                    .setTitle(R.string.all_dialog_tips)
                    .setMessage(tips)
                    .setPositiveButton(R.string.all_dialog_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMediaRecorder.stopRecord();
                        }
                    })
                    .create();
            dialog.show();
        }
    }
}
