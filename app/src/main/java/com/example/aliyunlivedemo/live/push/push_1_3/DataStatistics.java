package com.example.aliyunlivedemo.live.push.push_1_3;

import com.example.aliyunlivedemo.listener.ReportListener;

/**
 * Created by ZhangXinmin on 2017/11/18.
 * Copyright (c) 2017 . All rights reserved.
 * 数据统计类
 */

public class DataStatistics implements Runnable {
    private Thread mStatisticThread;
    private ReportListener mReportListener = null;
    private long mInterval;

    public DataStatistics(long interval) {
        this.mInterval = interval;
    }

    public void setReportListener(ReportListener listener) {
        this.mReportListener = listener;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (mReportListener != null) {
                    mReportListener.onInfoReport();
                }
                Thread.sleep(mInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void start() {
        mStatisticThread = new Thread(this);
        mStatisticThread.start();
    }

    public void stop() {
        mReportListener = null;
        mStatisticThread.interrupt();

        try {
            mStatisticThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
