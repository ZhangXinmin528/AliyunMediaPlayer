package com.example.aliyunlivedemo.live.pull;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.base.BaseActivity;

import static com.example.aliyunlivedemo.live.pull.LiveActivity.PARAMS_URL;


/**
 * Created by ZhangXinmin on 2017/11/13.
 * Copyright (c) 2017 . All rights reserved.
 * 直播分类界面
 */

public class LiveSortActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;

    @Override
    protected Object setLayout() {
        return R.layout.activity_live_sort;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;
    }

    @Override
    protected void initViews() {
        super.initViews();
        findViewById(R.id.iv_cctv1).setOnClickListener(this);
        findViewById(R.id.iv_cctv3).setOnClickListener(this);
        findViewById(R.id.iv_cctv5).setOnClickListener(this);
        findViewById(R.id.iv_cctv6).setOnClickListener(this);
        findViewById(R.id.iv_hks).setOnClickListener(this);
        findViewById(R.id.iv_asia).setOnClickListener(this);
        findViewById(R.id.tv_pull_flow_test).setOnClickListener(this);
        findViewById(R.id.tv_lol).setOnClickListener(this);
        findViewById(R.id.tv_douyu).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext, LiveActivity.class);
        switch (v.getId()) {
            case R.id.iv_cctv1:
                String cctv1 = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8";
                intent.putExtra(PARAMS_URL, cctv1);
                break;
            case R.id.iv_cctv3:
                String cctv3 = "http://ivi.bupt.edu.cn/hls/cctv3hd.m3u8";
                intent.putExtra(PARAMS_URL, cctv3);
                break;
            case R.id.iv_cctv5:
                String cctv5 = "http://ivi.bupt.edu.cn/hls/cctv5hd.m3u8";
                intent.putExtra(PARAMS_URL, cctv5);
                break;
            case R.id.iv_cctv6:
                String cctv6 = "http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8";
                intent.putExtra(PARAMS_URL, cctv6);
                break;
            case R.id.iv_hks:
                String hks = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
                intent.putExtra(PARAMS_URL, hks);
                break;
            case R.id.iv_asia:
                String dfws = "rtmp://v1.one-tv.com/live/mpegts.stream";
                intent.putExtra(PARAMS_URL, dfws);
            case R.id.tv_pull_flow_test:
                String bookReading = "http://djt-live.boguforum.com/hlHv/hcTest.m3u8";
                intent.putExtra(PARAMS_URL, bookReading);
                break;
            case R.id.tv_lol:
                String quanmin = "http://hls.quanmin.tv/live/1195161204/playlist.m3u8";
                intent.putExtra(PARAMS_URL, quanmin);
                break;
            case R.id.tv_douyu:
                String book = "http://hls3a.douyucdn.cn/live/20360rWIpcRco3kX_550/" +
                        "playlist.m3u8?wsSecret=65ade714defc4a3bb1b95ebee878c95d&wsTime=1510660475&token=h5-douyu-0-20360-77e16aae89931095edd4fb1bb07e41a5&did=AND-CHR|55-852700161510663454550";
                intent.putExtra(PARAMS_URL, book);
                break;
            default:
                break;
        }
        startActivity(intent);
    }
}
