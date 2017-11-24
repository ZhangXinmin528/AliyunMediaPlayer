package com.example.aliyunlivedemo;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.live.pull.LivePorActivity;
import com.example.aliyunlivedemo.live.pull.LiveSortActivity;
import com.example.aliyunlivedemo.live.push.push_1_3.FlowPushRequestBuilder;
import com.example.aliyunlivedemo.live.push.push_3_0.PushConfigBuilder;
import com.example.aliyunlivedemo.vod.VodSortActivity;

import static com.example.aliyunlivedemo.live.pull.LiveActivity.PARAMS_URL;

public class MainActivity extends BaseActivity implements View.OnClickListener {


    private Context mContext;
    private String mRtmpUrl;

    @Override
    protected Object setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;
        mRtmpUrl = "rtmp://video-center.alivecdn.com/hlHv/hcTest?vhost=djt-live.boguforum.com";
    }

    @Override
    protected void initViews() {
        super.initViews();
        findViewById(R.id.btn_live).setOnClickListener(this);
        findViewById(R.id.btn_vod).setOnClickListener(this);
        findViewById(R.id.btn_push_video_flow_v1_3).setOnClickListener(this);
        findViewById(R.id.btn_push_video_flow_v3_0).setOnClickListener(this);
        findViewById(R.id.btn_pull_video_flow).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_live:
                Intent liveIntent = new Intent(mContext, LiveSortActivity.class);
                startActivity(liveIntent);
                break;
            case R.id.btn_vod:
                Intent vodIntent = new Intent(mContext, VodSortActivity.class);
                startActivity(vodIntent);
                break;
            case R.id.btn_push_video_flow_v1_3://推流测试v1.3
                startPushFlowOld();
                break;
            case R.id.btn_push_video_flow_v3_0://推流测试v3.0
//                Intent pushIntent = new Intent(mContext, PushConfigActivity.class);
//                startActivity(pushIntent);
                startPushFlowNew();
                break;
            case R.id.btn_pull_video_flow://拉流测试
                Intent pullIntent = new Intent(mContext, LivePorActivity.class);
                final String book_reading = "http://djt-live.boguforum.com/hlHv/hcTest.m3u8";
                pullIntent.putExtra(PARAMS_URL, book_reading);
                startActivity(pullIntent);
                break;
            default:
                break;
        }

    }

    /**
     * 跳转到推流界面v1.3
     *
     * @hide
     */
    private void startPushFlowOld() {
        //水印位置
        final String waterurl = "assets://live/watermark/logo.png";

        Intent intent = new FlowPushRequestBuilder()
                .setBestBitrate(600)//最佳比特率
                .setCameraFacing(AlivcMediaFormat.CAMERA_FACING_FRONT)//摄像头
                .setPaddingx(30)
                .setPaddingy(30)
                .setWaterLocation(1)//地址
                .setRtmpUrl(mRtmpUrl)//推流地址
                .setVideoResolution(AlivcMediaFormat.OUTPUT_RESOLUTION_480P)//视频分辨率
                .setScreenOrientation(FlowPushRequestBuilder.SCREEN_PORTRAIT)//横竖屏设置
                .setWatermarkUrl("")//水印
                .setMinBitrate(500)//最小比特率
                .setMaxBitrate(800)//最大比特率
                .setFrameRate(30)//帧速率:必须在0~30之间
                .setInitBitrate(600)
                .createIntent(mContext);
        startActivity(intent);

    }

    private void startPushFlowNew() {
        Intent intent = new PushConfigBuilder()
                .setRtmpUrl(mRtmpUrl)
                .setAsync(false)
                .setAudioOnly(false)
                .setCameraId(0)
                .setFlashOn(false)
                .setScreenOrientation(0)
                .createIntent(mContext);
        startActivity(intent);
    }
}
