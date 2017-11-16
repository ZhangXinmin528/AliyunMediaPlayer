package com.example.aliyunlivedemo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by ZhangXinmin on 2017/11/9.
 * Copyright (c) 2017 . All rights reserved.
 * Base Activity
 */

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * 设置布局属性
     *
     * @return
     */
    protected abstract Object setLayout();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置布局
        if (setLayout() instanceof View) {
            setContentView((View) setLayout());
        } else if (setLayout() instanceof Integer) {
            setContentView((Integer) setLayout());
        } else {
            throw new RuntimeException("You must use the method:setLayout()");
        }

        initParamsAndValues();

        initViews();

        initData();
    }

    /**
     * init params and values
     *
     * @hide
     */
    protected abstract void initParamsAndValues();

    /**
     * init views
     *
     * @hide
     */
    protected void initViews() {

    }

    /**
     * init data
     *
     * @hide
     */
    protected void initData() {

    }
}
