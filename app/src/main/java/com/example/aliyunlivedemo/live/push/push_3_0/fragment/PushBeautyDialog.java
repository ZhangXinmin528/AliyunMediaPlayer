package com.example.aliyunlivedemo.live.push.push_3_0.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alivc.live.pusher.AlivcLivePusher;
import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.live.push.push_3_0.SharedPreferenceUtils;

public class PushBeautyDialog extends DialogFragment implements View.OnClickListener {

    private static final String BEAUTY_ON = "beauty_on";
    private TextView mBeautySwitch;
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
    private boolean mBeautyOn = true;
    private LivePushFragment.BeautyListener mBeautyListener;

    private AlivcLivePusher mAlivcLivePusher = null;

    public static PushBeautyDialog newInstance(boolean beauty) {
        PushBeautyDialog pushBeautyDialog = new PushBeautyDialog();
        Bundle args = new Bundle();
        args.putBoolean(BEAUTY_ON, beauty);
        pushBeautyDialog.setArguments(args);
        return pushBeautyDialog;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle);
        if(getArguments() != null) {
            mBeautyOn = getArguments().getBoolean(BEAUTY_ON, true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.push_beauty, container);
        mBeautySwitch = (TextView) view.findViewById(R.id.beauty_switch);
        mBeautySwitch.setOnClickListener(this);
        mBeautySwitch.setSelected(!mBeautyOn);
        mBeautySwitch.setText(!mBeautyOn ? getString(R.string.beauty_on) : getString(R.string.beauty_off));
        mSaturationBar = (SeekBar) view.findViewById(R.id.beauty_saturation_seekbar);
        mBrightnessBar = (SeekBar) view.findViewById(R.id.beauty_brightness_seekbar);
        mWhiteBar = (SeekBar) view.findViewById(R.id.beauty_white_seekbar);
        mSkinBar = (SeekBar) view.findViewById(R.id.beauty_skin_seekbar);
        mRuddyBar = (SeekBar) view.findViewById(R.id.beauty_ruddy_seekbar);
        mSaturation = (TextView) view.findViewById(R.id.saturation);
        mBrightness = (TextView) view.findViewById(R.id.brightness);
        mWhite = (TextView) view.findViewById(R.id.white);
        mSkin = (TextView) view.findViewById(R.id.skin);
        mRuddy = (TextView) view.findViewById(R.id.ruddy);
        mSaturationBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mBrightnessBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mWhiteBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSkinBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mRuddyBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mWhite.setText(String.valueOf(SharedPreferenceUtils.getWhiteValue(getActivity().getApplicationContext())));
        mWhiteBar.setProgress(SharedPreferenceUtils.getWhiteValue(getActivity().getApplicationContext()));
        mSkin.setText(String.valueOf(SharedPreferenceUtils.getBuffing(getActivity().getApplicationContext())));
        mSkinBar.setProgress(SharedPreferenceUtils.getBuffing(getActivity().getApplicationContext()));
        mRuddy.setText(String.valueOf(SharedPreferenceUtils.getRuddy(getActivity().getApplicationContext())));
        mRuddyBar.setProgress(SharedPreferenceUtils.getRuddy(getActivity().getApplicationContext()));
        mSaturation.setText(String.valueOf(SharedPreferenceUtils.getSaturation(getActivity().getApplicationContext())));
        mSaturationBar.setProgress(SharedPreferenceUtils.getSaturation(getActivity().getApplicationContext()));
        mBrightness.setText(String.valueOf(SharedPreferenceUtils.getBrightness(getActivity().getApplicationContext())));
        mBrightnessBar.setProgress(SharedPreferenceUtils.getBrightness(getActivity().getApplicationContext()));
        return view;
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        super.onResume();

        DisplayMetrics dpMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpMetrics);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();

        p.width = dpMetrics.widthPixels;
        p.height = dpMetrics.heightPixels/2;
        getDialog().getWindow().setAttributes(p);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.beauty_switch:
                if(mAlivcLivePusher != null) {
                    try {
                        boolean selected = mBeautySwitch.isSelected();
                        mAlivcLivePusher.setBeautyOn(selected);
                        mBeautySwitch.setText(selected ? getString(R.string.beauty_off) : getString(R.string.beauty_on));
                        mBeautySwitch.setSelected(!selected);
                        if(mBeautyListener != null) {
                            mBeautyListener.onBeautySwitch(selected);
                        }
                        SharedPreferenceUtils.setBeautyOn(getActivity().getApplicationContext(), selected);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try{
                int seekBarId = seekBar.getId();

                if(mSaturationBar.getId() == seekBarId) {
                    mAlivcLivePusher.setBeautySaturation(mSaturationBar.getProgress());
                    mSaturation.setText(String.valueOf(progress));
                    SharedPreferenceUtils.setSaturation(getActivity().getApplicationContext(), progress);
                } else if(mBrightnessBar.getId() == seekBarId) {
                    mAlivcLivePusher.setBeautyBrightness(mBrightnessBar.getProgress());
                    mBrightness.setText(String.valueOf(progress));
                    SharedPreferenceUtils.setBrightness(getActivity().getApplicationContext(), progress);
                } else if(mWhiteBar.getId() == seekBarId) {
                    mAlivcLivePusher.setBeautyWhite(mWhiteBar.getProgress());
                    mWhite.setText(String.valueOf(progress));
                    SharedPreferenceUtils.setWhiteValue(getActivity().getApplicationContext(), progress);
                } else if(mSkinBar.getId() == seekBarId) {
                    mAlivcLivePusher.setBeautyBuffing(mSkinBar.getProgress());
                    mSkin.setText(String.valueOf(progress));
                    SharedPreferenceUtils.setBuffing(getActivity().getApplicationContext(), progress);
                } else if (mRuddyBar.getId() == seekBarId) {
                    mAlivcLivePusher.setBeautyRuddy(mRuddyBar.getProgress());
                    mRuddy.setText(String.valueOf(progress));
                    SharedPreferenceUtils.setRuddy(getActivity().getApplicationContext(), progress);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void setAlivcLivePusher(AlivcLivePusher alivcLivePusher) {
        this.mAlivcLivePusher = alivcLivePusher;
    }

    public void setBeautyListener(LivePushFragment.BeautyListener beautyListener) {
        this.mBeautyListener = beautyListener;
    }
}
