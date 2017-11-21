package com.example.aliyunlivedemo.listener;

import android.view.ScaleGestureDetector;

/**
 * Created by ZhangXinmin on 2017/11/18.
 * Copyright (c) 2017 . All rights reserved.
 */

public class SimpleOnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }
}
