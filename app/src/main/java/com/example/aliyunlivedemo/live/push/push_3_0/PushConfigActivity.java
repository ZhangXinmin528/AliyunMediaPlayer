package com.example.aliyunlivedemo.live.push.push_3_0;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.acker.simplezxing.activity.CaptureActivity;
import com.alivc.live.pusher.AlivcAudioSampleRateEnum;
import com.alivc.live.pusher.AlivcEncodeModeEnum;
import com.alivc.live.pusher.AlivcLivePushCameraTypeEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushConstants;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.alivc.live.pusher.WaterMarkInfo;
import com.example.aliyunlivedemo.R;

import java.util.ArrayList;

import static com.alivc.live.pusher.AlivcAudioChannelEnum.AUDIO_CHANNEL_ONE;
import static com.alivc.live.pusher.AlivcAudioChannelEnum.AUDIO_CHANNEL_TWO;
import static com.alivc.live.pusher.AlivcFpsEnum.FPS_10;
import static com.alivc.live.pusher.AlivcFpsEnum.FPS_12;
import static com.alivc.live.pusher.AlivcFpsEnum.FPS_15;
import static com.alivc.live.pusher.AlivcFpsEnum.FPS_20;
import static com.alivc.live.pusher.AlivcFpsEnum.FPS_25;
import static com.alivc.live.pusher.AlivcFpsEnum.FPS_30;
import static com.alivc.live.pusher.AlivcFpsEnum.FPS_8;
import static com.alivc.live.pusher.AlivcLivePushConstants.DEFAULT_VALUE_INT_AUDIO_RETRY_COUNT;
import static com.alivc.live.pusher.AlivcLivePushConstants.DEFAULT_VALUE_INT_RETRY_INTERVAL;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_LEFT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT;
import static com.alivc.live.pusher.AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT;
import static com.alivc.live.pusher.AlivcVideoEncodeGopEnum.GOP_FIVE;
import static com.alivc.live.pusher.AlivcVideoEncodeGopEnum.GOP_FOUR;
import static com.alivc.live.pusher.AlivcVideoEncodeGopEnum.GOP_ONE;
import static com.alivc.live.pusher.AlivcVideoEncodeGopEnum.GOP_THREE;
import static com.alivc.live.pusher.AlivcVideoEncodeGopEnum.GOP_TWO;
import static com.example.aliyunlivedemo.live.push.push_3_0.LivePushFlowActivity.REQ_CODE_PUSH;

public class PushConfigActivity extends AppCompatActivity {
    private static final String TAG = "PushConfigActivity";

    private AlivcResolutionEnum mDefinition = AlivcResolutionEnum.RESOLUTION_540P;
    private static final int REQ_CODE_PERMISSION = 0x1111;
    private static final int PROGRESS_0 = 0;
    private static final int PROGRESS_16 = 16;
    private static final int PROGRESS_20 = 20;
    private static final int PROGRESS_33 = 33;
    private static final int PROGRESS_40 = 40;
    private static final int PROGRESS_50 = 50;
    private static final int PROGRESS_60 = 60;
    private static final int PROGRESS_66 = 66;
    private static final int PROGRESS_75 = 75;
    private static final int PROGRESS_80 = 80;
    private static final int PROGRESS_100 = 100;

    private static final int PROGRESS_AUDIO_320 = 30;
    private static final int PROGRESS_AUDIO_441 = 70;
    private InputMethodManager manager;

    private LinearLayout mPublish;
    private SeekBar mResolution;
    private SeekBar mAudioRate;
    private SeekBar mFps;
    private SeekBar mMinFps;
    private TextView mResolutionText;
    private TextView mAudioRateText;
    private TextView mWaterPosition;
    private TextView mFpsText;
    private TextView mMinFpsText;

    private EditText mUrl;
    private EditText mTargetRate;
    private EditText mMinRate;
    private EditText mInitRate;
    private EditText mRetryInterval;
    private EditText mRetryCount;

    private Switch mWaterMark;
    private Switch mPushMirror;
    private Switch mPreviewMirror;
    private Switch mHardCode;
    private Switch mCamera;
    private Switch mAudioOnly;
    private Switch mAutoFocus;
    private Switch mBeautyOn;
    private Switch mAsync;
    private Switch mFlash;
    private Switch mLog;
    private ImageView mQr;
    private ImageView mBack;
    private RadioGroup mAudioRadio;
    private RadioGroup mGop;
    private RadioGroup mOrientation;

    //美颜相关数据
    private SeekBar mSaturationBar;
    private SeekBar mBrightnessBar;
    private SeekBar mWhiteBar;
    private SeekBar mSkinBar;
    private SeekBar mRuddyBar;

    private TextView mSaturation;
    private TextView mBrightness;
    private TextView mWhite;
    private TextView mSkin;
    private TextView mRuddy;

    private LinearLayout mWaterLinear;

    private AlivcLivePushConfig mAlivcLivePushConfig;
    private boolean mAsyncValue = false;
    private boolean mAudioOnlyPush = false;
    private AlivcPreviewOrientationEnum mOrientationEnum = ORIENTATION_PORTRAIT;

    private ArrayList<WaterMarkInfo> waterMarkInfos = new ArrayList<>();

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isFlash = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_push_config);
        mAlivcLivePushConfig = new AlivcLivePushConfig();
        initView();
        setClick();
        Common.copyAsset(this);
        addWaterMarkInfo();
    }

    private void initView() {
        mUrl = (EditText) findViewById(R.id.url_editor);
        mPublish = (LinearLayout) findViewById(R.id.beginPublish);
        mResolution = (SeekBar) findViewById(R.id.resolution_seekbar);
        mResolutionText = (TextView) findViewById(R.id.resolution_text);
        mFps = (SeekBar) findViewById(R.id.fps_seekbar);
        mFpsText = (TextView) findViewById(R.id.fps_text);
        mTargetRate = (EditText) findViewById(R.id.target_rate_edit);
        mMinRate = (EditText) findViewById(R.id.min_rate_edit);
        mInitRate = (EditText) findViewById(R.id.init_rate_edit);
        mAudioRate = (SeekBar) findViewById(R.id.audio_rate_seekbar);
        mAudioRateText = (TextView) findViewById(R.id.audio_rate_text);
        mRetryInterval = (EditText) findViewById(R.id.retry_interval);
        mRetryCount = (EditText) findViewById(R.id.retry_count);
        mMinFps = (SeekBar) findViewById(R.id.min_fps_seekbar);
        mMinFpsText = (TextView) findViewById(R.id.min_fps_text);
        mWaterMark = (Switch) findViewById(R.id.watermark_switch);
        mWaterPosition = (TextView) findViewById(R.id.water_position);
        mPushMirror = (Switch) findViewById(R.id.push_mirror_switch);
        mPreviewMirror = (Switch) findViewById(R.id.preview_mirror_switch);
        mHardCode = (Switch) findViewById(R.id.hard_switch);
        mCamera = (Switch) findViewById(R.id.camera_switch);
        mAudioOnly = (Switch) findViewById(R.id.audio_only_switch);
        mAutoFocus = (Switch) findViewById(R.id.autofocus_switch);
        mBeautyOn = (Switch) findViewById(R.id.beautyOn_switch);
        mAsync = (Switch) findViewById(R.id.async_switch);
        mFlash = (Switch) findViewById(R.id.flash_switch);
        mLog = (Switch) findViewById(R.id.log_switch);
        mQr = (ImageView) findViewById(R.id.qr_code);
        mBack = (ImageView) findViewById(R.id.iv_back);
        mAudioRadio = (RadioGroup) findViewById(R.id.main_audio);
        mGop = (RadioGroup) findViewById(R.id.main_gop);
        mOrientation = (RadioGroup) findViewById(R.id.main_orientation);

        mSaturationBar = (SeekBar) findViewById(R.id.beauty_saturation_seekbar);
        mBrightnessBar = (SeekBar) findViewById(R.id.beauty_brightness_seekbar);
        mWhiteBar = (SeekBar) findViewById(R.id.beauty_white_seekbar);
        mSkinBar = (SeekBar) findViewById(R.id.beauty_skin_seekbar);
        mRuddyBar = (SeekBar) findViewById(R.id.beauty_ruddy_seekbar);
        mSaturation = (TextView) findViewById(R.id.saturation);
        mBrightness = (TextView) findViewById(R.id.brightness);
        mWhite = (TextView) findViewById(R.id.white);
        mSkin = (TextView) findViewById(R.id.skin);
        mRuddy = (TextView) findViewById(R.id.ruddy);
        mWaterLinear = (LinearLayout) findViewById(R.id.water_linear);
        mTargetRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
        mMinRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate()));
        mInitRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
        SharedPreferenceUtils.setHintTargetBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate());
        SharedPreferenceUtils.setHintMinBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate());
    }

    private void setClick() {
        mPublish.setOnClickListener(onClickListener);
        mWaterPosition.setOnClickListener(onClickListener);
        mWaterMark.setOnCheckedChangeListener(onCheckedChangeListener);
        mPushMirror.setOnCheckedChangeListener(onCheckedChangeListener);
        mPreviewMirror.setOnCheckedChangeListener(onCheckedChangeListener);
        mHardCode.setOnCheckedChangeListener(onCheckedChangeListener);
        mCamera.setOnCheckedChangeListener(onCheckedChangeListener);
        mAudioOnly.setOnCheckedChangeListener(onCheckedChangeListener);
        mAutoFocus.setOnCheckedChangeListener(onCheckedChangeListener);
        mBeautyOn.setOnCheckedChangeListener(onCheckedChangeListener);
        mResolution.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mAudioRate.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mFps.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mMinFps.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mAsync.setOnCheckedChangeListener(onCheckedChangeListener);
        mFlash.setOnCheckedChangeListener(onCheckedChangeListener);
        mLog.setOnCheckedChangeListener(onCheckedChangeListener);
        mQr.setOnClickListener(onClickListener);
        mBack.setOnClickListener(onClickListener);
        mAudioRadio.setOnCheckedChangeListener(mAudioListener);
        mGop.setOnCheckedChangeListener(mGopListener);
        mOrientation.setOnCheckedChangeListener(mOrientationListener);
        mSaturationBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mBrightnessBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mWhiteBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSkinBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mRuddyBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.beginPublish:
                    if (getPushConfig() != null) {
                        LivePushFlowActivity.startActivity(PushConfigActivity.this,
                                mAlivcLivePushConfig, mUrl.getText().toString(), mAsyncValue,
                                mAudioOnlyPush, mOrientationEnum, mCameraId, isFlash);
                    }
                    break;
                case R.id.qr_code:
                    if (ContextCompat.checkSelfPermission(PushConfigActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // Do not have the permission of camera, request it.
                        ActivityCompat.requestPermissions(PushConfigActivity.this, new String[]{Manifest.permission.CAMERA}, REQ_CODE_PERMISSION);
                    } else {
                        // Have gotten the permission
                        startCaptureActivityForResult();
                    }
                    break;
                case R.id.water_position:
                    PushWaterMarkDialog pushWaterMarkDialog = new PushWaterMarkDialog();
                    pushWaterMarkDialog.setWaterMarkInfo(waterMarkInfos);
                    pushWaterMarkDialog.show(getSupportFragmentManager(), "waterDialog");
                    break;
                case R.id.iv_back:
                    finish();
                default:
                    break;
            }
        }
    };

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.watermark_switch) {
                if (mWaterPosition != null) {
                    mWaterPosition.setClickable(isChecked);
                    mWaterPosition.setTextColor(isChecked ? getResources().getColor(R.color.text_blue) : getResources().getColor(R.color.darker_gray));
                }
            } else if (id == R.id.push_mirror_switch) {
                mAlivcLivePushConfig.setPushMirror(isChecked);
                SharedPreferenceUtils.setPushMirror(getApplicationContext(), isChecked);
            } else if (id == R.id.preview_mirror_switch) {
                mAlivcLivePushConfig.setPreviewMirror(isChecked);
                SharedPreferenceUtils.setPreviewMirror(getApplicationContext(), isChecked);
            } else if (id == R.id.hard_switch) {
                mAlivcLivePushConfig.setVideoEncodeMode(isChecked ? AlivcEncodeModeEnum.Encode_MODE_HARD : AlivcEncodeModeEnum.Encode_MODE_SOFT);
            } else if (id == R.id.camera_switch) {
                mAlivcLivePushConfig.setCameraType(isChecked ? AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT : AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK);
                mCameraId = (isChecked ? AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT.getCameraId() : AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK.getCameraId());
            } else if (id == R.id.audio_only_switch) {
                mAudioOnlyPush = isChecked;
                mAlivcLivePushConfig.setAudioOnly(isChecked);
            } else if (id == R.id.autofocus_switch) {
                mAlivcLivePushConfig.setAutoFocus(isChecked);
                SharedPreferenceUtils.setAutofocus(getApplicationContext(), isChecked);
            } else if (id == R.id.beautyOn_switch) {
                mAlivcLivePushConfig.setBeautyOn(isChecked);
                SharedPreferenceUtils.setBeautyOn(getApplicationContext(), isChecked);
            } else if (id == R.id.async_switch) {
                mAsyncValue = isChecked;
            } else if (id == R.id.flash_switch) {
                mAlivcLivePushConfig.setFlash(isChecked);
                isFlash = isChecked;
            } else if (id == R.id.log_switch) {
                if (isChecked) {
                    LogcatHelper.getInstance(getApplicationContext()).start();
                } else {
                    LogcatHelper.getInstance(getApplicationContext()).stop();
                }
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int seekBarId = seekBar.getId();
            if (mResolution.getId() == seekBarId) {
                if (progress <= PROGRESS_0) {
                    mDefinition = AlivcResolutionEnum.RESOLUTION_180P;
                    mResolutionText.setText(R.string.setting_resolution_180P);
                    mTargetRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_180P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
                    mMinRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_180P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate()));
                    mInitRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_180P.DEFAULT_VALUE_INT_INIT_BITRATE.getBitrate()));
                    SharedPreferenceUtils.setHintTargetBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_180P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate());
                    SharedPreferenceUtils.setHintMinBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_180P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate());
                } else if (progress > PROGRESS_0 && progress <= PROGRESS_20) {
                    mDefinition = AlivcResolutionEnum.RESOLUTION_240P;
                    mResolutionText.setText(R.string.setting_resolution_240P);
                    mTargetRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_240P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
                    mMinRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_240P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate()));
                    mInitRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_240P.DEFAULT_VALUE_INT_INIT_BITRATE.getBitrate()));
                    SharedPreferenceUtils.setHintTargetBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_240P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate());
                    SharedPreferenceUtils.setHintMinBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_240P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate());
                } else if (progress > PROGRESS_20 && progress <= PROGRESS_40) {
                    mDefinition = AlivcResolutionEnum.RESOLUTION_360P;
                    mResolutionText.setText(R.string.setting_resolution_360P);
                    mTargetRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_360P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
                    mMinRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_360P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate()));
                    mInitRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_360P.DEFAULT_VALUE_INT_INIT_BITRATE.getBitrate()));
                    SharedPreferenceUtils.setHintTargetBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_360P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate());
                    SharedPreferenceUtils.setHintMinBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_360P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate());
                } else if (progress > PROGRESS_40 && progress <= PROGRESS_60) {
                    mDefinition = AlivcResolutionEnum.RESOLUTION_480P;
                    mResolutionText.setText(R.string.setting_resolution_480P);
                    mTargetRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_480P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
                    mMinRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_480P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate()));
                    mInitRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_480P.DEFAULT_VALUE_INT_INIT_BITRATE.getBitrate()));
                    SharedPreferenceUtils.setHintTargetBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_480P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate());
                    SharedPreferenceUtils.setHintMinBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_480P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate());
                } else if (progress > PROGRESS_60 && progress <= PROGRESS_80) {
                    mDefinition = AlivcResolutionEnum.RESOLUTION_540P;
                    mResolutionText.setText(R.string.setting_resolution_540P);
                    mTargetRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
                    mMinRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate()));
                    mInitRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_INIT_BITRATE.getBitrate()));
                    SharedPreferenceUtils.setHintTargetBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate());
                    SharedPreferenceUtils.setHintMinBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_540P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate());
                } else if (progress > PROGRESS_80) {
                    mDefinition = AlivcResolutionEnum.RESOLUTION_720P;
                    mResolutionText.setText(R.string.setting_resolution_720P);
                    mTargetRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_720P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate()));
                    mMinRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_720P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate()));
                    mInitRate.setHint(String.valueOf(AlivcLivePushConstants.BITRATE_720P.DEFAULT_VALUE_INT_INIT_BITRATE.getBitrate()));
                    SharedPreferenceUtils.setHintTargetBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_720P.DEFAULT_VALUE_INT_TARGET_BITRATE.getBitrate());
                    SharedPreferenceUtils.setHintMinBit(getApplicationContext(), AlivcLivePushConstants.BITRATE_720P.DEFAULT_VALUE_INT_MIN_BITRATE.getBitrate());
                }
            } else if (mAudioRate.getId() == seekBarId) {
                if (progress <= 50) {
                    mAlivcLivePushConfig.setAudioSamepleRate(AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_32000);
                    mAudioRateText.setText(getString(R.string.setting_audio_320));
                } else {
                    mAlivcLivePushConfig.setAudioSamepleRate(AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_44100);
                    mAudioRateText.setText(getString(R.string.setting_audio_441));
                }
            } else if (mFps.getId() == seekBarId) {
                if (progress <= PROGRESS_0) {
                    mAlivcLivePushConfig.setFps(FPS_8);
                    mFpsText.setText(String.valueOf(FPS_8.getFps()));
                } else if (progress > PROGRESS_0 && progress <= PROGRESS_16) {
                    mAlivcLivePushConfig.setFps(FPS_10);
                    mFpsText.setText(String.valueOf(FPS_10.getFps()));
                } else if (progress > PROGRESS_16 && progress <= PROGRESS_33) {
                    mAlivcLivePushConfig.setFps(FPS_12);
                    mFpsText.setText(String.valueOf(FPS_12.getFps()));
                } else if (progress > PROGRESS_33 && progress <= PROGRESS_50) {
                    mAlivcLivePushConfig.setFps(FPS_15);
                    mFpsText.setText(String.valueOf(FPS_15.getFps()));
                } else if (progress > PROGRESS_50 && progress <= PROGRESS_66) {
                    mAlivcLivePushConfig.setFps(FPS_20);
                    mFpsText.setText(String.valueOf(FPS_20.getFps()));
                } else if (progress > PROGRESS_66 && progress <= PROGRESS_80) {
                    mAlivcLivePushConfig.setFps(FPS_25);
                    mFpsText.setText(String.valueOf(FPS_25.getFps()));
                } else if (progress > PROGRESS_80) {
                    mAlivcLivePushConfig.setFps(FPS_30);
                    mFpsText.setText(String.valueOf(FPS_30.getFps()));
                }
            } else if (mMinFps.getId() == seekBarId) {
                if (progress <= PROGRESS_0) {
                    mAlivcLivePushConfig.setMinFps(FPS_8);
                    mMinFpsText.setText(String.valueOf(FPS_8.getFps()));
                } else if (progress > PROGRESS_0 && progress <= PROGRESS_16) {
                    mAlivcLivePushConfig.setMinFps(FPS_10);
                    mMinFpsText.setText(String.valueOf(FPS_10.getFps()));
                } else if (progress > PROGRESS_16 && progress <= PROGRESS_33) {
                    mAlivcLivePushConfig.setMinFps(FPS_12);
                    mMinFpsText.setText(String.valueOf(FPS_12.getFps()));
                } else if (progress > PROGRESS_33 && progress <= PROGRESS_50) {
                    mAlivcLivePushConfig.setMinFps(FPS_15);
                    mMinFpsText.setText(String.valueOf(FPS_15.getFps()));
                } else if (progress > PROGRESS_50 && progress <= PROGRESS_66) {
                    mAlivcLivePushConfig.setMinFps(FPS_20);
                    mMinFpsText.setText(String.valueOf(FPS_20.getFps()));
                } else if (progress > PROGRESS_66 && progress <= PROGRESS_80) {
                    mAlivcLivePushConfig.setMinFps(FPS_25);
                    mMinFpsText.setText(String.valueOf(FPS_25.getFps()));
                } else if (progress > PROGRESS_80) {
                    mAlivcLivePushConfig.setMinFps(FPS_30);
                    mMinFpsText.setText(String.valueOf(FPS_30.getFps()));
                }
            } else if (mSaturationBar.getId() == seekBarId) {
                mSaturation.setText(String.valueOf(progress));
                if (mAlivcLivePushConfig != null) {
                    mAlivcLivePushConfig.setBeautySaturation(progress);
                    SharedPreferenceUtils.setSaturation(getApplicationContext(), progress);
                }
            } else if (mBrightnessBar.getId() == seekBarId) {
                mBrightness.setText(String.valueOf(progress));
                if (mAlivcLivePushConfig != null) {
                    mAlivcLivePushConfig.setBeautyBrightness(progress);
                    SharedPreferenceUtils.setBrightness(getApplicationContext(), progress);
                }
            } else if (mWhiteBar.getId() == seekBarId) {
                mWhite.setText(String.valueOf(progress));
                if (mAlivcLivePushConfig != null) {
                    mAlivcLivePushConfig.setBeautyWhite(progress);
                    SharedPreferenceUtils.setWhiteValue(getApplicationContext(), progress);
                }
            } else if (mSkinBar.getId() == seekBarId) {
                mSkin.setText(String.valueOf(progress));
                if (mAlivcLivePushConfig != null) {
                    mAlivcLivePushConfig.setBeautyBuffing(progress);
                    SharedPreferenceUtils.setBuffing(getApplicationContext(), progress);
                }
            } else if (mRuddyBar.getId() == seekBarId) {
                mRuddy.setText(String.valueOf(progress));
                if (mAlivcLivePushConfig != null) {
                    mAlivcLivePushConfig.setBeautyRuddy(progress);
                    SharedPreferenceUtils.setRuddy(getApplicationContext(), progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (mResolution.getId() == seekBar.getId()) {
                if (progress < PROGRESS_0) {
                    seekBar.setProgress(0);
                } else if (progress > PROGRESS_0 && progress <= PROGRESS_20) {
                    seekBar.setProgress(PROGRESS_20);
                } else if (progress > PROGRESS_20 && progress <= PROGRESS_40) {
                    seekBar.setProgress(PROGRESS_40);
                } else if (progress > PROGRESS_40 && progress <= PROGRESS_60) {
                    seekBar.setProgress(PROGRESS_60);
                } else if (progress > PROGRESS_60 && progress <= PROGRESS_80) {
                    seekBar.setProgress(PROGRESS_80);
                } else if (progress > PROGRESS_80) {
                    seekBar.setProgress(PROGRESS_100);
                }
            } else if (mFps.getId() == seekBar.getId()) {

                if (progress <= PROGRESS_0) {
                    seekBar.setProgress(0);
                } else if (progress > PROGRESS_0 && progress <= PROGRESS_16) {
                    seekBar.setProgress(PROGRESS_16);
                } else if (progress > PROGRESS_16 && progress <= PROGRESS_33) {
                    seekBar.setProgress(PROGRESS_33);
                } else if (progress > PROGRESS_33 && progress <= PROGRESS_50) {
                    seekBar.setProgress(PROGRESS_50);
                } else if (progress > PROGRESS_50 && progress <= PROGRESS_66) {
                    seekBar.setProgress(PROGRESS_66);
                } else if (progress > PROGRESS_66 && progress <= PROGRESS_80) {
                    seekBar.setProgress(PROGRESS_80);
                } else if (progress > PROGRESS_80) {
                    seekBar.setProgress(PROGRESS_100);
                }
            } else if (mAudioRate.getId() == seekBar.getId()) {
                if (progress <= 50) {
                    seekBar.setProgress(PROGRESS_AUDIO_320);
                } else {
                    seekBar.setProgress(PROGRESS_AUDIO_441);
                }
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mAudioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            switch (i) {
                case R.id.audio_channel_one:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setAudioChannels(AUDIO_CHANNEL_ONE);
                    }
                    break;
                case R.id.audio_channel_two:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setAudioChannels(AUDIO_CHANNEL_TWO);
                    }
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mGopListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            switch (i) {
                case R.id.gop_one:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setVideoEncodeGop(GOP_ONE);
                    }
                    break;
                case R.id.gop_two:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setVideoEncodeGop(GOP_TWO);
                    }
                    break;
                case R.id.gop_three:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setVideoEncodeGop(GOP_THREE);
                    }
                    break;
                case R.id.gop_four:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setVideoEncodeGop(GOP_FOUR);
                    }
                    break;
                case R.id.gop_five:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setVideoEncodeGop(GOP_FIVE);
                    }
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mOrientationListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            switch (i) {
                case R.id.portrait:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setPreviewOrientation(ORIENTATION_PORTRAIT);
                        mOrientationEnum = ORIENTATION_PORTRAIT;
                    }
                    break;
                case R.id.home_left:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setPreviewOrientation(ORIENTATION_LANDSCAPE_HOME_LEFT);
                        mOrientationEnum = ORIENTATION_LANDSCAPE_HOME_LEFT;
                    }
                    break;
                case R.id.home_right:
                    if (mAlivcLivePushConfig != null) {
                        mAlivcLivePushConfig.setPreviewOrientation(ORIENTATION_LANDSCAPE_HOME_RIGHT);
                        mOrientationEnum = ORIENTATION_LANDSCAPE_HOME_RIGHT;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mWhite.setText(String.valueOf(SharedPreferenceUtils.getWhiteValue(getApplicationContext())));
        mWhiteBar.setProgress(SharedPreferenceUtils.getWhiteValue(getApplicationContext()));
        mSkin.setText(String.valueOf(SharedPreferenceUtils.getBuffing(getApplicationContext())));
        mSkinBar.setProgress(SharedPreferenceUtils.getBuffing(getApplicationContext()));
        mRuddy.setText(String.valueOf(SharedPreferenceUtils.getRuddy(getApplicationContext())));
        mRuddyBar.setProgress(SharedPreferenceUtils.getRuddy(getApplicationContext()));
        mSaturation.setText(String.valueOf(SharedPreferenceUtils.getSaturation(getApplicationContext())));
        mSaturationBar.setProgress(SharedPreferenceUtils.getSaturation(getApplicationContext()));
        mBrightness.setText(String.valueOf(SharedPreferenceUtils.getBrightness(getApplicationContext())));
        mBrightnessBar.setProgress(SharedPreferenceUtils.getBrightness(getApplicationContext()));
        mPushMirror.setChecked(SharedPreferenceUtils.isPushMirror(getApplicationContext()));
        mPreviewMirror.setChecked(SharedPreferenceUtils.isPreviewMirror(getApplicationContext()));
        mAutoFocus.setChecked(SharedPreferenceUtils.isAutoFocus(getApplicationContext()));
        mBeautyOn.setChecked(SharedPreferenceUtils.isBeautyOn(getApplicationContext()));
    }

    private void startCaptureActivityForResult() {
        Intent intent = new Intent(PushConfigActivity.this, CaptureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CaptureActivity.KEY_NEED_BEEP, CaptureActivity.VALUE_BEEP);
        bundle.putBoolean(CaptureActivity.KEY_NEED_VIBRATION, CaptureActivity.VALUE_VIBRATION);
        bundle.putBoolean(CaptureActivity.KEY_NEED_EXPOSURE, CaptureActivity.VALUE_NO_EXPOSURE);
        bundle.putByte(CaptureActivity.KEY_FLASHLIGHT_MODE, CaptureActivity.VALUE_FLASHLIGHT_OFF);
        bundle.putByte(CaptureActivity.KEY_ORIENTATION_MODE, CaptureActivity.VALUE_ORIENTATION_AUTO);
        bundle.putBoolean(CaptureActivity.KEY_SCAN_AREA_FULL_SCREEN, CaptureActivity.VALUE_SCAN_AREA_FULL_SCREEN);
        bundle.putBoolean(CaptureActivity.KEY_NEED_SCAN_HINT_TEXT, CaptureActivity.VALUE_SCAN_HINT_TEXT);
        intent.putExtra(CaptureActivity.EXTRA_SETTING_BUNDLE, bundle);
        startActivityForResult(intent, CaptureActivity.REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User agree the permission
                    startCaptureActivityForResult();
                } else {
                    // User disagree the permission
                    Toast.makeText(this, "You must agree the camera permission request before you use the code scan function", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    private AlivcLivePushConfig getPushConfig() {
        if (mUrl.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.url_empty), Toast.LENGTH_LONG).show();
            return null;
        }
        mAlivcLivePushConfig.setResolution(mDefinition);
        if (!mInitRate.getText().toString().isEmpty()) {
            mAlivcLivePushConfig.setInitialVideoBitrate(Integer.valueOf(mInitRate.getText().toString()));
        } else {
            mAlivcLivePushConfig.setInitialVideoBitrate(Integer.valueOf(mInitRate.getHint().toString()));
        }

        if (!mMinRate.getText().toString().isEmpty()) {
            mAlivcLivePushConfig.setMinVideoBitrate(Integer.valueOf(mMinRate.getText().toString()));
            SharedPreferenceUtils.setMinBit(getApplicationContext(), Integer.valueOf(mMinRate.getText().toString()));
        } else {
            mAlivcLivePushConfig.setMinVideoBitrate(Integer.valueOf(mMinRate.getHint().toString()));
            SharedPreferenceUtils.setMinBit(getApplicationContext(), Integer.valueOf(mMinRate.getHint().toString()));
        }

        if (!mTargetRate.getText().toString().isEmpty()) {
            mAlivcLivePushConfig.setTargetVideoBitrate(Integer.valueOf(mTargetRate.getText().toString()));
            SharedPreferenceUtils.setTargetBit(getApplicationContext(), Integer.valueOf(mTargetRate.getText().toString()));
        } else {
            mAlivcLivePushConfig.setTargetVideoBitrate(Integer.valueOf(mTargetRate.getHint().toString()));
            SharedPreferenceUtils.setTargetBit(getApplicationContext(), Integer.valueOf(mTargetRate.getHint().toString()));
        }

        if (!mRetryCount.getText().toString().isEmpty()) {
            mAlivcLivePushConfig.setConnectRetryCount(Integer.valueOf(mRetryCount.getText().toString()));
        } else {
            mAlivcLivePushConfig.setConnectRetryCount(DEFAULT_VALUE_INT_AUDIO_RETRY_COUNT);
        }

        if (!mRetryInterval.getText().toString().isEmpty()) {
            mAlivcLivePushConfig.setConnectRetryInterval(Integer.valueOf(mRetryInterval.getText().toString()));
        } else {
            mAlivcLivePushConfig.setConnectRetryInterval(DEFAULT_VALUE_INT_RETRY_INTERVAL);
        }

        if (mWaterMark.isChecked()) {
            for (int i = 0; i < waterMarkInfos.size(); i++) {
                mAlivcLivePushConfig.addWaterMark(waterMarkInfos.get(i).mWaterMarkPath, waterMarkInfos.get(i).mWaterMarkCoordX, waterMarkInfos.get(i).mWaterMarkCoordY, waterMarkInfos.get(i).mWaterMarkWidth);
            }
        }

        return mAlivcLivePushConfig;
    }

    private void addWaterMarkInfo() {
        //添加三个水印，位置坐标不同
        WaterMarkInfo waterMarkInfo = new WaterMarkInfo();
        waterMarkInfo.mWaterMarkPath = Common.waterMark;
        WaterMarkInfo waterMarkInfo1 = new WaterMarkInfo();
        waterMarkInfo1.mWaterMarkPath = Common.waterMark;
        waterMarkInfo.mWaterMarkCoordY += 0.2;
        WaterMarkInfo waterMarkInfo2 = new WaterMarkInfo();
        waterMarkInfo2.mWaterMarkPath = Common.waterMark;
        waterMarkInfo2.mWaterMarkCoordY += 0.4;
        waterMarkInfos.add(waterMarkInfo);
        waterMarkInfos.add(waterMarkInfo1);
        waterMarkInfos.add(waterMarkInfo2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CaptureActivity.REQ_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        mUrl.setText(data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT));  //or do sth
                        break;
                    case RESULT_CANCELED:
                        if (data != null) {
                            // for some reason camera is not working correctly
                            mUrl.setText(data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT));
                        }
                        break;
                }
                break;
            case REQ_CODE_PUSH: {
                if (mWaterLinear != null && mWaterMark.isChecked()) {
                    mWaterLinear.setVisibility(View.GONE);
                }
                if (mTargetRate != null && mMinRate != null) {

                    if (!mTargetRate.getText().toString().isEmpty() || Integer.valueOf(mTargetRate.getHint().toString()) != SharedPreferenceUtils.getTargetBit(getApplicationContext())) {
                        mTargetRate.setText(String.valueOf(SharedPreferenceUtils.getTargetBit(getApplicationContext())));
                    }

                    if (!mMinRate.getText().toString().isEmpty() || Integer.valueOf(mMinRate.getHint().toString()) != SharedPreferenceUtils.getMinBit(getApplicationContext())) {
                        mMinRate.setText(String.valueOf(SharedPreferenceUtils.getMinBit(getApplicationContext())));
                    }
                }
            }
            break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                if (manager == null) {
                    manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                }
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferenceUtils.clear(getApplicationContext());
    }
}
