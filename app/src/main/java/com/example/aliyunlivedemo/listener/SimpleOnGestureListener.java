package com.example.aliyunlivedemo.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by ZhangXinmin on 2017/11/18.
 * Copyright (c) 2017 . All rights reserved.
 * 手势监听
 */

public class SimpleOnGestureListener implements GestureDetector.OnGestureListener {
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
