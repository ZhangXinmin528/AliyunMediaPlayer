package com.example.aliyunlivedemo.live.push.push_3_0.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePusher;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.live.push.push_3_0.ali.AliLivePushFlowActivity;
import com.example.aliyunlivedemo.util.LiveConfig;

import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK;
import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;

/**
 * Created by ZhangXinmin on 2017/11/21.
 * Copyright (c) 2017 . All rights reserved.
 */

public class LivePushFragment extends Fragment implements Runnable, View.OnClickListener {

    private static final String TAG = LivePushFragment.class.getSimpleName();

    private static final long REFRESH_INTERVAL = 2000;

    private Context mContext;
    private AlivcLivePusher mAlivcLivePusher = null;
    //参数
    private String mPushUrl = null;
    private SurfaceView mSurfaceView;
    private boolean mAsync = false;
    private boolean mAudio = false;
    private boolean isPushing = false;
    private int mCameraId;
    private boolean isFlash = false;
    private boolean flashState;
    //控件
    private ImageView mExit;
    private ImageView mVolume;
    private ImageView mFlash;
    private ImageView mCamera;
    private ImageView mBeautyButton;
    private LinearLayout mTopBar;
    private TextView mIsPushing;

    private Button mPreviewButton;
    private Button mPushButton;
    private Button mOperaButton;
    private Button mRestartButton;
    private Handler mHandler;

    private AliLivePushFlowActivity.PauseState mStateListener;

    public static LivePushFragment newInstance(String url, boolean async,
                                               boolean mAudio, int cameraId,
                                               boolean isFlash) {
        LivePushFragment fragment = new LivePushFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LiveConfig.LIVE_URL.name(), url);
        bundle.putBoolean(LiveConfig.IS_ANSYC.name(), async);
        bundle.putBoolean(LiveConfig.IS_AUDIO_ONLY.name(), mAudio);
        bundle.putInt(LiveConfig.CAMERA_ID.name(), cameraId);
        bundle.putBoolean(LiveConfig.IS_FLASH_ON.name(), isFlash);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParamsAndValues();
    }

    //init params and values
    private void initParamsAndValues() {
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mHandler = new Handler();
        //init params
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(LiveConfig.LIVE_URL.name())) {
                mPushUrl = getArguments().getString(LiveConfig.LIVE_URL.name());
            }
            if (bundle.containsKey(LiveConfig.IS_ANSYC.name())) {
                mAsync = getArguments().getBoolean(LiveConfig.IS_ANSYC.name(), false);
            }
            if (bundle.containsKey(LiveConfig.IS_AUDIO_ONLY.name())) {
                mAudio = getArguments().getBoolean(LiveConfig.IS_AUDIO_ONLY.name(), false);
            }
            if (bundle.containsKey(LiveConfig.CAMERA_ID.name())) {
                mCameraId = getArguments().getInt(LiveConfig.CAMERA_ID.name());
            }
            if (bundle.containsKey(LiveConfig.IS_FLASH_ON.name())) {
                isFlash = getArguments().getBoolean(LiveConfig.IS_FLASH_ON.name(), false);
            }
            flashState = isFlash;
        }

        //初始化推流监听器
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.setLivePushInfoListener(mPushInfoListener);
            mAlivcLivePusher.setLivePushErrorListener(mPushErrorListener);
            mAlivcLivePusher.setLivePushNetworkListener(mPushNetworkListener);
            isPushing = mAlivcLivePusher.isPushing();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live_push, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mExit = (ImageView) view.findViewById(R.id.exit);
        mExit.setOnClickListener(this);
        mVolume = (ImageView) view.findViewById(R.id.volume);
        mVolume.setOnClickListener(this);
        mVolume.setSelected(true);
        mFlash = (ImageView) view.findViewById(R.id.flash);
        mFlash.setOnClickListener(this);
        mFlash.setSelected(isFlash);
        mCamera = (ImageView) view.findViewById(R.id.camera);
        mCamera.setOnClickListener(this);
        mCamera.setSelected(true);

        mPreviewButton = (Button) view.findViewById(R.id.preview_button);
        mPreviewButton.setOnClickListener(this);
        mPreviewButton.setSelected(false);
        mPushButton = (Button) view.findViewById(R.id.push_button);
        mPushButton.setOnClickListener(this);
        mPushButton.setSelected(true);
        mOperaButton = (Button) view.findViewById(R.id.opera_button);
        mOperaButton.setOnClickListener(this);
        mOperaButton.setSelected(false);
        mBeautyButton = (ImageView) view.findViewById(R.id.beauty_button);
        mBeautyButton.setOnClickListener(this);
        mBeautyButton.setSelected(true);
        mRestartButton = (Button) view.findViewById(R.id.restart_button);
        mRestartButton.setOnClickListener(this);
        mTopBar = (LinearLayout) view.findViewById(R.id.top_bar);
        mIsPushing = (TextView) view.findViewById(R.id.isPushing);
        mIsPushing.setText(String.valueOf(isPushing));

        mTopBar.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mFlash.setClickable(mCameraId == CAMERA_TYPE_FRONT.getCameraId() ? false : true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(this);
    }

    public interface BeautyListener {
        void onBeautySwitch(boolean beauty);
    }

    private BeautyListener mBeautyListener = new BeautyListener() {
        @Override
        public void onBeautySwitch(boolean beauty) {
            if (mBeautyButton != null) {
                mBeautyButton.setSelected(beauty);
            }
        }
    };

    @Override
    public void run() {
        if (mIsPushing != null && mAlivcLivePusher != null) {
            try {
                isPushing = mAlivcLivePusher.isPushing();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mIsPushing.setText(String.valueOf(isPushing));
        }
        mHandler.postDelayed(this, REFRESH_INTERVAL);
    }

    //=========================================推流监听==============================================

    //推流通知监听器
    AlivcLivePushInfoListener mPushInfoListener = new AlivcLivePushInfoListener() {
        @Override
        public void onPreviewStarted(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.start_preview));
        }

        @Override
        public void onPreviewStoped(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.stop_preview));
        }

        @Override
        public void onPushStarted(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.start_push));
        }

        @Override
        public void onPushPauesed(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.pause_push));
        }

        @Override
        public void onPushResumed(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.resume_push));
        }

        @Override
        public void onPushStoped(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.stop_push));
        }

        @Override
        public void onPushRestarted(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.restart_success));
        }

        @Override
        public void onFirstFramePreviewed(AlivcLivePusher pusher) {
            showToast("推流通知监听器.." + getString(R.string.first_frame));
        }

        @Override
        public void onDropFrame(AlivcLivePusher pusher, int countBef, int countAft) {
            showToast("推流通知监听器.." + getString(R.string.drop_frame) + ", 丢帧前：" + countBef + ", 丢帧后：" + countAft);
        }

        @Override
        public void onAdjustBitRate(AlivcLivePusher pusher, int curBr, int targetBr) {
            showToast("推流通知监听器.." + getString(R.string.adjust_bitrate) + ", 当前码率：" + curBr + "Kps, 目标码率：" + targetBr + "Kps");
        }

        @Override
        public void onAdjustFps(AlivcLivePusher pusher, int curFps, int targetFps) {
            showToast("推流通知监听器.." + getString(R.string.adjust_fps) + ", 当前帧率：" + curFps + ", 目标帧率：" + targetFps);
        }
    };

    //推流错误监听器
    AlivcLivePushErrorListener mPushErrorListener = new AlivcLivePushErrorListener() {

        @Override
        public void onSystemError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            showDialog(getString(R.string.system_error) + error.toString());
        }

        @Override
        public void onSDKError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            if (error != null) {
                showDialog(getString(R.string.sdk_error) + error.toString());
            }
        }
    };

    //推流网络监听器
    AlivcLivePushNetworkListener mPushNetworkListener = new AlivcLivePushNetworkListener() {
        @Override
        public void onNetworkPoor(AlivcLivePusher pusher) {
            showNetWorkDialog(getString(R.string.network_poor));
        }

        @Override
        public void onNetworkRecovery(AlivcLivePusher pusher) {
            showToast(getString(R.string.network_recovery));
        }

        @Override
        public void onReconnectStart(AlivcLivePusher pusher) {

            showToastShort(getString(R.string.reconnect_start));
        }

        @Override
        public void onReconnectFail(AlivcLivePusher pusher) {

            showDialog(getString(R.string.reconnect_fail));
        }

        @Override
        public void onReconnectSucceed(AlivcLivePusher pusher) {
            showToast(getString(R.string.reconnect_success));
        }

        @Override
        public void onSendDataTimeout(AlivcLivePusher pusher) {
            showDialog(getString(R.string.senddata_timeout));
        }

        @Override
        public void onConnectFail(AlivcLivePusher pusher) {
            showDialog(getString(R.string.connect_fail));
        }
    };

    //==============================================================================================

    //========================================提示性方法=============================================
    private void showToast(final String text) {
        if (getActivity() == null || text == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void showToastShort(final String text) {
        if (getActivity() == null || text == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void showDialog(final String message) {
        if (getActivity() == null || message == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(getString(R.string.dialog_title));
                    dialog.setMessage(message);
                    dialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private void showNetWorkDialog(final String message) {
        if (getActivity() == null || message == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(getString(R.string.dialog_title));
                    dialog.setMessage(message);
                    dialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.setNeutralButton(getString(R.string.reconnect), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                mAlivcLivePusher.reconnectPushAsync();
                            } catch (IllegalStateException e) {

                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    //==============================================================================================


    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    public AlivcLivePusher getAlivcLivePusher() {
        return mAlivcLivePusher;
    }

    public void setAlivcLivePusher(AlivcLivePusher alivcLivePusher) {
        this.mAlivcLivePusher = alivcLivePusher;
    }

    public void setStateListener(AliLivePushFlowActivity.PauseState stateListener) {
        this.mStateListener = stateListener;
    }

    @Override
    public void onClick(View v) {
        if (mAlivcLivePusher == null) {
            if (getActivity() != null) {
                mAlivcLivePusher = ((AliLivePushFlowActivity) getActivity()).getAlivcLivePusher();
            }
            if (mAlivcLivePusher == null) {
                return;
            }
        }
        //点击事件
        try {
            switch (v.getId()) {
                case R.id.exit:
                    getActivity().finish();
                    break;
                case R.id.volume:
                    mAlivcLivePusher.setMute(mVolume.isSelected());
                    mVolume.post(new Runnable() {
                        @Override
                        public void run() {
                            mVolume.setSelected(!mVolume.isSelected());
                        }
                    });
                    break;
                case R.id.flash:
                    mAlivcLivePusher.setFlash(!mFlash.isSelected());
                    flashState = !mFlash.isSelected();
                    mFlash.post(new Runnable() {
                        @Override
                        public void run() {
                            mFlash.setSelected(!mFlash.isSelected());
                        }
                    });
                    break;
                case R.id.camera:
                    if (mCameraId == CAMERA_TYPE_FRONT.getCameraId()) {
                        mCameraId = CAMERA_TYPE_BACK.getCameraId();
                    } else {
                        mCameraId = CAMERA_TYPE_FRONT.getCameraId();
                    }
                    mAlivcLivePusher.switchCamera();
                    mFlash.post(new Runnable() {
                        @Override
                        public void run() {
                            mFlash.setClickable(mCameraId != CAMERA_TYPE_FRONT.getCameraId());
                            if (mCameraId == CAMERA_TYPE_FRONT.getCameraId()) {
                                mFlash.setSelected(false);
                            } else {
                                mFlash.setSelected(flashState);
                            }
                        }
                    });

                    break;
                case R.id.preview_button:
                    final boolean isPreview = mPreviewButton.isSelected();
                    if (mSurfaceView == null && getActivity() != null) {
                        mSurfaceView = ((AliLivePushFlowActivity) getActivity()).getSurfaceView();
                    }
                    if (!isPreview) {
                        mAlivcLivePusher.stopPreview();
                    } else {
                        if (mAsync) {
                            mAlivcLivePusher.startPreviewAysnc(mSurfaceView);
                        } else {
                            mAlivcLivePusher.startPreview(mSurfaceView);
                        }
                    }

                    mPreviewButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mPreviewButton.setText(isPreview ? getString(R.string.stop_preview_button) : getString(R.string.start_preview_button));
                            mPreviewButton.setSelected(!isPreview);
                        }
                    });

                    break;
                case R.id.push_button:
                    final boolean isPush = mPushButton.isSelected();
                    if (isPush) {
                        if (mAsync) {
                            mAlivcLivePusher.startPushAysnc(mPushUrl);
                        } else {
                            mAlivcLivePusher.startPush(mPushUrl);
                        }
                    } else {
                        mAlivcLivePusher.stopPush();
                        mOperaButton.post(new Runnable() {
                            @Override
                            public void run() {
                                mOperaButton.setText(getString(R.string.pause_button));
                                mOperaButton.setSelected(false);
                            }
                        });
                    }

                    mPushButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mPushButton.setText(isPush ? getString(R.string.stop_button) : getString(R.string.start_button));
                            mPushButton.setSelected(!isPush);
                        }
                    });

                    break;
                case R.id.opera_button:
                    final boolean isPause = mOperaButton.isSelected();
                    if (!isPause) {
                        mAlivcLivePusher.pause();
                    } else {
                        if (mAsync) {
                            mAlivcLivePusher.resumeAsync();
                        } else {
                            mAlivcLivePusher.resume();
                        }
                    }

                    if (mStateListener != null) {
                        mStateListener.updatePause(!isPause);
                    }
                    mOperaButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mOperaButton.setText(isPause ? getString(R.string.pause_button) : getString(R.string.resume_button));
                            mOperaButton.setSelected(!isPause);
                        }
                    });

                    break;
                case R.id.beauty_button:
                    PushBeautyDialog pushBeautyDialog = PushBeautyDialog.newInstance(mBeautyButton.isSelected());
                    pushBeautyDialog.setAlivcLivePusher(mAlivcLivePusher);
                    pushBeautyDialog.setBeautyListener(mBeautyListener);
                    pushBeautyDialog.show(getFragmentManager(), "beautyDialog");
                    break;
                case R.id.restart_button:
                    if (mAsync) {
                        mAlivcLivePusher.restartPushAync();
                    } else {
                        mAlivcLivePusher.restartPush();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            showDialog(e.getMessage());
            e.printStackTrace();
        }
    }

}
