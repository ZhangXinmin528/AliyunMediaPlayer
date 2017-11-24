package com.example.aliyunlivedemo.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.example.aliyunlivedemo.R;

/**
 * Created by ZhangXinmin on 2017/11/23.
 * Copyright (c) 2017 . All rights reserved.
 * 提示性弹窗
 */

public final class DialogUtils {
    private DialogUtils() {
    }

    /**
     * 弹出错误提示
     *
     * @param context
     * @param message 提示信息
     */
    public static void showErrorDialog(@NonNull Context context, String message) {
        if (!TextUtils.isEmpty(message)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(context.getString(R.string.dialog_title));
            dialog.setMessage(message);
            dialog.setNegativeButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
        }
    }
}
