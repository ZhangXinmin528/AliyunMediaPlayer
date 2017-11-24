package com.example.aliyunlivedemo.listener;

/**
 * Created by ZhangXinmin on 2017/11/23.
 * Copyright (c) 2017 . All rights reserved.
 */

public interface LivePauseStateListener {
    /**
     * 暂停状态
     *
     * @param state
     */
    void onPause(boolean state);
}
