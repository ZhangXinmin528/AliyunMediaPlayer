package com.example.aliyunlivedemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangXinmin on 2017/11/21.
 * Copyright (c) 2017 . All rights reserved.
 * 直播页面Adapter
 */

public class LivePushFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> list = new ArrayList<>();

    public LivePushFragmentAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
