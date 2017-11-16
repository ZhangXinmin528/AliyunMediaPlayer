package com.example.aliyunlivedemo;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.aliyunlivedemo.live.LiveSortActivity;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.vod.VodSortActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {


    private Context mContext;

    @Override
    protected Object setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;
    }

    @Override
    protected void initViews() {
        super.initViews();
        findViewById(R.id.btn_live).setOnClickListener(this);
        findViewById(R.id.btn_vod).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_live:
                intent.setClass(mContext, LiveSortActivity.class);
                break;
            case R.id.btn_vod:
                intent.setClass(mContext, VodSortActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }
}
