package com.example.aliyunlivedemo.vod;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aliyunlivedemo.R;
import com.example.aliyunlivedemo.base.BaseActivity;
import com.example.aliyunlivedemo.vod.model.VideoInfo;

import java.util.ArrayList;
import java.util.List;

import static com.example.aliyunlivedemo.vod.VodActivity.PARAMS_URL;

/**
 * Created by ZhangXinmin on 2017/11/13.
 * Copyright (c) 2017 . All rights reserved.
 * 点播分类界面
 */

public class VodSortActivity extends BaseActivity {
    private Context mContext;
    private LinearLayoutCompat mContainer;
    private List<VideoInfo> mDataList;

    @Override
    protected Object setLayout() {
        return R.layout.activity_vod_sort;
    }

    @Override
    protected void initParamsAndValues() {
        mContext = this;
        mDataList = new ArrayList<>();
    }

    @Override
    protected void initViews() {
        super.initViews();
        mContainer = findViewById(R.id.container_vod);

    }

    @Override
    protected void initData() {
        super.initData();

        //init data
        VideoInfo video1 = new VideoInfo();
        video1.setId(0);
        video1.setTitle(".Ready For It");
        video1.setAuthor("Taylor Swift");
        video1.setLink("http://221.228.226.18/11/j/u/o/w/juowsjmdnokhbqojfypdbzjhlirsas/" +
                "sh.yinyuetai.com/AF1F015F67F1659CF2F5CE36D70F76EF.mp4");

        VideoInfo video2 = new VideoInfo();
        video2.setId(1);
        video2.setTitle("When I Was Young");
        video2.setAuthor("--");
        video2.setLink("http://112.253.22.158/19/r/l/a/e/rlaezuhwskbflltybkopedmyaeacuy/" +
                "he.yinyuetai.com/1162015FA0F23352954338E9644C1DAA.mp4");

        VideoInfo video3 = new VideoInfo();
        video3.setId(2);
        video3.setTitle("Torches 电影《变形金刚5:最后的骑士》中国区片尾曲");
        video3.setAuthor("张杰");
        video3.setLink("http://112.253.22.164/7/z/i/e/s/ziesihyohbxkkogkoyykskujrapgfs/" +
                "he.yinyuetai.com/F170015C9F2654CA02A19688B29966CD.mp4");

        VideoInfo video4 = new VideoInfo();
        video4.setId(4);
        video4.setTitle("Hurt 电影<金刚狼3:殊死一战>原声带");
        video4.setAuthor("Johnny Cash");
        video4.setLink("http://112.253.22.157/17/p/a/d/o/padoeqmsgejhkqpvvuguuzhvcyhcqx/" +
                "sh.yinyuetai.com/70C80159F0CE44EC16C52799F76C8556.mp4");

        VideoInfo video5 = new VideoInfo();
        video5.setId(5);
        video5.setTitle("Unconditionally");
        video5.setAuthor("Katy Perry");
        video5.setLink("http://112.253.22.153/5/u/w/e/f/uwefrhjuwrayebknhiztkuuqaozhzm/" +
                "he.yinyuetai.com/4F2C0142750E4ADD37A7CF0DB7A06833.flv");

        VideoInfo video6 = new VideoInfo();
        video6.setId(6);
        video6.setTitle("Fifteen 中英字幕");
        video6.setAuthor("Taylor Swift");
        video6.setLink("http://112.253.22.163/6/v/x/l/d/vxldcbqrerhegsbhdvpwujjkomjixc/" +
                "he.yinyuetai.com/5634014097C4ECEEFCE20BFFF8E11178.flv");

        VideoInfo video7 = new VideoInfo();
        video7.setId(7);
        video7.setTitle("Homily 课程");
        video7.setAuthor("Homily");
        video7.setLink("http://iptv.legu168.com/gsjt/gszfx4194.flv");

        mDataList.add(video1);
        mDataList.add(video2);
        mDataList.add(video3);
        mDataList.add(video4);
        mDataList.add(video5);
        mDataList.add(video6);
        mDataList.add(video7);

        addItemView();
    }

    private void addItemView() {
        if (mDataList == null || mDataList.isEmpty())
            return;

        final int size = mDataList.size();
        for (int i = 0; i < size; i++) {
            mContainer.addView(initItemView(mDataList.get(i)));
        }
    }

    /**
     * init item view，then set data
     *
     * @param videoInfo
     * @hide
     */
    private LinearLayoutCompat initItemView(@Nullable final VideoInfo videoInfo) {
        LinearLayoutCompat itemLayout =
                (LinearLayoutCompat) LayoutInflater.from(mContext)
                        .inflate(R.layout.layout_vod_sort_item, null);
        //margin
        LinearLayoutCompat.LayoutParams layoutParams =
                new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, mContext.getResources().getDimensionPixelSize(R.dimen.dp_1)
                , 0, 0);
        itemLayout.setLayoutParams(layoutParams);

        //title
        TextView title = itemLayout.findViewById(R.id.tv_vod_sort_title);
        title.setText(videoInfo.getTitle());

        //author
        TextView author = itemLayout.findViewById(R.id.tv_vod_sort_author);
        author.setText(videoInfo.getAuthor());

        //点击事件
        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vod = new Intent(mContext, VodActivity.class);
                vod.putExtra(PARAMS_URL, videoInfo.getLink());
                startActivity(vod);
            }
        });
        return itemLayout;
    }
}
